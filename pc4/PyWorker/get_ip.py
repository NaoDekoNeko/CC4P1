import socket

def get_container_ip():
    hostname = socket.gethostname()
    ip_address = socket.gethostbyname(hostname)
    print(f"Container IP Address: {ip_address}")

if __name__ == "__main__":
    get_container_ip()
