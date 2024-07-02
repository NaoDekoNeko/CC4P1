from flask import Flask, request, jsonify
import os
import time
import requests
import threading
import random

app = Flask(__name__)

# Inicialización de variables
task_queue = []
is_leader = False
leader_url = None
heartbeat_interval = 5  # Intervalo de tiempo en segundos para enviar heartbeats

worker_id = os.getenv('WORKER_ID', 'unknown')
worker_port = os.getenv('PORT', '5000')
hostname = os.getenv('HOSTNAME', 'localhost')
workers = ['http://worker1:5001', 'http://worker2:5002']

def process_task(task):
    print(f"Processing task: {task['task_type']} for client {task['client_id']}")
    file_content = task['file_content']
    task_type = task['task_type']
    keyword = task.get('keyword')
    n = task.get('n')
    result = None
    if task_type == 'word_count':
        result = file_content.lower().count(keyword.lower()) if keyword else len(file_content.split())
    elif task_type == 'keyword_search':
        result = keyword.lower() in file_content.lower()
    elif task_type == 'keyword_repetition':
        result = file_content.lower().split().count(keyword.lower()) >= n
    else:
        result = "Invalid task type"
    print(f"Result for client {task['client_id']} is {result}")
    return result

@app.route('/submit_task', methods=['POST'])
def submit_task():
    try:
        task = request.json
        print(f"Received task: {task}")
        result = process_task(task)
        return jsonify({"status": "Task processed successfully", "result": result}), 200
    except Exception as e:
        print(f"Error processing task: {e}")
        return jsonify({"status": f"Error processing task: {e}"}), 400

@app.route('/heartbeat', methods=['POST'])
def heartbeat():
    global is_leader, leader_url
    try:
        leader_id = request.json['worker_id']
        print(f"Received heartbeat from worker {leader_id}")
        if leader_id != worker_id:
            is_leader = False
            leader_url = f"http://worker{leader_id}:{os.getenv('WORKER_PORT')}"
        else:
            print(f"Worker {worker_id} is the leader")
        return jsonify({"status": "Heartbeat received"}), 200
    except Exception as e:
        print(f"Error receiving heartbeat: {e}")
        return jsonify({"status": f"Error receiving heartbeat: {e}"}), 400

@app.route('/is_leader', methods=['GET'])
def get_leader_status():
    return jsonify({"is_leader": is_leader, "worker_id": worker_id}), 200

def send_heartbeat():
    global leader_url, is_leader
    while True:
        time.sleep(heartbeat_interval)
        try:
            if leader_url and leader_url != f"http://{hostname}:{worker_port}":
                response = requests.post(f"{leader_url}/heartbeat", json={"worker_id": worker_id})
                if response.status_code == 200:
                    print(f"Heartbeat sent from worker {worker_id} to leader {leader_url}")
                else:
                    print(f"Failed to send heartbeat from worker {worker_id} to leader {leader_url}")
            else:
                # Check if current worker should become leader
                print(f"No leader detected, worker {worker_id} is checking for leadership")
                for worker in workers:
                    if worker != f"http://{hostname}:{worker_port}":
                        try:
                            response = requests.get(f"{worker}/is_leader")
                            if response.status_code == 200:
                                data = response.json()
                                if data['is_leader']:
                                    leader_url = worker
                                    break
                        except Exception as e:
                            print(f"Error checking leader status from {worker}: {e}")
                else:
                    is_leader = True
                    leader_url = f"http://{hostname}:{worker_port}"
                    print(f"Worker {worker_id} assuming leadership")
        except Exception as e:
            print(f"Error sending heartbeat from worker {worker_id}: {e}")
            if is_leader:
                leader_url = None
                is_leader = False

if __name__ == '__main__':
    if worker_id == '1':
        is_leader = True
        leader_url = f"http://{hostname}:{worker_port}"
        print(f"Worker {worker_id} assuming leadership")
    threading.Thread(target=send_heartbeat).start()
    
    app.run(host='0.0.0.0', port=int(worker_port))
