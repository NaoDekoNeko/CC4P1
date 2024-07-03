from flask import Flask, request, jsonify
import os
import time
import requests
import threading
import logging

app = Flask(__name__)

# Configuración de logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Inicialización de variables
task_queue = []
is_leader = False
leader_url = None
heartbeat_interval = 1  # Intervalo de tiempo en segundos para enviar heartbeats
heartbeat_failure_threshold = 3

worker_id = os.getenv('WORKER_ID', 'unknown')
worker_port = os.getenv('PORT', '5000')
hostname = os.getenv('HOSTNAME', 'localhost')
workers =  ["http://worker1:5001", "http://worker3:5003", "http://worker4:5004"]

heartbeat_failures = 0
current_worker_index = 0  # Índice del worker al que se asignará la próxima tarea

def process_task(task):
    logging.info(f"Processing task: {task['task_type']} for client {task['client_id']}")
    file_content = task['file_content']
    task_type = task['task_type']
    keyword = task.get('keyword')
    n = task.get('n')
    result = None
    if task_type == 'word_count':
        result = file_content.lower().count(keyword.lower()) if keyword else len(file_content.split())
        logging.info(f"Word count for client {task['client_id']} is {result}")
    elif task_type == 'keyword_search':
        result = keyword.lower() in file_content.lower()
        logging.info(f"Keyword search for client {task['client_id']} is {result}")
    elif task_type == 'keyword_repetition':
        result = file_content.lower().split().count(keyword.lower()) >= n
        logging.info(f"Keyword repetition for client {task['client_id']} is {result}")
    else:
        result = "Invalid task type"
        logging.info(f"Invalid task type for client {task['client_id']}")
    logging.info(f"Result for client {task['client_id']} is {result}")
    return result

@app.route('/submit_task', methods=['POST'])
def submit_task():
    try:
        task = request.json
        logging.info(f"Received task: {task}")
        result = process_task(task)
        return jsonify({"status": "Task processed successfully", "result": result}), 200
    except Exception as e:
        logging.error(f"Error processing task: {e}")
        return jsonify({"status": f"Error processing task: {e}"}), 400

@app.route('/heartbeat', methods=['POST'])
def heartbeat():
    global is_leader, leader_url, heartbeat_failures
    try:
        leader_id = request.json['worker_id']
        logging.info(f"Received heartbeat from worker {leader_id}")
        if leader_id != worker_id:
            is_leader = False
            leader_url = f"http://worker{leader_id}:{os.getenv('PORT')}"
        else:
            logging.info(f"Worker {worker_id} is the leader")
        heartbeat_failures = 0  # Reset failures on successful heartbeat
        return jsonify({"status": "Heartbeat received"}), 200
    except Exception as e:
        logging.error(f"Error receiving heartbeat: {e}")
        return jsonify({"status": f"Error receiving heartbeat: {e}"}), 400

@app.route('/is_leader', methods=['GET'])
def get_leader_status():
    logging.info(f"Checking leader status for worker {worker_id}")
    return jsonify({"is_leader": is_leader, "worker_id": worker_id}), 200

def send_heartbeat():
    global leader_url, is_leader, heartbeat_failures
    while True:
        time.sleep(heartbeat_interval)
        try:
            if is_leader:
                for worker in workers:
                    if worker != f"http://{hostname}:{worker_port}":
                        try:
                            response = requests.post(f"{worker}/heartbeat", json={"worker_id": worker_id})
                            logging.info(f"Heartbeat sent from leader {worker_id} to worker {worker}")
                        except Exception as e:
                            logging.error(f"Error sending heartbeat from leader {worker_id} to worker {worker}: {e}")
                            heartbeat_failures += 1
                            if heartbeat_failures >= heartbeat_failure_threshold:
                                logging.info(f"Leader {worker_id} failed. Transferring leadership.")
                                is_leader = False
                                transfer_leadership()
            else:
                logging.info(f"Worker {worker_id} is checking for leadership")
                check_for_leadership()
        except Exception as e:
            logging.error(f"Error in heartbeat management for worker {worker_id}: {e}")

def check_for_leadership():
    global is_leader, leader_url, heartbeat_failures
    for worker in workers:
        if worker != f"http://{hostname}:{worker_port}":
            try:
                response = requests.get(f"{worker}/is_leader")
                if response.status_code == 200:
                    data = response.json()
                    if data['is_leader']:
                        leader_url = worker
                        logging.info(f"Leader detected: {leader_url}")
                        return
            except Exception as e:
                logging.error(f"Error checking leader status from {worker}: {e}")
    is_leader = True
    leader_url = f"http://{hostname}:{worker_port}"
    logging.info(f"Worker {worker_id} assuming leadership")

def transfer_leadership():
    global is_leader, leader_url, heartbeat_failures
    heartbeat_failures = 0
    for worker in workers:
        if worker != f"http://{hostname}:{worker_port}":
            try:
                response = requests.get(f"{worker}/is_leader")
                if response.status_code == 200:
                    data = response.json()
                    if not data['is_leader']:
                        leader_url = worker
                        logging.info(f"New leader is {worker}")
                        return
            except Exception as e:
                logging.error(f"Error transferring leadership to {worker}: {e}")
    is_leader = True
    leader_url = f"http://{hostname}:{worker_port}"
    logging.info(f"Worker {worker_id} reassuming leadership due to lack of alternatives")

def assign_task(task):
    global current_worker_index
    attempts = 0
    while attempts < len(workers):
        worker = workers[current_worker_index]
        if worker != f"http://{hostname}:{worker_port}":  # No asignarse la tarea a sí mismo
            try:
                response = requests.post(f"{worker}/submit_task", json=task)
                if response.status_code == 200:
                    logging.info(f"Task assigned to {worker}")
                    current_worker_index = (current_worker_index + 1) % len(workers)
                    return response.json()
            except Exception as e:
                logging.error(f"Error assigning task to {worker}: {e}")
        current_worker_index = (current_worker_index + 1) % len(workers)
        attempts += 1
    return {"status": "Failed to assign task to any worker"}

@app.route('/leader_submit_task', methods=['POST'])
def leader_submit_task():
    if not is_leader:
        return jsonify({"status": "Not the leader"}), 403
    task = request.json
    logging.info(f"Leader {worker_id} received task: {task}")
    result = assign_task(task)
    return jsonify(result)

if __name__ == '__main__':
    if worker_id == '1':
        is_leader = True
        leader_url = f"http://{hostname}:{worker_port}"
        logging.info(f"Worker {worker_id} assuming leadership")
    threading.Thread(target=send_heartbeat).start()
    
    app.run(host='0.0.0.0', port=int(worker_port))