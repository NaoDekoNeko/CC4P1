import os
from flask import jsonify

STORAGE_DIR = "storage/"

import os

def handle_list():
    storage_dir = 'storage'
    files = os.listdir(storage_dir)
    print(f"Files in storage: {files}")
    return {'files': files}