package LiarGame;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LiarGameMain {
    // MQTT client declaration
    private MqttClient client;
    final String topic1 = "control";
    final String topic2 = "game";
    String broker = "tcp://localhost:1883";
    String client_id;
    int host_flag = 0;
    private List<JPanel> roomPanels = new ArrayList<>(); // List to manage room panels
    private JPanel roomsPanel; // Panel to hold all the room panels
    private JFrame gameFrame;

    public LiarGameMain() {
        createAndShowLoginGUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LiarGameMain();
            }
        });
    }

    private void createAndShowLoginGUI() {
        JFrame frame = new JFrame("Liar Game 로그인창");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(null);

        JLabel label = new JLabel("ID : ");
        label.setBounds(10, 10, 80, 25);
        frame.add(label);

        JTextField idField = new JTextField();
        idField.setBounds(100, 10, 165, 25);
        frame.add(idField);

        JButton submitButton = new JButton("로그인");
        submitButton.setBounds(10, 50, 100, 25);
        frame.add(submitButton);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client_id = idField.getText();
                if (!client_id.isEmpty()) {
                    try {
                        // Initialize and connect the MQTT client
                        client = new MqttClient(broker, client_id);

                        // Set the callback for the client
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                System.out.println("Connection lost: " + cause.getMessage());
                                reconnect();
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                String payload = new String(message.getPayload());
                                handleIncomingMessage(payload);
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                            }
                        });

                        client.connect();

                        // Subscribe to topic1
                        client.subscribe(topic1);

                        // Show success dialog
                        JOptionPane.showMessageDialog(frame, "Connected to broker successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Create and show the new window
                        createAndShowGameGUI();

                        // Close the login frame
                        frame.dispose();

                    } catch (MqttException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Failed to connect to broker", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    private void createAndShowGameGUI() {
        gameFrame = new JFrame("Liar Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(600, 400);
        gameFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 3));

        // 종료하기 버튼 추가
        JButton exitButton = new JButton("종료하기");
        topPanel.add(exitButton);

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
                System.exit(0);
            }
        });

        // 방만들기 버튼 추가
        JButton createRoomButton = new JButton("방만들기");
        topPanel.add(createRoomButton);

        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.C_Base base = new Data.C_Base(Constant.C_GAMEROOMMAKE, client_id, "all", System.currentTimeMillis(), client_id);
                Gson gson = new Gson();
                String message = gson.toJson(base);
                send(topic1, message);
            }
        });

        // 새로고침 버튼 추가
        JButton refreshButton = new JButton("새로고침");
        topPanel.add(refreshButton);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.C_Base base = new Data.C_Base(Constant.C_REFRESH, client_id, "server", System.currentTimeMillis(), client_id);
                Data.C_refresh refresh = new Data.C_refresh(base);
                Gson gson = new Gson();
                String message = gson.toJson(refresh);
                send(topic1, message);
            }
        });

        gameFrame.add(topPanel, BorderLayout.NORTH);

        roomsPanel = new JPanel();
        roomsPanel.setLayout(new GridLayout(0, 1)); // Dynamic rows, 1 column
        gameFrame.add(new JScrollPane(roomsPanel), BorderLayout.CENTER);

        gameFrame.setVisible(true);
    }

    private void handleIncomingMessage(String message) {
        Gson gson = new Gson();
        Data.C_Base base = gson.fromJson(message, Data.C_Base.class);

        switch (base.type()) {
            case Constant.C_GAMEROOMMAKE:
                createRoomPanel(base.sender(), base.roomId());
                break;
            case Constant.C_GAMEROOMENTER:
                // Handle game room enter logic
                break;
            // handle other message types as needed
        }
    }

    private void createRoomPanel(String sender, String roomId) {
        JPanel roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout());

        JLabel roomLabel = new JLabel("Room ID: " + roomId + ", Host: " + sender);
        roomPanel.add(roomLabel, BorderLayout.CENTER);

        JButton joinButton = new JButton("참가하기");
        roomPanel.add(joinButton, BorderLayout.EAST);

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.C_Base base = new Data.C_Base(Constant.C_GAMEROOMENTER, client_id, "server", System.currentTimeMillis(), roomId);
                Data.C_gameroomenter gameroomenter = new Data.C_gameroomenter(base);
                Gson gson = new Gson();
                String message = gson.toJson(gameroomenter);
                send(topic1, message);
            }
        });

        roomPanels.add(roomPanel);
        roomsPanel.add(roomPanel);
        roomsPanel.revalidate();
        roomsPanel.repaint();
    }

    private void reconnect() {
        try {
            while (!client.isConnected()) {
                System.out.println("Reconnecting...");
                client.reconnect();
                Thread.sleep(2000); // Wait for 2 seconds before retrying
            }
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean send(String topic, String msg) {
        try {
            if (!client.isConnected()) {
                System.out.println("Client is not connected. Cannot send message.");
                reconnect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
