const fs = require('fs-extra');
const path = require('path');

const STORAGE_DIR = path.join(__dirname, '../storage');

const handleDownload = (req, res) => {
  const fileName = req.query.file_name;
  if (!fileName) {
    return res.status(400).json({ error: 'No file name specified' });
  }

  const filePath = path.join(STORAGE_DIR, fileName);
  if (!fs.existsSync(filePath)) {
    return res.status(404).json({ error: 'File not found' });
  }

  res.download(filePath);
};

module.exports = { handleDownload };
