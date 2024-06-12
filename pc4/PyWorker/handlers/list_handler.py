import os
from flask import jsonify

STORAGE_DIR = "storage/"

def handle_list():
    files = os.listdir(STORAGE_DIR)
    return jsonify({"files": files}), 200