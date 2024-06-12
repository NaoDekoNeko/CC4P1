import os
from flask import request, send_from_directory, jsonify

STORAGE_DIR = "storage/"

def handle_download(request):
    file_name = request.args.get('file_name')
    if not file_name:
        return jsonify({"error": "No file name specified"}), 400

    file_path = os.path.join(STORAGE_DIR, file_name)
    if not os.path.exists(file_path):
        return jsonify({"error": "File not found"}), 404

    return send_from_directory(STORAGE_DIR, file_name)