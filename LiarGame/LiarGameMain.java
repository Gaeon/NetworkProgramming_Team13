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
    private boolean subscribed_flag1 = false;
    private boolean subscribed_flag2 = false;
    private List<RoomPanel> roomPanels = new ArrayList<>();
    private JPanel roomsPanel;
    private JFrame gameFrame;
    private static final Logger LOGGER = Logger.getLogger(LiarGameMain.class.getName());
    private GameRoom gameroom;
    private String enter_room = null;

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
                        client = new MqttClient(broker, client_id, new MemoryPersistence());
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                LOGGER.log(Level.SEVERE, "Connection lost: " + cause.getMessage());
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
                        subscribed_flag1 = true;

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
        topPanel.setLayout(new GridLayout(1, 4));

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
                if (host_flag != 1) {
                    String selectedTopic = (String) JOptionPane.showInputDialog(
                            gameFrame,
                            "주제를 선택하세요:",
                            "주제 선택",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            GameTopic.TOPICS,
                            GameTopic.TOPICS[0]
                    );

                    if (selectedTopic != null && !selectedTopic.isEmpty()) {
                        Data.C_gameroommake m = new Data.C_gameroommake(new Data.C_Base(Constant.C_GAMEROOMMAKE, client_id, "all", System.currentTimeMillis(), client_id));
                        Gson gson = new Gson();
                        String message = gson.toJson(m);
                        send(topic1, message);
                        gameroom = new GameRoom(client_id, client_id, selectedTopic);
                        gameroom.addParticipant(client_id);
                    }
                }
            }

        });

        JButton refreshButton = new JButton("새로고침");
        topPanel.add(refreshButton);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.C_refresh m = new Data.C_refresh(new Data.C_Base(Constant.C_REFRESH, client_id, "host", System.currentTimeMillis(), client_id),null);
                Gson gson = new Gson();
                String message = gson.toJson(m);
                send(topic1, message);
            }
        });
        topPanel.add(new JLabel("내 정보 : "+client_id));
        gameFrame.add(topPanel, BorderLayout.NORTH);

        roomsPanel = new JPanel();
        roomsPanel.setLayout(new GridLayout(0, 1));
        gameFrame.add(new JScrollPane(roomsPanel), BorderLayout.CENTER);
        gameFrame.setVisible(true);
    }

    private void handleIncomingMessage(String message) {
        Gson gson = new Gson();
        try {
            JsonObject msg = gson.fromJson(message, JsonObject.class);
            JsonObject baseJson = msg.get("base").getAsJsonObject();
            Data.C_Base base = fromJsonToC_Base(baseJson);
            //json에서 record class로 바꾸려할떄 mqtt와 충돌 발생 이유는 모름
            System.out.println(base.type());
            switch (base.type()) {
                case Constant.C_REFRESH:
                    String receiver = base.receiver();
                    if (receiver.equals(client_id)) {
                        createRoomPanel(base.sender(), base.roomId(),msg.get("participants_num").getAsInt());
                    } else if (receiver.equals("host")) {
                        if (host_flag == 1) {
                            Data.C_refresh m_r = new Data.C_refresh(new Data.C_Base(Constant.C_REFRESH, client_id, base.sender(), System.currentTimeMillis(), client_id), gameroom.getParticipants_num());
                            String message_r = gson.toJson(m_r);
                            System.out.println("받은 refresh에 대해 " + base.sender() + "에게 보냄");
                            send(topic1, message_r);
                        }
                    }
                    break;
                case Constant.C_GAMEROOMMAKE:
                    System.out.println("sender: " + base.sender() + " roomId: " + base.roomId());
                    if (base.sender().equals(client_id)) {
                        if (host_flag == 1) {
                            System.out.println("방을 이미 생성했습니다.");
                        } else {
                            createRoomPanel(base.sender(), base.roomId(),1);
                            host_flag = 1;
                        }
                    } else {
                        createRoomPanel(base.sender(), base.roomId(),1);
                    }
                    break;
                case Constant.C_GAMEROOMCANCLE:
                    // 게임 방 취소 처리 로직
                    break;
                case Constant.C_GAMEROOMEXIT:
                    // 게임 방 나가기 처리 로직
                    break;
                case Constant.C_GAMEROOMSTART:
                    // 게임 방 시작 처리 로직
                    break;
                case Constant.C_GAMEROOMENTER:
                    if(base.receiver().equals(client_id))
                    {
                        gameroom.addParticipant(base.sender());
                    }
                    break;
                case Constant.C_GAMEROOMINFO:
                    // 게임 방 정보 처리 로직
                    break;
                default:
                    System.out.println("잘못된 메시지 타입: " + base.type());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to handle incoming message: " + message, e);
        }
    }

    private void createRoomPanel(String sender, String roomId,int num) {
        if (!isRoomPanelExists(sender)) {
            RoomPanel roomPanel = new RoomPanel(sender, roomId, num);
            roomPanels.add(roomPanel);
            roomsPanel.add(roomPanel);
            roomsPanel.revalidate();
            roomsPanel.repaint();
        }
    }

    private void UpdateRoomPanel(String sender, String roomId,int num) {

    }

    private boolean isRoomPanelExists(String sender) {
        for (RoomPanel panel : roomPanels) {
            if (panel.gethost().equals(sender)) {
                return true;
            }
        }
        return false;
    }

    private void deleteRoomPanel(String sender, String roomId) {
        RoomPanel panelToRemove = null;
        for (RoomPanel panel : roomPanels) {
            if (panel.gethost().equals(sender)) {
                panelToRemove = panel;
                break;
            }
        }
        if (panelToRemove != null) {
            roomPanels.remove(panelToRemove);
            roomsPanel.remove(panelToRemove);
            roomsPanel.revalidate();
            roomsPanel.repaint();
        }
    }

    public String getClientId() {
        return client_id;
    }

    public String getTopic1() {
        return topic1;
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

    private void reconnect() {
        while (!client.isConnected()) {
            LOGGER.log(Level.INFO, "Reconnecting...");
            try {
                client.reconnect();
                if (subscribed_flag1) {
                    client.subscribe(topic1);
                }
                if (subscribed_flag2) {
                    client.subscribe(topic2);
                }
                LOGGER.log(Level.INFO, "Reconnected successfully.");
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Reconnect failed. Retrying...", e);
                return;
            }
        }
    }

    public void close() {
        if (client != null) {
            try {
                if (subscribed_flag1) {
                    client.unsubscribe(topic1);
                }
                if (subscribed_flag2) {
                    client.unsubscribe(topic2);
                }
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                LOGGER.log(Level.SEVERE, "Failed to close MQTT client", e);
            }
        }
    }

    // Inner RoomPanel class
    class RoomPanel extends JPanel {
        private String host;
        private String roomId;
        private LiarGameMain liarGameMain;
        private int participants_num;

        public RoomPanel(String sender, String roomId,int num) {
            this.host = sender;
            this.roomId = roomId;
            this.liarGameMain = liarGameMain;
            this.participants_num = num;
            initialize();
        }

        private void initialize() {
            this.setLayout(new BorderLayout());
            TitledBorder titledBorder = BorderFactory.createTitledBorder("방 번호: " + roomId);
            this.setBorder(titledBorder);

            JLabel roomLabel = new JLabel("Host 이름: " + host);
            this.add(roomLabel, BorderLayout.NORTH);
            JLabel numLabel = new JLabel("참가자인원 8/"+participants_num);
            this.add(numLabel,BorderLayout.CENTER);
            JButton joinButton = new JButton("참가하기");
            this.add(joinButton, BorderLayout.SOUTH);

            joinButton.addActionListener(e -> joinRoom());
        }

        private void joinRoom() {
            Data.C_Base base = new Data.C_Base(Constant.C_GAMEROOMENTER, client_id, host, System.currentTimeMillis(), roomId);
            Data.C_gameroomenter gameroomenter = new Data.C_gameroomenter(base);
            Gson gson = new Gson();
            String message = gson.toJson(gameroomenter);
            send(topic1, message);
        }

        public String gethost() {
            return host;
        }

        public String getRoomId() {
            return roomId;
        }
        public void setParticipants_num(int num){
            this.participants_num = num;
        }
    }

    private Data.C_Base fromJsonToC_Base(JsonObject baseJson) {
        int m_Type = baseJson.get("type").getAsInt();
        String m_sender = baseJson.get("sender").getAsString();
        String m_receiver = baseJson.get("receiver").getAsString();
        long m_time = baseJson.get("time").getAsLong();
        String m_roomId = baseJson.get("roomId").getAsString();
        return new Data.C_Base(m_Type, m_sender, m_receiver, m_time, m_roomId);
    }
}
