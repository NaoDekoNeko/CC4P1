const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');
const formidable = require('formidable');
const path = require('path');
const fs = require('fs');

const app = express();
app.use(bodyParser.json());
app.use(express.static('public'));

const clientId = Math.random().toString(36).substring(7);
const port = process.env.PORT || 5006;
let leaderUrl = null;

const workers = ["http://worker1:5001", "http://worker3:5003", "http://worker4:5004"];

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

const getLeaderUrl = async () => {
    for (const worker of workers) {
        try {
            const response = await axios.get(`${worker}/is_leader`);
            if (response.data.is_leader) {
                leaderUrl = worker;
                break;
            }
        } catch (error) {
            console.error(`Error checking leader status from ${worker}: ${error.message}`);
        }
    }
};

app.get('/', async (req, res) => {
    await getLeaderUrl();
    res.render('index', { clientId });
});

app.post('/submit_task', (req, res) => {
    const form = formidable({ multiples: true });

    form.parse(req, async (err, fields, files) => {
        if (err) {
            return res.status(400).json({ status: `Error processing request data: ${err.message}` });
        }

        const task = {
            client_id: fields.client_id,
            client_host: 'client-js',
            client_port: port,
            file_content: await fs.promises.readFile(files.file.path, 'utf8'),
            task_type: fields.task_type,
            keyword: fields.keyword,
            n: parseInt(fields.n, 10)
        };

        try {
            const response = await axios.post(`${leaderUrl}/submit_task`, task);
            res.json(response.data);
        } catch (error) {
            res.status(500).json({ status: `Error submitting task: ${error.message}` });
        }
    });
});

app.listen(port, () => {
    console.log(`Client running on port ${port}`);
});
