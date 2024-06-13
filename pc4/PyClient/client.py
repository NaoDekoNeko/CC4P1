import os
import requests

workers = {
    "py": "http://pyworker:8080",
    "js": "http://jsworker:8081",
    "java": "http://javaworker:8082"
}

def get_server_url():
    while True:
        print("Choose the type of worker:")
        print("1. Python Worker")
        print("2. JavaScript Worker")
        print("3. Java Worker")
        choice = input("Enter choice: ")
        if choice == '1':
            return workers["py"]
        elif choice == '2':
            return workers["js"]
        elif choice == '3':
            return workers["java"]
        else:
            print("Invalid choice. Please try again.")

SERVER_URL = get_server_url()
print(f"Selected worker URL: {SERVER_URL}")

def list_files():
    try:
        response = requests.get(f"{SERVER_URL}/list")
        response.raise_for_status()
        try:
            data = response.json()
            print(data)
        except requests.exceptions.JSONDecodeError:
            print("Error: Received response is not a valid JSON")
            print(response.text)
    except requests.exceptions.RequestException as e:
        print(f"HTTP request failed: {e}")

def upload_file(file_path):
    with open(file_path, 'rb') as f:
        files = {'file': f}
        try:
            response = requests.post(f"{SERVER_URL}/upload", files=files)
            response.raise_for_status()
            try:
                print(response.json())
            except requests.exceptions.JSONDecodeError:
                print("Error: Received response is not a valid JSON")
                print(response.text)
        except requests.exceptions.RequestException as e:
            print(f"HTTP request failed: {e}")

def download_file(file_name, download_path='.'):
    try:
        response = requests.get(f"{SERVER_URL}/download", params={'file_name': file_name})
        response.raise_for_status()
        with open(os.path.join(download_path, file_name), 'wb') as f:
            f.write(response.content)
        print(f"File {file_name} downloaded successfully")
    except requests.exceptions.RequestException as e:
        print(f"HTTP request failed: {e}")

if __name__ == "__main__":
    while True:
        print("Choose an option:")
        print("1. Upload a file")
        print("2. List files")
        print("3. Download a file")
        print("4. Change worker")
        print("5. Exit")
        choice = input("Enter choice: ")

        if choice == '1':
            file_path = input("Enter the path of the file to upload: ")
            upload_file(file_path)
        elif choice == '2':
            list_files()
        elif choice == '3':
            file_name = input("Enter the name of the file to download: ")
            download_path = input("Enter the path where the file should be saved (press Enter to save in the current directory): ")
            if download_path == '':
                download_path = '.'
            download_file(file_name, download_path)
        elif choice == '4':
            SERVER_URL = get_server_url()
            print(f"Changed to worker URL: {SERVER_URL}")
        elif choice == '5':
            break
        else:
            print("Invalid choice. Please try again.")
