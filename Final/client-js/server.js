const express = require('express');
const formidable = require('formidable');
const fs = require('fs');
const axios = require('axios');

const app = express();
const port = process.env.PORT || 5006;

const clientId = 'client-js';
let leaderUrl = null;
const workers = ['http://worker1:5001', 'http://worker2:5002', 'http://worker3:5003', 'http://worker4:5004'];

async function getLeaderUrl() {
    for (const worker of workers) {
        try {
            console.log(`Checking leader status from ${worker}`);
            const response = await axios.get(`${worker}/is_leader`);
            if (response.data.is_leader) {
                leaderUrl = worker;
                console.log(`Leader detected: ${leaderUrl}`);
                break;
            }
        } catch (error) {
            console.error(`Error checking leader status from ${worker}: ${error}`);
        }
    }
}

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

app.post('/submit_task', (req, res) => {
    const form = new formidable.IncomingForm();
    form.parse(req, async (err, fields, files) => {
        if (err) {
            console.error(`Error parsing form: ${err}`);
            return res.status(400).json({ status: `Error parsing form: ${err}` });
        }

        const taskType = fields.task_type;
        const keyword = fields.keyword;
        const n = fields.n ? parseInt(fields.n) : undefined;
        const file = files.file;
        const fileContent = fs.readFileSync(file.path, 'utf-8');

        const task = {
            client_id: clientId,
            client_host: 'client-js',
            client_port: port,
            file_content: fileContent,
            task_type: taskType,
            keyword: keyword,
            n: n
        };

        await getLeaderUrl();
        if (!leaderUrl) {
            return res.status(500).json({ status: "No leader available" });
        }

        try {
            console.log(`Submitting task to leader ${leaderUrl} with task: ${JSON.stringify(task)}`);
            const response = await axios.post(`${leaderUrl}/submit_task`, task);
            const resultData = response.data;
            console.log(`Task submitted: ${JSON.stringify(resultData)}`);
            res.json(resultData);
        } catch (error) {
            console.error(`Error submitting task to leader: ${error}`);
            res.status(500).json({ status: `Error submitting task: ${error}` });
        }
    });
});

app.listen(port, () => {
    console.log(`Client-JS running on port ${port}`);
});
