package org.suai.laba11.view;

import org.suai.laba11.model.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;

    public class ServerView extends JFrame {

        private Server server;

        private JTextField jtfMessage;
        private JTextField jtfName;
        private JTextArea jtaTextAreaMessage;

        public ServerView(Server server) {

            this.server = server;

            if(!server.tryConnect()){
                System.out.println("Connection fail!!!");
                System.exit(1);
            }

            setBounds(600, 300, 600, 500);
            setTitle("Server");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            jtaTextAreaMessage = new JTextArea();
            jtaTextAreaMessage.setEditable(false);
            jtaTextAreaMessage.setLineWrap(true);
            JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
            add(jsp, BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            add(bottomPanel, BorderLayout.SOUTH);
            JButton jbSendMessage = new JButton("Send");
            bottomPanel.add(jbSendMessage, BorderLayout.EAST);
            jtfMessage = new JTextField("Input your message: ");
            bottomPanel.add(jtfMessage, BorderLayout.CENTER);
            jtfName = new JTextField("Input your name: ");
            bottomPanel.add(jtfName, BorderLayout.WEST);

            JRootPane rootPane = SwingUtilities.getRootPane(jbSendMessage);
            rootPane.setDefaultButton(jbSendMessage);

            jbSendMessage.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // если имя клиента, и сообщение непустые, то отправляем сообщение
                    if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty() && server.clientHasConnected()) {
                        server.setServerName(jtfName.getText());
                        sendMsg();
                        // фокус на текстовое поле с сообщением
                        jtfMessage.grabFocus();
                    }
                }
            });

            // при фокусе поле сообщения очищается
            jtfMessage.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    jtfMessage.setText("");
                }
            });

            // при фокусе поле имя очищается
            jtfName.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    jtfName.setText("");
                }
            });

            ListenerMessageServerThread listenerMessageServerThread = new ListenerMessageServerThread(this.server, this.server.getServerSocket(), this.jtaTextAreaMessage);
            listenerMessageServerThread.start();

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);

                    byte[] buffer = null;
                    DatagramPacket sendPacket = null;
                    // здесь проверяем, что имя клиента непустое и не равно значению по умолчанию
                    if (!server.getServerName().isEmpty() && !Objects.equals(server.getServerName(), "Input your name: "))
                        buffer = (server.getServerName() + " has left from the chat!").getBytes();
                    else
                        buffer = "Server has left the chat room without introducing himself".getBytes();

                    sendPacket = new DatagramPacket(buffer, buffer.length, server.getClientIP(), server.getClientPort());

                    try {
                        server.getServerSocket().send(sendPacket);
                        server.getServerSocket().close();
                    }
                    catch (IOException exception){
                        System.out.println("Sending message error!");
                    }

                }
            });

            setVisible(true);
        }

        public void sendMsg() {
            // формируем сообщение для отправки на сервер
            String message = jtfName.getText() + ": " + jtfMessage.getText();
            // отправляем сообщение
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, server.getClientIP(), server.getClientPort());
            try {
                this.server.getServerSocket().send(sendPacket);
            }
            catch (IOException exception){
                System.out.println("Sending message error!");
            }
            jtaTextAreaMessage.append("Me: " + jtfMessage.getText() + "\n");
            jtfMessage.setText("");
        }

    }

    class ListenerMessageServerThread extends Thread{
        private Server server;
        private final DatagramSocket serverSocket;
        private JTextArea jtaTextAreaMessage;
        private byte[] buffer;

        public ListenerMessageServerThread(Server server, DatagramSocket serverSocket, JTextArea jtaTextAreaMessage){
            this.server = server;
            this.serverSocket = serverSocket;
            this.buffer = new byte[1024];
            this.jtaTextAreaMessage = jtaTextAreaMessage;
        }


        public void run(){

            while(true){

                try{
                    DatagramPacket receivePacket = new DatagramPacket(this.buffer, this.buffer.length);
                    if(!this.server.clientHasConnected())
                        this.jtaTextAreaMessage.append("Waiting for the client to connect to the chat...\n");
                    this.serverSocket.receive(receivePacket);

                    if (!this.server.clientHasConnected()) {
                        this.server.setClientIP(receivePacket.getAddress());
                        this.server.setClientPort(receivePacket.getPort());
                        this.server.setClientHasConnected(true);
                        this.jtaTextAreaMessage.append("Client has connected to chat!\n");
                    }

                    String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    jtaTextAreaMessage.append(receivedString + "\n");
                }
                catch (Exception exception){
                    jtaTextAreaMessage.append("Receive message from server error!\n");
                }

            }
        }

    }


