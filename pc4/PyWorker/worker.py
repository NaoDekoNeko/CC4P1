import os
import threading
import requests
from flask import Flask, request, jsonify
from handlers.upload_handler import handle_upload
from handlers.download_handler import handle_download
from handlers.list_handler import handle_list
from raft_consensus import RaftNode

app = Flask(__name__)

worker_addresses = ["http://pyworker:8080", "http://jsworker:8081", "http://javaworker:8082"]
raft_node = RaftNode("pyworker", worker_addresses)
raft_thread = threading.Thread(target=raft_node.run)
raft_thread.start()

def replicate_file(file_path, file_name):
    for worker in worker_addresses:
        try:
            if worker != request.host_url.rstrip('/'):
                files = {'file': open(file_path, 'rb')}
                response = requests.post(f"{worker}/upload", files=files)
                print(f"Replicating {file_name} to {worker}: {response.status_code}")
        except Exception as e:
            print(f"Error replicating {file_name} to {worker}: {str(e)}")

@app.route('/upload', methods=['POST'])
def upload_file():
    result = handle_upload(request)
    if result['status'] == 'success':
        file_path = os.path.join('storage', result['file_name'])
        if raft_node.state == 'leader':
            replicate_file(file_path, result['file_name'])
    return jsonify(result)

@app.route('/download', methods=['GET'])
def download_file():
    return handle_download(request)

@app.route('/list', methods=['GET'])
def list_files():
    result = handle_list()
    print(f"List files response: {result}")
    return jsonify(result)

if __name__ == '__main__':
    print("Python worker starting...")
    app.run(host='0.0.0.0', port=8080)
