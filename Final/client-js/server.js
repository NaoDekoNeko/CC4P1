const express = require('express');
const formidable = require('formidable');
const fs = require('fs');
const path = require('path');
const fetch = require('node-fetch');

const app = express();
const port = process.env.PORT || 5006;

const clientId = Math.random().toString(36).substring(7);

let leaderUrl = null;
const workers = ["http://worker1:5001", "http://worker2:5002", "http://worker3:5003", "http://worker4:5004"];

const getLeaderUrl = async () => {
    for (const worker of workers) {
        try {
            const response = await fetch(`${worker}/is_leader`);
            const data = await response.json();
            if (data.is_leader) {
                leaderUrl = worker;
                return;
            }
        } catch (error) {
            console.error(`Error checking leader status from ${worker}: ${error}`);
        }
    }
    console.error('No leader found.');
};

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

app.use(express.static(path.join(__dirname, 'public')));

app.get('/', async (req, res) => {
    await getLeaderUrl();
    res.render('index', { clientId });
});

app.post('/submit_task', (req, res) => {
    const form = new formidable.IncomingForm();
    form.parse(req, async (err, fields, files) => {
        if (err) {
            console.error('Error parsing form:', err);
            return res.status(400).json({ status: 'Error processing request data', error: err.message });
        }

        const fileContent = fs.readFileSync(files.file.path, 'utf-8');
        const taskType = fields.task_type;
        const keyword = fields.keyword || '';
        const n = parseInt(fields.n, 10) || 1;

        const task = {
            client_id: clientId,
            client_host: 'client',
            client_port: port,
            file_content: fileContent,
            task_type: taskType,
            keyword: keyword,
            n: n
        };

        if (!leaderUrl) {
            await getLeaderUrl();
            if (!leaderUrl) {
                return res.status(500).json({ status: 'No leader available' });
            }
        }

        try {
            const response = await fetch(`${leaderUrl}/leader_submit_task`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(task)
            });
            const resultData = await response.json();
            console.log(`Task submitted: ${JSON.stringify(resultData)}`);
            res.json(resultData);
        } catch (error) {
            console.error('Error submitting task to leader:', error);
            res.status(500).json({ status: `Error submitting task: ${error.message}` });
        }
    });
});

app.listen(port, () => {
    console.log(`Client running on port ${port}`);
});
