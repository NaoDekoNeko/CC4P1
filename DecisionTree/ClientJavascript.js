const net = require('net');
const { parse, stringify } = JSON;

const port = 1703;
const host = 'localhost';

let featureIndex;
let threshold;
let dataBuffer = '';

const client = new net.Socket();

client.connect(port, host, () => {
    console.log('Connected to server');
});

client.on('data', function(data) {
    dataBuffer += data.toString();

    if (dataBuffer.includes('END_OF_TRANSMISSION')) {
        console.error('End of transmission message received from server');
        dataBuffer = dataBuffer.replace('END_OF_TRANSMISSION', '');

        const jsonObject = JSON.parse(dataBuffer);

        featureIndex = jsonObject.featureIndex;
        threshold = jsonObject.threshold;
        console.log('FeatureIndex received from server: ' + featureIndex);
        console.log('Threshold received from server: ' + threshold);

        const dataset = jsonObject.dataset.map(innerArray => innerArray.map(Number));
        console.log('Data received from server: ');
        console.log(dataset);

        split(dataset);

        dataBuffer = '';
    }
});

client.on('error', (err) => {
    throw err;
});



client.on('close', () => {
    console.log('Connection closed');
});

client.on('error', (error) => {    
    console.error('Error:', error);
});

function split(dataset) {
    const leftList = [];
    const rightList = [];

    for (const row of dataset) {
        if (row[featureIndex] <= threshold) {
            leftList.push(row);
        } else {
            rightList.push(row);
        }
    }

    const datasetLeft = leftList;
    const datasetRight = rightList;

    const datasetLeftRight = [datasetLeft, datasetRight];

    const jsonArray = stringify(datasetLeftRight);
    try {
        client.write(jsonArray);

        // Send end of transmission message
        const endOfTransmission = 'END_OF_TRANSMISSION\n';
        client.write(endOfTransmission);
    } catch (e) {
        console.error(e);
    }
}