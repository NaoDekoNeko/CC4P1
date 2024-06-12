import requests
import os

def get_server_url():
    server_ip = input("Enter the server IP address: ")
    server_port = input("Enter the server port: ")
    return f"http://{server_ip}:{server_port}"

SERVER_URL = get_server_url()

def upload_file(file_path):
    abs_file_path = os.path.abspath(file_path)
    with open(abs_file_path, 'rb') as f:
        files = {'file': f}
        response = requests.post(f"{SERVER_URL}/upload", files=files)
        print(response.json())

def list_files():
    response = requests.get(f"{SERVER_URL}/list")
    print(response.json())

def download_file(file_name, download_path):
    if not download_path:
        download_path = os.getcwd() + "/downloads"
    response = requests.get(f"{SERVER_URL}/download", params={'file_name': file_name})
    if response.status_code == 200:
        with open(os.path.join(download_path, file_name), 'wb') as f:
            f.write(response.content)
        print(f"File {file_name} downloaded successfully")
    else:
        print(response.json())

if __name__ == "__main__":
    while True:
        print("Choose an option:")
        print("1. Upload a file")
        print("2. List files")
        print("3. Download a file")
        print("4. Exit")
        choice = input("Enter choice: ")

        if choice == '1':
            file_path = input("Enter the path of the file to upload: ")
            upload_file(file_path)
        elif choice == '2':
            list_files()
        elif choice == '3':
            file_name = input("Enter the name of the file to download: ")
            download_path = input("Enter the path where the file should be saved (press Enter to save in the current directory): ")
            download_file(file_name, download_path)
        elif choice == '4':
            break
        else:
            print("Invalid choice. Please try again.")
