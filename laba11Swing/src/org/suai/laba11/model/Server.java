package org.suai.laba11.model;

import java.net.*;

public class Server {

    private int listeningPort;
    private String serverName;
    private DatagramSocket serverSocket;
    private InetAddress clientIP;
    private int clientPort;
    private boolean clientHasConnected = false;

    public Server(int listeningPortPort, String clientName) {
        this.listeningPort = listeningPortPort;
        this.serverName = clientName;
    }

    public String getServerName() {
        return serverName;
    }

    public boolean clientHasConnected(){
        return this.clientHasConnected;
    }

    public void setClientHasConnected(boolean clientHasConnected) {
        this.clientHasConnected = clientHasConnected;
    }

    public void setServerName(String clientName) {
        this.serverName = clientName;
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    public int getClientPort() {
        return clientPort;
    }

    public InetAddress getClientIP() {
        return clientIP;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public void setClientIP(InetAddress clientIP) {
        this.clientIP = clientIP;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public boolean tryConnect() {

        try {
            this.serverSocket = new DatagramSocket(listeningPort);
        } catch (SocketException exc) {
            this.serverSocket.close();
            return false;
        }
        return true;
    }
}

