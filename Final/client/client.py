from quart import Quart, request, render_template, jsonify
import requests
import random
import string
import os
import aiohttp

app = Quart(__name__)

leader_url = None
client_id = ''.join(random.choices(string.ascii_letters + string.digits, k=6))
port = os.getenv('PORT', '5003')

def get_leader_url():
    global leader_url
    workers = ['http://worker1:5001', 'http://worker2:5002']
    for worker in workers:
        try:
            print(f"Checking leader status from {worker}")
            response = requests.get(f"{worker}/is_leader")
            data = response.json()
            if data['is_leader']:
                leader_url = worker
                print(f"Leader detected: {leader_url}")
                break
        except Exception as e:
            print(f"Error checking leader status from {worker}: {e}")

@app.route('/')
async def index():
    get_leader_url()
    return await render_template('index.html', client_id=client_id)

@app.route('/submit_task', methods=['POST'])
async def submit_task():
    get_leader_url()
    if leader_url is None:
        return jsonify({"status": "No leader available"}), 500

    try:
        data = await request.form
        file = (await request.files)['file']
        file_content = file.read()
        file_content = file_content.decode('utf-8')
        task_type = data['task_type']
        keyword = data.get('keyword')
        n = data.get('n', None)
        if n:
            n = int(n)

        task = {
            "client_id": client_id,
            "client_host": "client",
            "client_port": port,
            "file_content": file_content,
            "task_type": task_type,
            "keyword": keyword,
            "n": n
        }

        print(f"Submitting task to leader {leader_url} with task: {task}")
        try:
            response = requests.post(f"{leader_url}/submit_task", json=task)
            result_data = response.json()
            print(f"Task submitted: {result_data}")
            return jsonify(result_data)
        except Exception as e:
            print(f"Error submitting task to leader: {e}")
            return jsonify({"status": f"Error submitting task: {e}"}), 500
    except Exception as e:
        print(f"Error processing request data: {e}")
        return jsonify({"status": f"Error processing request data: {e}"}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(port))
