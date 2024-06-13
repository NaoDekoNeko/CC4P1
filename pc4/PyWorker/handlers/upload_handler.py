import os
from flask import request, jsonify

STORAGE_DIR = "storage/"

import os
from flask import jsonify

def handle_upload(request):
    storage_dir = 'storage'
    if 'file' not in request.files:
        return {'status': 'error', 'message': 'No file part'}
    file = request.files['file']
    if file.filename == '':
        return {'status': 'error', 'message': 'No selected file'}
    file_path = os.path.join(storage_dir, file.filename)
    file.save(file_path)
    return {'status': 'success', 'file_name': file.filename}