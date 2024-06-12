const fs = require('fs-extra');
const path = require('path');

const STORAGE_DIR = path.join(__dirname, '../storage');

const handleList = (req, res) => {
  fs.readdir(STORAGE_DIR, (err, files) => {
    if (err) return res.status(500).json({ error: err.message });

    res.json({ files });
  });
};

module.exports = { handleList };
