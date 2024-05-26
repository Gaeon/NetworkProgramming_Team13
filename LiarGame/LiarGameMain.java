package LiarGame;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LiarGameMain {
    private MqttClient client;
    private final String topic1 = "control";
    private final String topic2 = "game";
    private String broker = "tcp://localhost:1883";
    private String client_id;
    private int host_flag = 0;
    private List<JPanel> roomPanels = new ArrayList<>();
    private JPanel roomsPanel;
    private JFrame gameFrame;
    private MqttHandler mqttHandler = new MqttHandler();
    private static final Logger LOGGER = Logger.getLogger(LiarGameMain.class.getName());

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
                client_id = idField.getText() + "_" + System.currentTimeMillis();
                if (!client_id.isEmpty()) {
                    try {
                        client = new MqttClient(broker, client_id,new MemoryPersistence());
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                //LOGGER.log(Level.SEVERE, "Connection lost: " + cause.getMessage());
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

                        client.subscribe(topic1);

                        JOptionPane.showMessageDialog(frame, "Connected to broker successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        createAndShowGameGUI();
                        frame.dispose();
                    } catch (MqttException ex) {
                        LOGGER.log(Level.SEVERE, "Failed to connect to broker", ex);
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

        JButton exitButton = new JButton("종료하기");
        topPanel.add(exitButton);

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
                System.exit(0);
            }
        });

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
        roomsPanel.setLayout(new GridLayout(0, 1));
        gameFrame.add(new JScrollPane(roomsPanel), BorderLayout.CENTER);

        gameFrame.setVisible(true);
    }

    private void handleIncomingMessage(String message) {
        Gson gson = new Gson();
        JsonObject base = gson.fromJson(message, JsonObject.class);

        // "type" 필드에서 메시지 타입을 가져옴
        int messageType = base.get("type").getAsInt();

        // 메시지 타입에 따라 처리
        switch (messageType) {
            case Constant.C_GAMEROOMMAKE:
                // "sender"와 "roomId" 필드에서 값을 추출하여 메서드에 전달
                createRoomPanel(base.get("sender").getAsString(), base.get("roomId").getAsString());
                break;
            case Constant.C_GAMEROOMENTER:
                // 다른 처리
                break;
        }
    }



    private void createRoomPanel(String sender, String roomId) {
        // Create a panel for the room
        JPanel roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout());

        // Create a titled border for the room panel
        TitledBorder titledBorder = BorderFactory.createTitledBorder("방 번호" + roomId);
        roomPanel.setBorder(titledBorder);

        // Label to display the host of the room
        JLabel roomLabel = new JLabel("Host 이름 :" + sender);
        roomPanel.add(roomLabel, BorderLayout.CENTER);

        // Button to join the room
        JButton joinButton = new JButton("참가하기");
        roomPanel.add(joinButton, BorderLayout.SOUTH);

        // ActionListener for the join button
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform action when join button is clicked
                // For example, sending a request to join the room
                Data.C_Base base = new Data.C_Base(Constant.C_GAMEROOMENTER, client_id, "server", System.currentTimeMillis(), roomId);
                Data.C_gameroomenter gameroomenter = new Data.C_gameroomenter(base);
                Gson gson = new Gson();
                String message = gson.toJson(gameroomenter);
                send(topic1, message);
            }
        });

        // Add the room panel to the roomsPanel
        roomsPanel.add(roomPanel);
        // Revalidate and repaint the roomsPanel to reflect the changes
        roomsPanel.revalidate();
        roomsPanel.repaint();
    }



    private void reconnect() {
        while (!client.isConnected()) {
            LOGGER.log(Level.INFO, "Reconnecting...");
            try {
                client.reconnect();
                LOGGER.log(Level.INFO, "Reconnected successfully.");
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Reconnect failed. Retrying...", e);
                return;
            }
        }
    }

    public boolean send(String topic, String msg) {
        try {
            if (!client.isConnected()) {
                LOGGER.log(Level.INFO, "Client is not connected. Reconnecting...");
                reconnect();
            }
            if (client.isConnected()) {
                MqttMessage message = new MqttMessage();
                message.setPayload(msg.getBytes());
                client.publish(topic, message);
            } else {
                LOGGER.log(Level.SEVERE, "Failed to reconnect.");
                return false;
            }
        } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Failed to publish message", e);
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
                LOGGER.log(Level.SEVERE, "Failed to close MQTT client", e);
            }
        }
    }
}