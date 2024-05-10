import socket
import json
import numpy as np

HOST = 'localhost'
PORT = 1703

def split_dataset(dataset, feature_index, threshold):
    left_list = [row for row in dataset if row[feature_index] <= threshold]
    right_list = [row for row in dataset if row[feature_index] > threshold]
    return [left_list, right_list]

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST, PORT))
        print(f'Client has connected to server on port {PORT}')

        data = ''
        while True:
            message = s.recv(1024).decode('utf-8')
            if not message:
                break
            data += message
            if 'END_OF_TRANSMISSION' in data:
                data = data.replace('END_OF_TRANSMISSION', '')
                json_data = json.loads(data)
                feature_index = json_data['featureIndex']
                threshold = json_data['threshold']
                print(f'FeatureIndex received from server: {feature_index}')
                print(f'Threshold received from server: {threshold}')
                dataset = np.array(json_data['dataset'])
                print(f'Data received from server: \n{dataset}')
                split_data = split_dataset(dataset.tolist(), feature_index, threshold)
                message = json.dumps(split_data) + 'END_OF_TRANSMISSION\n'
                s.sendall(message.encode('utf-8'))
                data = ''

if __name__ == "__main__":
    main()