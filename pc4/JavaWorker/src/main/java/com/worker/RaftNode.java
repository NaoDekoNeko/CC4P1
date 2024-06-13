package com.worker;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RaftNode {
    private String workerId;
    private List<String> workerAddresses;
    private String state;
    private String leaderId;
    private long electionTimeout;
    private ExecutorService executorService;

    public RaftNode(String workerId, List<String> workerAddresses) {
        this.workerId = workerId;
        this.workerAddresses = workerAddresses;
        this.state = "follower";
        this.leaderId = null;
        this.electionTimeout = new Random().nextInt((300 - 150) + 1) + 150;
        this.executorService = Executors.newSingleThreadExecutor();
        startElection();
    }

    public void startElection() {
        state = "candidate";
        int voteCount = 1;

        // Simulate getting votes from other workers
        for (String workerAddress : workerAddresses) {
            if (!workerAddress.equals(workerId)) {
                if (requestVote(workerAddress)) {
                    voteCount += 1;
                }
            }
        }

        if (voteCount > workerAddresses.size() / 2) {
            state = "leader";
            leaderId = workerId;
            System.out.println(workerId + " became the leader");
        } else {
            state = "follower";
        }
    }

    public boolean requestVote(String workerAddress) {
        // Simulate getting a vote from another worker
        return true;
    }

    public void run() {
        executorService.submit(() -> {
            while (true) {
                if (state.equals("follower") || state.equals("candidate")) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(electionTimeout);
                        startElection();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public String getState() {
        return state;
    }

    public String getLeaderId() {
        return leaderId;
    }
}
