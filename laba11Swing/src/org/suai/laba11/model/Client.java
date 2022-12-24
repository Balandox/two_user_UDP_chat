package org.suai.laba11.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client {

    private InetAddress serverIp;
    private int serverPort;
    private String clientName;
    private DatagramSocket clientSocket;

    public Client(InetAddress serverIp, int serverPort, String clientName){
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }


    public void setClientName(String clientName){
        this.clientName = clientName;
    }

    public DatagramSocket getClientSocket() {
        return clientSocket;
    }

    public int getServerPort() {
        return serverPort;
    }

    public InetAddress getServerIp() {
        return serverIp;
    }

    public boolean tryConnect(){

        try {
            this.clientSocket = new DatagramSocket();
        }
        catch (SocketException exc){
            this.clientSocket.close();
            return false;
        }

        return true;
    }

}
