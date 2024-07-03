const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');

const app = express();
app.use(bodyParser.json());

const workerId = process.env.WORKER_ID || 'unknown';
const workerPort = process.env.PORT || 5000;
const hostname = process.env.HOSTNAME || 'localhost';
let workers =  ["http://worker1:5001", "http://worker3:5003", "http://worker4:5004"];

let isLeader = false;
let leaderUrl = null;
const heartbeatInterval = 1000; // Intervalo de tiempo en milisegundos para enviar heartbeats
const heartbeatFailureThreshold = 3;
let heartbeatFailures = 0;
let currentWorkerIndex = 0;  // Índice del worker al que se asignará la próxima tarea

const processTask = (task) => {
    console.log(`Processing task: ${task.task_type} for client ${task.client_id}`);
    const fileContent = task.file_content;
    const taskType = task.task_type;
    const keyword = task.keyword;
    const n = task.n;
    let result = null;

    if (taskType === 'word_count') {
        result = keyword ? (fileContent.match(new RegExp(keyword, 'gi')) || []).length : fileContent.split(/\s+/).length;
    } else if (taskType === 'keyword_search') {
        result = fileContent.toLowerCase().includes(keyword.toLowerCase());
    } else if (taskType === 'keyword_repetition') {
        result = (fileContent.match(new RegExp(keyword, 'gi')) || []).length >= n;
    } else {
        result = "Invalid task type";
    }

    console.log(`Result for client ${task.client_id} is ${result}`);
    return result;
};

app.post('/submit_task', (req, res) => {
    try {
        const task = req.body;
        console.log(`Received task: ${JSON.stringify(task)}`);
        const result = processTask(task);
        res.json({ status: "Task processed successfully", result });
    } catch (e) {
        console.error(`Error processing task: ${e}`);
        res.status(400).json({ status: `Error processing task: ${e}` });
    }
});

app.post('/heartbeat', (req, res) => {
    try {
        const leaderId = req.body.worker_id;
        console.log(`Received heartbeat from worker ${leaderId}`);
        if (leaderId !== workerId) {
            isLeader = false;
            leaderUrl = `http://${leaderId}:${workerPort}`;
        } else {
            console.log(`Worker ${workerId} is the leader`);
        }
        heartbeatFailures = 0;  // Reset failures on successful heartbeat
        res.json({ status: "Heartbeat received" });
    } catch (e) {
        console.error(`Error receiving heartbeat: ${e}`);
        res.status(400).json({ status: `Error receiving heartbeat: ${e}` });
    }
});

app.get('/is_leader', (req, res) => {
    console.log(`Checking leader status for worker ${workerId}`);
    res.json({ is_leader: isLeader, worker_id: workerId });
});

const sendHeartbeat = async () => {
    while (true) {
        await new Promise(resolve => setTimeout(resolve, heartbeatInterval));
        try {
            if (isLeader) {
                for (const worker of workers) {
                    if (worker !== `http://${hostname}:${workerPort}`) {
                        try {
                            await axios.post(`${worker}/heartbeat`, { worker_id: workerId });
                            console.log(`Heartbeat sent from leader ${workerId} to worker ${worker}`);
                        } catch (e) {
                            console.error(`Error sending heartbeat from leader ${workerId} to worker ${worker}: ${e}`);
                            heartbeatFailures += 1;
                            if (heartbeatFailures >= heartbeatFailureThreshold) {
                                console.log(`Leader ${workerId} failed. Transferring leadership.`);
                                isLeader = false;
                                transferLeadership();
                            }
                        }
                    }
                }
            } else {
                console.log(`Worker ${workerId} is checking for leadership`);
                checkForLeadership();
            }
        } catch (e) {
            console.error(`Error in heartbeat management for worker ${workerId}: ${e}`);
        }
    }
};

const checkForLeadership = async () => {
    for (const worker of workers) {
        if (worker !== `http://${hostname}:${workerPort}`) {
            try {
                const response = await axios.get(`${worker}/is_leader`);
                if (response.status === 200 && response.data.is_leader) {
                    leaderUrl = worker;
                    console.log(`Leader detected: ${leaderUrl}`);
                    return;
                }
            } catch (e) {
                console.error(`Error checking leader status from ${worker}: ${e}`);
            }
        }
    }
    isLeader = true;
    leaderUrl = `http://${hostname}:${workerPort}`;
    console.log(`Worker ${workerId} assuming leadership`);
};

const transferLeadership = async () => {
    heartbeatFailures = 0;
    for (const worker of workers) {
        if (worker !== `http://${hostname}:${workerPort}`) {
            try {
                const response = await axios.get(`${worker}/is_leader`);
                if (response.status === 200 && !response.data.is_leader) {
                    leaderUrl = worker;
                    console.log(`New leader is ${worker}`);
                    return;
                }
            } catch (e) {
                console.error(`Error transferring leadership to ${worker}: ${e}`);
            }
        }
    }
    isLeader = true;
    leaderUrl = `http://${hostname}:${workerPort}`;
    console.log(`Worker ${workerId} reassuming leadership due to lack of alternatives`);
};

const assignTask = async (task) => {
    let attempts = 0;
    while (attempts < workers.length) {
        const worker = workers[currentWorkerIndex];
        if (worker !== `http://${hostname}:${workerPort}`) {  // No asignarse la tarea a sí mismo
            try {
                const response = await axios.post(`${worker}/submit_task`, task);
                if (response.status === 200) {
                    console.log(`Task assigned to ${worker}`);
                    currentWorkerIndex = (currentWorkerIndex + 1) % workers.length;
                    return response.data;
                }
            } catch (e) {
                console.error(`Error assigning task to ${worker}: ${e}`);
            }
        }
        currentWorkerIndex = (currentWorkerIndex + 1) % workers.length;
        attempts += 1;
    }
    return { status: "Failed to assign task to any worker" };
};

app.post('/leader_submit_task', async (req, res) => {
    if (!isLeader) {
        return res.status(403).json({ status: "Not the leader" });
    }
    const task = req.body;
    console.log(`Leader ${workerId} received task: ${JSON.stringify(task)}`);
    const result = await assignTask(task);
    res.json(result);
});

if (workerId === '1') {
    isLeader = true;
    leaderUrl = `http://${hostname}:${workerPort}`;
    console.log(`Worker ${workerId} assuming leadership`);
}

app.listen(workerPort, () => {
    console.log(`Worker ${workerId} running on port ${workerPort}`);
    sendHeartbeat();
});
