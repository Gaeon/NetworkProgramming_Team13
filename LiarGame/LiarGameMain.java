package LiarGame;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LiarGameMain {
    private LiarGameMain liarGameMain = this;//
    private MqttClient client;
    private final String topic1 = "control";
    private final String topic2 = "game";
    private String broker = "tcp://localhost:1883";
    private String client_id;
    private int host_flag = 0;//0은 아무것도 안한 상태,1은 방장,2는 참가자
    private int count = 0;
    private boolean subscribed_flag1 = false;
    private boolean subscribed_flag2 = false;
    private List<RoomPanel> roomPanels = new ArrayList<>();
    private JPanel roomsPanel;
    private JFrame gameFrame;

    private static final Logger LOGGER = Logger.getLogger(LiarGameMain.class.getName());
    private GameRoom gameroom;
    private String enter_room = null;
    private Set<GameWindow> activeGameWindows = new HashSet<>();
    private Map<String, Integer> voteCount = new HashMap<>();
    private String liar = null;
    private boolean isLiar = false;

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
                client_id = idField.getText()+System.currentTimeMillis();
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
                                if (topic.equals(topic1)){
                                    handleIncomingMessage(payload);
                                } else if (topic.equals(topic2)){
                                    handleIncomingGMessage(payload);
                                } else {
                                    System.out.println("Unknown topic: " + topic);
                                }
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
                        Data.C_gameroommake m = new Data.C_gameroommake(new Data.C_Base(Constant.C_GAMEROOMMAKE, client_id, "all", System.currentTimeMillis(), client_id),selectedTopic);
                        Gson gson = new Gson();
                        String message = gson.toJson(m);
                        send(topic1, message);
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
            String receiver = base.receiver();
            System.out.println(message);
            switch (base.type()) {
                case Constant.C_REFRESH:
                    if (receiver.equals(client_id)) {
                        createRoomPanel(base.sender(), base.roomId(), msg.get("participants_num").getAsInt());
                    } else if (receiver.equals("host")) {
                        if (host_flag == 1) {
                            Data.C_refresh m_r = new Data.C_refresh(new Data.C_Base(Constant.C_REFRESH, client_id, base.sender(), System.currentTimeMillis(), client_id), gameroom.getParticipants_num());
                            String message_r = gson.toJson(m_r);
                            send(topic1, message_r);
                        }
                    }
                    break;
                case Constant.C_GAMEROOMMAKE:
                    if (base.sender().equals(client_id)) {
                        if (host_flag == 1) {
                            System.out.println("방을 이미 생성했습니다.");
                        } else {
                            createRoomPanel(base.sender(), base.roomId(), 1);
                            host_flag = 1;
                            gameroom = new GameRoom(client_id, client_id, msg.get("GameTopic").getAsString(),liarGameMain, host_flag);
                            gameroom.addParticipant(client_id);
                        }
                    } else {
                        createRoomPanel(base.sender(), base.roomId(), 1);
                    }
                    break;
                case Constant.C_GAMEROOMCANCEL:
                    // 게임 방 취소 처리 로직
                    System.out.println("삭제1");
                    if(base.receiver().equals(enter_room))
                    {
                        gameroom.cancelRoom();
                        System.out.println("삭제2");
                    }
                    System.out.println("삭제3");
//                    deleteRoomPanel(base.receiver());
                    // 방 삭제 메시지를 받으면 로컬에서 방 목록을 갱신
                    deleteRoomPanel(base.roomId());
                    break;
                case Constant.C_GAMEROOMEXIT:
                    if (base.receiver().equals(client_id)) {
                        gameroom.deleteParticipant(base.sender());
                        Data.C_gameroomInfo m_r1 = new Data.C_gameroomInfo(new Data.C_Base(Constant.C_GAMEROOMINFO, client_id, client_id, System.currentTimeMillis(), client_id), 2, base.sender(),gameroom.getParticipants_num());
                        String message_r1 = gson.toJson(m_r1);
                        send(topic1, message_r1);
                    }
                    // 게임 방 나가기 처리 로직
                    break;
                case Constant.C_GAMEROOMSTART:
                    // 게임 방 시작 처리 로직
                    if (base.roomId().equals(gameroom.getRoomId())) {
                        client.subscribe(topic2);
                        subscribed_flag2 = true;
                        gameroom.closeRoom();

                        if (host_flag == 1) {
                            List<String> participants_name = new ArrayList<>();
                            for (var participantElement : msg.get("participants_name").getAsJsonArray()) {
                                participants_name.add(participantElement.getAsString());
                            }
                            String game_topic = msg.get("GameTopic").toString();
                            Random random = new Random();
                            liar = participants_name.get(random.nextInt(participants_name.size()));
                            String word = GameTopic.getRandomWord(game_topic);

                            GData.G_GameSetting settings = new GData.G_GameSetting(new GData.G_Base(client_id, Constant.G_GAMESETTING, "host", "all", System.currentTimeMillis()), liar, word, false);
                            gson = new Gson();
                            message = gson.toJson(settings);
                            liarGameMain.send(getTopic2(), message);
                        }
                        startGameWindow(msg.get("participants_name").getAsJsonArray(), base.roomId(), msg.get("GameTopic").getAsString());
                    }
                    break;
                case Constant.C_GAMEROOMENTER:
                    if (base.receiver().equals(client_id)) {
                        gameroom.addParticipant(base.sender());
                        Data.C_gameroomInfo m_r1 = new Data.C_gameroomInfo(new Data.C_Base(Constant.C_GAMEROOMINFO, client_id, client_id, System.currentTimeMillis(), client_id), 1, base.sender(),gameroom.getParticipants_num());
                        String message_r1 = gson.toJson(m_r1);
                        send(topic1, message_r1);
                        Data.C_gameroomenter_confirm m_r2= new Data.C_gameroomenter_confirm(new Data.C_Base(Constant.C_GAMEROOMENTER_CONFIRM, client_id, base.sender(), System.currentTimeMillis(), client_id), gameroom.getAllParticipants(), gameroom.getGame_topic());
                        String message_r2 = gson.toJson(m_r2);
                        send(topic1, message_r2);
                    }
                    break;
                case Constant.C_GAMEROOMINFO:
                    // 게임 방 정보 처리 로직
                    if (base.receiver().equals(enter_room)) {
                        if (msg.get("infotype").getAsInt() == 1) {
                            gameroom.addParticipant(msg.get("participant").getAsString());
                        } else if (msg.get("infotype").getAsInt() == 2) {
                            gameroom.deleteParticipant(msg.get("participant").getAsString());
                        }
                    }
                    for (RoomPanel panel : roomPanels) {
                        if (panel.gethost().equals(base.sender())) {
                            panel.setParticipants_num(msg.get("participants_num").getAsInt());
                            panel.refreshPanel();
                            break;
                        }
                    }
                    break;
                case Constant.C_GAMEROOMENTER_CONFIRM:
                    if (receiver.equals(client_id)) {
                        host_flag = 2;
                        enter_room = base.sender();
                        gameroom = new GameRoom(base.sender(), base.sender(), msg.get("GameTopic").getAsString(),liarGameMain, host_flag);

                        // participants_name JSON 배열을 가져와서 gameroom에 추가
                        for (var participantElement : msg.get("participants_name").getAsJsonArray()) {
                            gameroom.addParticipant(participantElement.getAsString());
                        }
                    }
                    break;

                default:
                    System.out.println("잘못된 메시지 타입: " + base.type());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to handle incoming message: " + message, e);
        }
    }

    private void handleIncomingGMessage(String message) {
        Gson gson = new Gson();
        try {
            JsonObject msg = gson.fromJson(message, JsonObject.class);
            JsonObject baseJson = msg.get("base").getAsJsonObject();
            GData.G_Base base = fromJsonToG_Base(baseJson);
            String receiver = base.receiver();
            String sender = base.sender();
            System.out.println(message);
            switch (base.type()) {
                case Constant.G_GAMESETTING:
                    if (base.id().equals(gameroom.getRoomId())){
                        if (host_flag == 1 && receiver.equals("host")) {
                            if (msg.get("status").getAsBoolean()) {
                                count++;
                                System.out.println(count);
                                if (count == gameroom.getParticipants_num()) {
                                    GData.G_GameStart gamestart = new GData.G_GameStart(new GData.G_Base(client_id, Constant.G_GAMESTART, client_id, "all", System.currentTimeMillis()));
                                    String gamestartmsg = gson.toJson(gamestart);
                                    send(getTopic2(), gamestartmsg);
                                    count = 0;
                                }
                            }
                        }
                        if (sender.equals("host")) {
                            String liar = msg.get("liar").getAsString();
                            String keyword = null;
                            if (liar.equals(client_id)) {
                                isLiar = true;
                            } else {
                                keyword = msg.get("keyword").getAsString();
                            }
                            for (GameWindow window : activeGameWindows) {
                                if (window.getRoomId().equals(base.id())) {
                                    window.updateRoleAndKeyword(isLiar, keyword);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case Constant.G_GAMESTART:
                    if (base.id().equals(gameroom.getRoomId())){
                        for (GameWindow window : activeGameWindows) {
                            if (window.getRoomId().equals(base.id())) {
                                window.chatWindow(); // 채팅 GUI 활성화
                                if (host_flag == 1) {
                                    GData.G_FirstOpinion firstOpinion = new GData.G_FirstOpinion(new GData.G_Base(client_id, Constant.G_FIRSTOPINION, "host", "host", System.currentTimeMillis()), "0");
                                    String firstopinionmsg = gson.toJson(firstOpinion);
                                    send(getTopic2(), firstopinionmsg);
                                    break;
                                }
                                break;
                            }
                        }
                    }
                    break;
                case Constant.G_FIRSTOPINION:
                    if (base.id().equals(gameroom.getRoomId())) {
                        if (receiver.equals("host")) {
                            if (host_flag == 1 && sender.equals("host")) {
                                List<String> participants = gameroom.getAllParticipants();

                                GameServer gameServer = new GameServer(client_id, "game", participants, liarGameMain);
                                gameServer.FirstOpinionTimer();
                            }
                        } else {
                            if (base.sender().equals("host")) {
                                if (receiver.equals(client_id)) {
                                    for (GameWindow window : activeGameWindows) {
                                        if (window.getRoomId().equals(base.id())) {
                                            window.activateChatField();
                                            window.receiveOpinionMessage("HOST", "키워드에 대한 설명을 진행해주세요");
                                            break;
                                        }
                                    }
                                } else {
                                    for (GameWindow window : activeGameWindows) {
                                        if (window.getRoomId().equals(base.id())) {
                                            window.deactivateChatField();
                                            break;
                                        }
                                    }
                                }
                            } else if (!base.sender().equals(client_id)) {
                                for (GameWindow window : activeGameWindows) {
                                    if (window.getRoomId().equals(base.id())) {
                                        window.receiveOpinionMessage(base.sender(), msg.get("message").toString());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case Constant.G_CHAT:
                    if (base.id().equals(gameroom.getRoomId())){
                        if (base.sender().equals("host")) {
                            if (host_flag == 1) {
                                GameServer gameServer = new GameServer(client_id, "game", null, liarGameMain);
                                gameServer.ChatTimer();
                            }
                            for (GameWindow window : activeGameWindows) {
                                if (window.getRoomId().equals(base.id())) {
                                    window.startChat();
                                    window.receiveChatMessage("HOST", "180초동안 자유롭게 토론을 진행해주세요");
                                    window.activateChatField();
                                    break;
                                }
                            }
                        }
                        if (!base.sender().equals("host") && !base.sender().equals(client_id)) {
                            for (GameWindow window : activeGameWindows) {
                                if (window.getRoomId().equals(base.id())) {
                                    window.startChat();
                                    window.receiveChatMessage(base.sender(), msg.get("chat").toString());
                                    window.activateChatField();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case Constant.G_VOTE:
                    if (base.id().equals(gameroom.getRoomId())) {
                        if (host_flag == 1 && receiver.equals("host")) {
                            List<String> participants = gameroom.getAllParticipants();
                            if (sender.equals("host")) {
                                GData.G_Vote voteStartMessage = new GData.G_Vote(new GData.G_Base(client_id, Constant.G_VOTE, "host", "all", System.currentTimeMillis()), participants, null);
                                String voteStartMsg = gson.toJson(voteStartMessage);
                                liarGameMain.send(liarGameMain.getTopic2(), voteStartMsg);
                            } else {
                                String votedPlayer = msg.get("votedLiar").getAsString();
                                voteCount.put(votedPlayer, voteCount.getOrDefault(votedPlayer, 0) + 1);
                                count++;
                                if (count == gameroom.getParticipants_num()) {
                                    String mostVotedPlayer = Collections.max(voteCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                                    GData.G_Result voteResultMessage = new GData.G_Result(new GData.G_Base(client_id, Constant.G_RESULT, "host", "all", System.currentTimeMillis()), mostVotedPlayer, liar);
                                    String voteResultMsg = gson.toJson(voteResultMessage);
                                    send(topic2, voteResultMsg);
                                }
                            }
                        } else if (base.sender().equals("host")) {
                            for (GameWindow window : activeGameWindows) {
                                if (window.getRoomId().equals(base.id())) {
                                    window.showVoteDialog(gameroom.getAllParticipants()); // 투표 창을 열어서 participants 정보 전달
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case Constant.G_RESULT:
                    if (base.id().equals(gameroom.getRoomId())) {
                        String votedLiar = msg.get("votedLiar").toString();
                        String liar = msg.get("liar").toString();
                        boolean isLiarCorrect = votedLiar.equals(liar);

                        for (GameWindow window : activeGameWindows) {
                            if (window.getRoomId().equals(base.id())) {
                                window.showResultDialog(isLiarCorrect, votedLiar, liar, host_flag);
                                break;
                            }
                        }
                        // 게임방 삭제 메시지 방송
                        broadcastRoomDeletion(base.id().toString());
                    }
                    break;
                default:
                    System.out.println("잘못된 메시지 타입: " + base.type());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to handle incoming message: " + message, e);
        }
    }

    private void startGameWindow(JsonArray participants, String roomId, String topic) {
        List<String> participantsList = new ArrayList<>();
        for (JsonElement participantElement : participants) {
            participantsList.add(participantElement.getAsString());
        }
        GameWindow gameWindow = new GameWindow(participantsList, roomId, topic, client_id, this);
        activeGameWindows.add(gameWindow);
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

    private void deleteRoomPanel(String roomID) {
        RoomPanel panelToRemove = null;
        for (RoomPanel panel : roomPanels) {
            if (panel.gethost().equals(roomID)) {
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

    public String getTopic2() {return topic2;}

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
            if(host_flag ==0) {
                Data.C_Base base = new Data.C_Base(Constant.C_GAMEROOMENTER, client_id, host, System.currentTimeMillis(), roomId);
                Data.C_gameroomenter gameroomenter = new Data.C_gameroomenter(base);
                Gson gson = new Gson();
                String message = gson.toJson(gameroomenter);
                send(topic1, message);
            }
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

        public void refreshPanel() {
            JLabel numLabel = (JLabel) this.getComponent(1); // 참가 인원 라벨이 두 번째 컴포넌트라고 가정
            numLabel.setText("참가자인원 8/" + participants_num);
            this.revalidate();
            this.repaint();
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

    private GData.G_Base fromJsonToG_Base(JsonObject baseJson) {
        String m_id = baseJson.get("id").getAsString();
        int m_Type = baseJson.get("type").getAsInt();
        String m_sender = baseJson.get("sender").getAsString();
        String m_receiver = baseJson.get("receiver").getAsString();
        long m_time = baseJson.get("time").getAsLong();
        return new GData.G_Base(m_id, m_Type, m_sender, m_receiver, m_time);
    }
    public void setEnterRoom(String enter){
        this.enter_room = enter;
    }
    public void setHostFlag(int flag){
        this.host_flag = flag;
    }
    public MqttClient getClient() {
        return client;
    }
    private void broadcastRoomDeletion(String roomId) {
        // 모든 클라이언트에게 방 삭제 메시지 전송
        Data.C_gameroomcancel cancelMessage = new Data.C_gameroomcancel(new Data.C_Base(Constant.C_GAMEROOMCANCEL, client_id, "all", System.currentTimeMillis(), roomId));
        Gson gson = new Gson();
        String message = gson.toJson(cancelMessage);
        send(topic1, message); // 모든 클라이언트가 이 topic을 구독하고 있어야 합니다.
    }}