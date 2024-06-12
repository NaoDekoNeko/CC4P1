const os = require('os');

const getContainerIP = () => {
  const interfaces = os.networkInterfaces();
  for (const iface of Object.values(interfaces)) {
    for (const alias of iface) {
      if (alias.family === 'IPv4' && !alias.internal) {
        console.log(`Container IP Address: ${alias.address}`);
        return alias.address;
      }
    }
  }
  return null;
};

getContainerIP();
