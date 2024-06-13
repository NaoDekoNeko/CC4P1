import threading
import time
import random

class RaftNode:
    def __init__(self, worker_id, worker_addresses):
        self.worker_id = worker_id
        self.worker_addresses = worker_addresses
        self.current_term = 0
        self.voted_for = None
        self.log = []
        self.commit_index = 0
        self.last_applied = 0
        self.state = 'follower'
        self.leader_id = None
        self.election_timeout = random.randint(150, 300) / 1000  # Convert to seconds
        self.start_election()

    def start_election(self):
        self.state = 'candidate'
        self.current_term += 1
        self.voted_for = self.worker_id
        vote_count = 1

        # Simulate getting votes from other workers
        for worker_address in self.worker_addresses:
            if worker_address != self.worker_id:
                if self.request_vote(worker_address):
                    vote_count += 1

        if vote_count > len(self.worker_addresses) / 2:
            self.state = 'leader'
            self.leader_id = self.worker_id
            print(f"{self.worker_id} became the leader")
        else:
            self.state = 'follower'

    def request_vote(self, worker_address):
        # Simulate getting a vote from another worker
        return True

    def run(self):
        while True:
            if self.state == 'follower':
                time.sleep(self.election_timeout)
                self.start_election()
            time.sleep(1)

# Initialize Raft node
worker_addresses = ["http://pyworker:8080", "http://jsworker:8081", "http://javaworker:8082"]
raft_node = RaftNode("pyworker", worker_addresses)
raft_thread = threading.Thread(target=raft_node.run)
raft_thread.start()
