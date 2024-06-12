const express = require('express');
const bodyParser = require('body-parser');
const { handleUpload } = require('./handlers/uploadHandler');
const { handleList } = require('./handlers/listHandler');
const { handleDownload } = require('./handlers/downloadHandler');
const { startConsensus } = require('./consensus');

const app = express();
const PORT = 8081;

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.post('/upload', handleUpload);
app.get('/list', handleList);
app.get('/download', handleDownload);

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Worker running on port ${PORT}`);
  startConsensus();
});
