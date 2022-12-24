package org.suai.laba11.model;

import org.suai.laba11.view.ClientView;
import org.suai.laba11.view.ServerView;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;

public class ChatConnector {


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

    public static void main(String[] args) throws UnknownHostException {

        String[] selectionValues = {"Server", "Client"};
        String initialSection = "Server";

        Object selection = JOptionPane.showInputDialog(null, "Login as : ", "TwoPersonChatApp", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialSection);

        if (selection.equals("Server")) {
            String serverPortString = JOptionPane.showInputDialog("Enter the listening port");

            int listeningPort = 0;
            try {
                listeningPort = Integer.parseInt(serverPortString);
            } catch (NumberFormatException exception) {
                System.out.println("The port number must be an integer!");
                System.exit(1);
            }

            Server server = new Server(listeningPort, "");
            ServerView serverView = new ServerView(server);

        }
        else {
            String IPServer = JOptionPane.showInputDialog("Enter the Server ip address");
            String serverPortString = JOptionPane.showInputDialog("Enter the Server port");


            InetAddress serverIp = null;
            if(checkIPv4(IPServer)) {
                serverIp = InetAddress.getByName(IPServer);
            }
            else{
                System.out.println("Incorrect IP address!");
                System.exit(1);
            }

            int serverPortInt = 0;
            try {
                serverPortInt = Integer.parseInt(serverPortString);
            }
            catch (NumberFormatException exception){
                System.out.println("The port number must be an integer!");
                System.exit(1);
            }

            Client client = new Client(serverIp, serverPortInt, "");
            ClientView clientView = new ClientView(client);


        }
    }
}


