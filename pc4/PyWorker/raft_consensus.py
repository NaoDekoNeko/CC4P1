import threading
import socket
import json
import time
import random

class RaftConsensus:
    def __init__(self):
        self.is_leader = False
        self.port = 9000
        self.peers = [("localhost", 9001), ("localhost", 9002)]  # Lista de otros nodos en el clúster
        self.current_term = 0
        self.voted_for = None
        self.log = []

    def start(self):
        threading.Thread(target=self.start_server).start()
        threading.Thread(target=self.start_election_timeout).start()

    def start_server(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('0.0.0.0', self.port))
        server_socket.listen(5)
        print(f"Consensus module started on port {self.port}")

        while True:
            client_socket, _ = server_socket.accept()
            threading.Thread(target=self.handle_client, args=(client_socket,)).start()

    def handle_client(self, client_socket):
        data = client_socket.recv(1024).decode()
        message = json.loads(data)

        if message['type'] == 'request_vote':
            self.handle_request_vote(client_socket, message)
        elif message['type'] == 'append_entries':
            self.handle_append_entries(client_socket, message)

        client_socket.close()

    def start_election_timeout(self):
        while True:
            time.sleep(random.uniform(0.15, 0.3))
            if not self.is_leader:
                self.start_election()

    def start_election(self):
        self.current_term += 1
        self.voted_for = self.port
        votes_received = 1  # Vote for self

        request_vote_msg = {
            'type': 'request_vote',
            'term': self.current_term,
            'candidate_id': self.port,
            'last_log_index': len(self.log) - 1,
            'last_log_term': self.log[-1]['term'] if self.log else 0
        }

        for peer in self.peers:
            threading.Thread(target=self.send_request_vote, args=(peer, request_vote_msg, votes_received)).start()

    def send_request_vote(self, peer, message, votes_received):
        try:
            client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client_socket.connect(peer)
            client_socket.sendall(json.dumps(message).encode())
            response = json.loads(client_socket.recv(1024).decode())

            if response['vote_granted']:
                votes_received += 1
                if votes_received > len(self.peers) // 2:
                    self.is_leader = True
                    print(f"Node on port {self.port} is the leader")
        except:
            pass

    def handle_request_vote(self, client_socket, message):
        response = {
            'term': self.current_term,
            'vote_granted': False
        }

        if message['term'] >= self.current_term and (self.voted_for is None or self.voted_for == message['candidate_id']):
            self.current_term = message['term']
            self.voted_for = message['candidate_id']
            response['vote_granted'] = True

        client_socket.sendall(json.dumps(response).encode())

    def handle_append_entries(self, client_socket, message):
        response = {
            'term': self.current_term,
            'success': False
        }

        if message['term'] >= self.current_term:
            self.current_term = message['term']
            self.log.extend(message['entries'])
            response['success'] = True

        client_socket.sendall(json.dumps(response).encode())

    def is_leader(self):
        return self.is_leader

    # Methods to handle consensus logic like election, log replication, etc.
