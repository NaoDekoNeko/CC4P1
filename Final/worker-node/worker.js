const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');

const app = express();
app.use(bodyParser.json());

const workerId = process.env.WORKER_ID || 'unknown';
const workerPort = process.env.PORT || 5000;
const hostname = process.env.HOSTNAME || 'localhost';
let workers = ["http://worker1:5001", "http://worker2:5002", "http://worker3:5003"];

let isLeader = false;
let leaderUrl = null;
const heartbeatInterval = 1000; // Intervalo de tiempo en milisegundos para enviar heartbeats

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
        res.json({ status: "Heartbeat received" });
    } catch (e) {
        console.error(`Error receiving heartbeat: ${e}`);
        res.status(400).json({ status: `Error receiving heartbeat: ${e}` });
    }
});

app.get('/is_leader', (req, res) => {
    res.json({ is_leader: isLeader, worker_id: workerId });
});

const sendHeartbeat = async () => {
    while (true) {
        try {
            await new Promise(resolve => setTimeout(resolve, heartbeatInterval));
            if (leaderUrl && leaderUrl !== `http://${hostname}:${workerPort}`) {
                try {
                    const response = await axios.post(`${leaderUrl}/heartbeat`, { worker_id: workerId });
                    console.log(`Heartbeat sent from worker ${workerId} to leader ${leaderUrl}`);
                } catch (e) {
                    console.error(`Failed to send heartbeat from worker ${workerId} to leader ${leaderUrl}: ${e}`);
                }
            } else {
                console.log(`No leader detected, worker ${workerId} is checking for leadership`);
                let leaderFound = false;
                for (const worker of workers) {
                    if (worker !== `http://${hostname}:${workerPort}`) {
                        try {
                            const response = await axios.get(`${worker}/is_leader`);
                            if (response.data.is_leader) {
                                leaderUrl = worker;
                                leaderFound = true;
                                break;
                            }
                        } catch (e) {
                            console.error(`Error checking leader status from ${worker}: ${e}`);
                        }
                    }
                }
                if (!leaderFound) {
                    isLeader = true;
                    leaderUrl = `http://${hostname}:${workerPort}`;
                    console.log(`Worker ${workerId} assuming leadership`);
                }
            }
        } catch (e) {
            console.error(`Error sending heartbeat from worker ${workerId}: ${e}`);
            if (isLeader) {
                leaderUrl = null;
                isLeader = false;
            }
        }
    }
};

if (workerId === '1') {
    isLeader = true;
    leaderUrl = `http://${hostname}:${workerPort}`;
    console.log(`Worker ${workerId} assuming leadership`);
}

app.listen(workerPort, () => {
    console.log(`Worker ${workerId} running on port ${workerPort}`);
    sendHeartbeat();
});
