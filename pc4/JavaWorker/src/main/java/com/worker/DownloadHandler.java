package com.worker;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("file_name=")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        String fileName = query.split("=")[1];
        File file = new File("storage/" + fileName);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        byte[] fileContent = Files.readAllBytes(Paths.get("storage/" + fileName));
        exchange.sendResponseHeaders(200, fileContent.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileContent);
        os.close();
    }
}
