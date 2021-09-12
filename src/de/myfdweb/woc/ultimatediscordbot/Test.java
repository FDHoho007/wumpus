package de.myfdweb.woc.ultimatediscordbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Test extends Thread{

    private Socket socket;
    private int i;

    public Test(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9876);
            while(true)
                new Test(serverSocket.accept()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("new connection from " + socket.getInetAddress().getHostAddress());
            String read;
            while((read = reader.readLine()) != null)
                System.out.println(i++ + ": " + read);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(socket.getInetAddress().getHostAddress() + " disconnected");
    }
}
