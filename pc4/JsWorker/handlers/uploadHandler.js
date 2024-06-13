const fs = require('fs-extra');
const path = require('path');
const axios = require('axios');
const FormData = require('form-data');
const RaftNode = require('../raftNode'); // Asegúrate de que la ruta es correcta

const STORAGE_DIR = path.join(__dirname, '../storage');

const workerAddresses = ["http://pyworker:8080", "http://jsworker:8081", "http://javaworker:8082"];
const raftNode = new RaftNode("jsworker", workerAddresses);
raftNode.run();

const replicateFile = (filePath, fileName) => {
    workerAddresses.forEach(worker => {
        if (worker !== `http://jsworker:8081`) {
            const formData = new FormData();
            formData.append('file', fs.createReadStream(filePath));
            axios.post(`${worker}/upload`, formData, {
                headers: formData.getHeaders()
            }).then(response => {
                console.log(`Replicating ${fileName} to ${worker}: ${response.status}`);
            }).catch(error => {
                console.error(`Error replicating ${fileName} to ${worker}: ${error.message}`);
            });
        }
    });
};

const handleUpload = (req, res) => {
    console.log('Handling file upload...');
    console.log('Request headers:', req.headers);

    const contentType = req.headers['content-type'];
    if (!contentType || !contentType.startsWith('multipart/form-data')) {
        return res.status(400).json({ error: 'Content-Type must be multipart/form-data' });
    }

    let fileBuffer = Buffer.alloc(0);
    req.on('data', (chunk) => {
        fileBuffer = Buffer.concat([fileBuffer, chunk]);
    });

    req.on('end', () => {
        console.log('Received file data');
        const boundary = '--' + contentType.split('boundary=')[1];
        const parts = fileBuffer.toString().split(boundary).slice(1, -1);

        if (parts.length === 0) {
            return res.status(400).json({ error: 'No files were uploaded.' });
        }

        const filePart = parts[0];
        const [header, fileContent] = filePart.split('\r\n\r\n');
        const headerLines = header.toString().split('\r\n');
        const filenameLine = headerLines.find(line => line.includes('filename='));
        const filename = filenameLine.match(/filename="([^"]+)"/)[1].trim();

        console.log('Saving file:', filename);
        const filePath = path.join(STORAGE_DIR, filename);

        // Write the file content to the file system
        fs.writeFile(filePath, fileContent.trim(), (err) => {
            if (err) {
                console.error('Error saving file:', err.message);
                return res.status(500).json({ error: err.message });
            }
            if (raftNode.getState() === 'leader') {
                replicateFile(filePath, filename);
            }
            res.json({ message: 'File uploaded successfully' });
        });
    });

    req.on('error', (err) => {
        console.error('Request error:', err.message);
        res.status(500).json({ error: err.message });
    });
};

module.exports = { handleUpload };
