import os
from flask import request, jsonify

STORAGE_DIR = "storage/"

def handle_upload(request):
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    file_path = os.path.join(STORAGE_DIR, file.filename)
    file.save(file_path)
    return jsonify({"message": "File uploaded successfully"}), 200