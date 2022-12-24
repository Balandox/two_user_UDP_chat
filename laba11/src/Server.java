import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Server {

    public static void main(String[] args) throws UnknownHostException, SocketException {

        int listeningPort = 0;
        try {
            listeningPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        System.out.println("Welcome to chat application for two person!");
        System.out.print("1. Set user name (@name Vasya)\n" +
                "2. Start a chatting with default name(@hello)\n" +
                "3. Exit (@quit)\nSelect action: ");
        Scanner console = new Scanner(System.in);
        String serverName = "";

        String choose = console.nextLine();

        switch (choose) {
            case "@hello":
                serverName = "User2(Server)";
                break;
            case "@quit":
                System.out.println("Good bye!!!");
                System.exit(1);
                break;
            default:
                StringTokenizer stringTokenizer = new StringTokenizer(choose);
                if (Objects.equals(stringTokenizer.nextToken(), "@name")) {
                    serverName = stringTokenizer.nextToken();
                } else {
                    System.out.println("Incorrect input!");
                    System.exit(1);
                }
                break;
        }


        DatagramSocket serverSocket = new DatagramSocket(listeningPort);
        ListenerMessageServerThread listenerMessageThread = new ListenerMessageServerThread(serverSocket, serverName);

        listenerMessageThread.start();

    }
}


    class SendMessageServerThread extends Thread {

        private DatagramSocket serverSocket;
        private byte[] buffer;
        private int clientPort;
        private InetAddress ipOfClient;
        private BufferedReader serverInput;
        private String serverName;

        public SendMessageServerThread(DatagramSocket serverSocket, InetAddress ipOfClient, int clientPort, String serverName) {
            this.serverSocket = serverSocket;
            this.ipOfClient = ipOfClient;
            this.clientPort = clientPort;
            this.buffer = new byte[1024];
            this.serverInput = new BufferedReader(new InputStreamReader(System.in));
            this.serverName = serverName;
        }

        private boolean sendMessage(String message) throws IOException {

            if (Objects.equals(message, "@quit")) {
                this.buffer = "Server has left from chat:(".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(this.buffer, buffer.length, this.ipOfClient, this.clientPort);
                this.serverSocket.send(sendPacket);
                return true;
            }

            this.buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(this.buffer, buffer.length, this.ipOfClient, this.clientPort);
            this.serverSocket.send(sendPacket);
            return false;
        }

        public void run() {


            while (true) {
                try {
                    while (!this.serverInput.ready()) {
                        Thread.sleep(100);
                    }
                    if (sendMessage(this.serverInput.readLine())) {
                        System.out.println("Good Bye " + this.serverName);
                        System.exit(1);
                        this.serverSocket.close();
                    }

                } catch (Exception exception) {
                    System.out.println("Sending message error!");
                }
            }

        }

    }

    class ListenerMessageServerThread extends Thread {
        private DatagramSocket serverSocket;
        private byte[] buffer;
        private InetAddress clientIP = null;
        private int clientPort = 0;
        private boolean firstPacket = true;
        private String serverName;

        public ListenerMessageServerThread(DatagramSocket serverSocket, String serverName) {
            this.serverSocket = serverSocket;
            this.buffer = new byte[1024];
            this.serverName = serverName;
        }


        public void run() {

            while (true) {

                try {
                    DatagramPacket receivePacket = new DatagramPacket(this.buffer, this.buffer.length);
                    if(this.firstPacket)
                        System.out.println("Waiting for the client to connect to the chat...");
                    this.serverSocket.receive(receivePacket);

                    if (this.firstPacket) {
                        this.clientIP = receivePacket.getAddress();
                        this.clientPort = receivePacket.getPort();
                        SendMessageServerThread sendMessageThread = new SendMessageServerThread(this.serverSocket, this.clientIP, this.clientPort, this.serverName);
                        this.firstPacket = false;
                        System.out.println("Client has connected to chat!");
                        sendMessageThread.start();
                    }

                    String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Client: " + receivedString);
                } catch (Exception exception) {
                    System.out.println("Receive message from server error!");
                }

            }
        }
    }