import os
from flask import Flask, request, jsonify
from handlers.upload_handler import handle_upload
from handlers.download_handler import handle_download
from handlers.list_handler import handle_list
import requests

app = Flask(__name__)

workers = [
    "http://pyworker:8080",
    "http://jsworker:8081",
    "http://javaworker:8082"
]

def replicate_file(file_path, file_name):
    for worker in workers:
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
        replicate_file(file_path, result['file_name'])
    return jsonify(result)

@app.route('/download', methods=['GET'])
def download_file():
    return handle_download(request)

@app.route('/list', methods=['GET'])
def list_files():
    return handle_list(request)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
