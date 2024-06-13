package com.worker;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> workerAddresses = Arrays.asList("http://pyworker:8080", "http://jsworker:8081", "http://javaworker:8082");
        RaftNode raftNode = new RaftNode("javaworker", workerAddresses);
        raftNode.run();

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8082), 0);
        server.createContext("/upload", new UploadHandler(raftNode));
        server.createContext("/download", new DownloadHandler());
        server.createContext("/list", new ListHandler());
        server.createContext("/requestVote", new RequestVoteHandler(raftNode));
        server.createContext("/appendEntries", new AppendEntriesHandler(raftNode));
        server.setExecutor(null);
        server.start();
    }
}
