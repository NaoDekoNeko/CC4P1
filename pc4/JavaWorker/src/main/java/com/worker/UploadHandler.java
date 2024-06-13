package com.worker;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UploadHandler implements HttpHandler {
    private static final Gson gson = new Gson();
    private static final String[] WORKERS = {
        "http://pyworker:8080",
        "http://jsworker:8081",
        "http://javaworker:8082"
    };
    private final RaftNode raftNode;

    public UploadHandler(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Handling file upload...");

        if (!"POST".equals(exchange.getRequestMethod())) {
            System.out.println("Invalid request method: " + exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            System.out.println("Invalid content type: " + contentType);
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        String boundary = "--" + contentType.split("boundary=")[1];
        InputStream inputStream = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }

        byte[] body = buffer.toByteArray();
        String bodyString = new String(body);
        String[] parts = bodyString.split(boundary);

        for (String part : parts) {
            if (part.contains("filename=\"")) {
                System.out.println("Processing part: " + part);
                String[] headers = part.split("\r\n");
                String fileName = headers[1].split("filename=\"")[1].split("\"")[0];
                int fileStart = part.indexOf("\r\n\r\n") + 4;
                int fileEnd = part.lastIndexOf("\r\n--");
                if (fileEnd == -1) {
                    fileEnd = part.length() - 2;
                }
                byte[] fileContent = part.substring(fileStart, fileEnd).getBytes();

                Files.write(Paths.get("storage/" + fileName), fileContent);
                System.out.println("File " + fileName + " uploaded successfully");
                if (raftNode.getState().equals("leader")) {
                    replicateFile("storage/" + fileName, fileName);
                }
                Map<String, String> response = new HashMap<>();
                response.put("message", "File uploaded successfully");
                String jsonResponse = gson.toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
                return;
            }
        }

        System.out.println("No files were uploaded.");
        exchange.sendResponseHeaders(400, -1); // Bad Request
    }

    private void replicateFile(String filePath, String fileName) {
        for (String worker : WORKERS) {
            if (!worker.equals("http://javaworker:8082")) {
                try {
                    File file = new File(filePath);
                    HttpURLConnection connection = (HttpURLConnection) new URL(worker + "/upload").openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---ContentBoundary");

                    OutputStream os = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os), true);

                    writer.append("---ContentBoundary\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                    writer.append("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n");
                    writer.flush();

                    Files.copy(file.toPath(), os);
                    os.flush();

                    writer.append("\r\n---ContentBoundary--\r\n");
                    writer.flush();

                    int responseCode = connection.getResponseCode();
                    System.out.println("Replicating " + fileName + " to " + worker + ": " + responseCode);

                    writer.close();
                    os.close();
                } catch (Exception e) {
                    System.err.println("Error replicating " + fileName + " to " + worker + ": " + e.getMessage());
                }
            }
        }
    }
}
