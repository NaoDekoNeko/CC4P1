package service;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;



public class Client {
    private static SocketChannel clientSocket;
    private static ByteBuffer buffer;
    private static Client instance;
    private static byte[] response;
    private final static int LEN_BUFFER = 4096;
    
    public static Client start(String hostname, int port) {
        if (instance == null)
        	try {
                System.out.println("attempt");
        		instance = new Client(hostname,port);
                System.out.println("conected");
        	}
        	catch(Exception e) {
        		System.out.println("Cannot connect to Server !");
        	}
            
        return instance;
    }
    public static void close() throws IOException {
    	Client.clientSocket.close();
    	Client.instance = null;
    }
    
    private Client(String hostname, int port ) throws IOException  {
        clientSocket = SocketChannel.open(new InetSocketAddress(hostname, port));
        buffer = ByteBuffer.allocate(LEN_BUFFER);    
    }
    
    public static byte[] sendMessage(String msg) {
    	Client.clearBufffer();
        Client.buffer.put(0,msg.getBytes());
        Client.response = null;
        try {
        	Client.clientSocket.write(Client.buffer);
        	Client.clearBufffer();
        	int len = Client.clientSocket.read(Client.buffer);
            System.out.println(len);
            if (len < 4096){
                System.out.println("Khong du:" + len);
            }
            Client.response = Client.buffer.array();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Client.response;

    }

    
    
    private static void clearBufffer() {
    	Client.buffer.put(0,new byte[Client.LEN_BUFFER]);
    	Client.buffer.clear();
    }
    
    
    
}
