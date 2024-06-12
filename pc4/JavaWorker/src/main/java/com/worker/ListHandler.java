package com.worker;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;

public class ListHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        File folder = new File("storage");
        File[] listOfFiles = folder.listFiles();
        StringBuilder response = new StringBuilder("[");

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    response.append("\"").append(file.getName()).append("\",");
                }
            }
            if (response.length() > 1) {
                response.setLength(response.length() - 1); // Remove trailing comma
            }
        }

        response.append("]");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
