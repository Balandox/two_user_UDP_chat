package org.suai.laba11.view;

import jdk.swing.interop.SwingInterOpUtils;
import org.suai.laba11.model.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;

public class ClientView extends JFrame {


    private Client client;

    private JTextField jtfMessage;
    private JTextField jtfName;
    private JTextArea jtaTextAreaMessage;

    public ClientView(Client client) {

        this.client = client;

        if(!client.tryConnect()){
            System.out.println("Connection fail!!!");
            System.exit(1);
        }

        setBounds(600, 300, 600, 500);
        setTitle("Client");
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
                if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                    client.setClientName(jtfName.getText());
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

        ListenerMessageThread listenerMessageThread = new ListenerMessageThread(this.client.getClientSocket(),this.jtaTextAreaMessage);
        listenerMessageThread.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                    byte[] buffer = null;
                    DatagramPacket sendPacket = null;
                    // здесь проверяем, что имя клиента непустое и не равно значению по умолчанию
                    if (!client.getClientName().isEmpty() && !Objects.equals(client.getClientName(), "Input your name: "))
                        buffer = (client.getClientName() + " has left from the chat!").getBytes();
                    else
                        buffer = "Client has left the chat room without introducing himself".getBytes();

                sendPacket = new DatagramPacket(buffer, buffer.length, client.getServerIp(), client.getServerPort());

                try {
                    client.getClientSocket().send(sendPacket);
                    client.getClientSocket().close();
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
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, client.getServerIp(), client.getServerPort());
        try {
            this.client.getClientSocket().send(sendPacket);
        }
        catch (IOException exception){
            System.out.println("Sending message error!");
        }
        jtaTextAreaMessage.append("Me: " + jtfMessage.getText() + "\n");
        jtfMessage.setText("");
    }

}

class ListenerMessageThread extends Thread{
    private final DatagramSocket clientSocket;
    private JTextArea jtaTextAreaMessage;
    private byte[] buffer;

    public ListenerMessageThread(DatagramSocket clientSocket, JTextArea jtaTextAreaMessage){
        this.clientSocket = clientSocket;
        this.buffer = new byte[1024];
        this.jtaTextAreaMessage = jtaTextAreaMessage;
    }


    public void run(){

        while(true){

            try{
                DatagramPacket receivePacket = new DatagramPacket(this.buffer, this.buffer.length);
                this.clientSocket.receive(receivePacket);
                String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                jtaTextAreaMessage.append(receivedString + "\n");
            }
            catch (Exception exception){
                jtaTextAreaMessage.append("Receive message from server error!\n");
            }

        }
    }

}
