import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Client {

    public static boolean checkIPv4(final String ip) {
        boolean isIPv4;

        if(Objects.equals(ip, "localhost"))
            return true;

        try {
            final InetAddress inet = InetAddress.getByName(ip);
            isIPv4 = inet.getHostAddress().equals(ip)
                    && inet instanceof Inet4Address;
        } catch (final UnknownHostException e) {
            isIPv4 = false;
        }

        return isIPv4;
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        Date date = new Date();
        System.out.println(date);
        InetAddress serverIp = null;

        if(checkIPv4(args[0])) {
            serverIp = InetAddress.getByName(args[0]);
        }
        else{
            System.out.println("Incorrect IP address!");
            System.exit(1);
        }

        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception){
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        System.out.println("Welcome to chat application for two person!");
        System.out.print("1. Set user name (@name Vasya)\n" +
                "2. Start a chatting with default name(@hello)\n" +
                "3. Exit (@quit)\nSelect action: ");
        Scanner console = new Scanner(System.in);
        String clientName = "";

        String choose = console.nextLine();

        switch (choose){
            case "@hello":
                clientName = "User1(Client)";
                break;
            case "@quit":
                System.out.println("Good bye!!!");
                System.exit(1);
                break;
            default:
                StringTokenizer stringTokenizer = new StringTokenizer(choose);
                if(Objects.equals(stringTokenizer.nextToken(), "@name")) {
                    clientName = stringTokenizer.nextToken();
                }
                else {
                    System.out.println("Incorrect input!");
                    System.exit(1);
                }
                break;
        }


        DatagramSocket clientSocket = new DatagramSocket();
        ListenerMessageThread listenerMessageThread = new ListenerMessageThread(clientSocket);
        SendMessageThread sendMessageThread = new SendMessageThread(clientSocket, serverIp, serverPort, clientName);

        listenerMessageThread.start();
        sendMessageThread.start();


    }

}


class SendMessageThread extends Thread{

    private DatagramSocket clientSocket;
    private byte[] buffer;
    private int serverPort;
    private InetAddress ipOfServer;
    private BufferedReader clientInput;
    private String clientName;

    public SendMessageThread(DatagramSocket clientSocket, InetAddress ipOfServer, int serverPort, String clientName){
        this.clientSocket = clientSocket;
        this.ipOfServer = ipOfServer;
        this.serverPort = serverPort;
        this.buffer = new byte[1024];
        this.clientInput = new BufferedReader(new InputStreamReader(System.in));
        this.clientName = clientName;
    }

    private boolean sendMessage(String message) throws IOException {


        if(Objects.equals(message, "@quit")) {
            this.buffer = "Client has left from chat:(".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(this.buffer, buffer.length, this.ipOfServer, this.serverPort);
            this.clientSocket.send(sendPacket);
            return true;
        }

        this.buffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(this.buffer, buffer.length, this.ipOfServer, this.serverPort);
        this.clientSocket.send(sendPacket);
        return false;
    }

    public void run(){


        while(true){
            try{
                while(!this.clientInput.ready()){
                    Thread.sleep(100);
                }
                if(sendMessage(this.clientInput.readLine())){
                    System.out.println("Good Bye " + this.clientName);
                    System.exit(1);
                    this.clientSocket.close();
                }

            }
            catch (Exception exception){
                System.out.println("Sending message error!");
            }
        }

    }

}

class ListenerMessageThread extends Thread{
    private DatagramSocket clientSocket;
    private byte[] buffer;

    public ListenerMessageThread(DatagramSocket clientSocket){
        this.clientSocket = clientSocket;
        this.buffer = new byte[1024];
    }


    public void run(){

        while(true){

            try{
             DatagramPacket receivePacket = new DatagramPacket(this.buffer, this.buffer.length);
             this.clientSocket.receive(receivePacket);
             String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());
             System.out.println("Server: " + receivedString);
          }
          catch (Exception exception){
              System.out.println("Receive message from server error!");
          }

        }
    }

}

