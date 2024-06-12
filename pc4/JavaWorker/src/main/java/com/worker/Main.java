package com.worker;

import com.sun.net.httpserver.HttpServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8082), 0);
        server.createContext("/upload", new UploadHandler());
        server.createContext("/list", new ListHandler());
        server.createContext("/download", new DownloadHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        // Obtener y mostrar la IP del contenedor
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr.isLoopbackAddress()) continue;

                System.out.println("Container IP Address: " + addr.getHostAddress());
            }
        }

        System.out.println("Java Worker running on port 8082");
    }
}
