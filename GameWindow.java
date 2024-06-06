package LiarGame;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;


public class GameWindow {
    private List<String> participants;
    private String roomId;
    private String topic;
    private String clientId;
    private LiarGameMain liarGameMain;
    private JFrame gameFrame;
    private JTextArea chatArea;
    private JTextField chatInput;
    private boolean isLiar;
    private String keyword;
    private boolean roleWindowShown = false;
    private boolean chatWindowShown = false;
    private boolean isVoteDialogOpen = false;
    private JButton sendButton;

    public GameWindow(List<String> participants, String roomId, String topic, String clientId, LiarGameMain liarGameMain) {
        this.participants = participants;
        this.roomId = roomId;
        this.topic = topic;
        this.clientId = clientId;
        this.liarGameMain = liarGameMain;
        createGameGUI();
    }

    private void createGameGUI() {
        gameFrame = new JFrame("Liar Game - Room: " + roomId + " Client: " + clientId);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setSize(500, 400);
        gameFrame.setLayout(new BorderLayout());
    }

    public void showRoleWindow() {
        JFrame roleFrame = new JFrame("Role : " + clientId);
        roleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        roleFrame.setSize(300, 200);
        roleFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel roleLabel = new JLabel("역할: " + (isLiar ? "거짓말쟁이" : "정직한 플레이어"));
        JLabel keywordLabel = new JLabel("단어: " + (isLiar ? "비밀" : keyword));

        panel.add(roleLabel);
        panel.add(keywordLabel);

        JButton confirmButton = new JButton("확인");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendStatusMessage();
                roleFrame.dispose();  // 역할 창 닫기
                roleWindowShown = true;
            }
        });

        panel.add(confirmButton);

        roleFrame.add(panel, BorderLayout.CENTER);
        roleFrame.setVisible(true);
    }

    public void updateRoleAndKeyword(boolean isLiar, String keyword) {
        this.isLiar = isLiar;
        this.keyword = keyword;
        if (!roleWindowShown) {
            showRoleWindow();  // 역할 창을 한 번만 띄움
        }
    }

    public void chatWindow() {
        if (chatWindowShown) return;

        gameFrame.getContentPane().removeAll(); // 기존 GUI 요소 제거

        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        JLabel roomLabel = new JLabel("Room: " + roomId);
        JLabel topicLabel = new JLabel("Topic: " + topic);
        JLabel infoLabel = new JLabel("Word: " + (isLiar ? "im liar" : keyword));

        topPanel.add(roomLabel);
        topPanel.add(topicLabel);
        topPanel.add(infoLabel);

        gameFrame.add(topPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        gameFrame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        sendButton = new JButton("Send"); // sendButton 초기화

        sendButton.addActionListener(e -> sendFirstOpinionMessage());

        bottomPanel.add(chatInput, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // 채팅 입력 필드와 전송 버튼을 비활성화
        chatInput.setEnabled(false);
        sendButton.setEnabled(false);

        gameFrame.add(bottomPanel, BorderLayout.SOUTH);

        gameFrame.revalidate();
        gameFrame.repaint();
        gameFrame.setVisible(true);
        chatWindowShown = true;
    }

    // 채팅 시작 시 호출되는 메서드
    public void startChat() {
        sendButton.addActionListener(e -> sendChatMessage()); // sendChatMessage()로 설정
        chatInput.setEnabled(true);
        sendButton.setEnabled(true);
    }

    // 내 차례가 되었을 때 호출되는 메서드
    public void activateChatField() {
        if (chatWindowShown && sendButton != null) {
            chatInput.setEnabled(true);
            sendButton.setEnabled(true);
        }
    }

    // 내 차례가 끝났을 때 호출되는 메서드
    public void deactivateChatField() {
        if (chatWindowShown && sendButton != null) {
            chatInput.setEnabled(false);
            sendButton.setEnabled(false);
        }
    }

    public String getRoomId() {
        return roomId;
    }

    private void sendStatusMessage() {
        Gson gson = new Gson();
        GData.G_GameSetting statusMessage = new GData.G_GameSetting(
                new GData.G_Base(roomId, Constant.G_GAMESETTING, clientId, "host", System.currentTimeMillis()),
                "0", "0", true
        );
        String message = gson.toJson(statusMessage);
        liarGameMain.send(liarGameMain.getTopic2(), message);
    }

    private void sendFirstOpinionMessage() {
        String message = chatInput.getText();
        if (!message.trim().isEmpty()) {
            GData.G_FirstOpinion chatMessage = new GData.G_FirstOpinion(
                    new GData.G_Base(roomId, Constant.G_FIRSTOPINION, clientId, "all", System.currentTimeMillis()),
                    message);
            Gson gson = new Gson();
            String chatJson = gson.toJson(chatMessage);
            liarGameMain.send(liarGameMain.getTopic2(), chatJson);
            chatArea.append("Me: " + message + "\n");
            chatInput.setText("");
        }
    }

    public void receiveOpinionMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
    }

    private void sendChatMessage() {
        String message = chatInput.getText();
        if (!message.trim().isEmpty()) {
            GData.G_Chat chatMessage = new GData.G_Chat(
                    new GData.G_Base(roomId, Constant.G_CHAT, clientId, "all", System.currentTimeMillis()),
                    message);
            Gson gson = new Gson();
            String chatJson = gson.toJson(chatMessage);
            liarGameMain.send(liarGameMain.getTopic2(), chatJson);
            chatArea.append("Me: " + message + "\n");
            chatInput.setText("");
        }
    }

    public void receiveChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
    }

    public void showVoteDialog(List<String> participants) {
        if (isVoteDialogOpen) {
            return; // 이미 투표 창이 열려 있는 경우 실행하지 않음
        }
        isVoteDialogOpen = true; // 투표 창이 열렸음을 표시

        JFrame voteFrame = new JFrame("Vote");
        voteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        voteFrame.setSize(300, 400);
        voteFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 245, 245)); // 배경색 설정
        panel.setLayout(new GridLayout(participants.size() + 1, 1, 5, 5)); // 그리드 레이아웃 간격 설정

        JLabel titleLabel = new JLabel("Vote for a player:");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel);

        ButtonGroup group = new ButtonGroup();

        // 각 참가자에 대한 라디오 버튼 생성
        for (String participant : participants) {
            JRadioButton radioButton = new JRadioButton(participant);
            radioButton.setBackground(new Color(255, 255, 255));
            radioButton.setFont(new Font("Arial", Font.PLAIN, 12));
            group.add(radioButton);
            panel.add(radioButton);
        }

        JButton confirmButton = new JButton("Vote");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 12));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(new Color(34, 139, 34)); // 버튼 색상 변경
        confirmButton.addActionListener(e -> {
            String selectedParticipant = null;
            for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();
                if (button.isSelected()) {
                    selectedParticipant = button.getText();
                    break;
                }
            }

            sendVoteMessage(selectedParticipant);

            isVoteDialogOpen = false;
            voteFrame.dispose();
        });
        panel.add(confirmButton);

        voteFrame.add(panel, BorderLayout.CENTER);
        voteFrame.setVisible(true);
    }

    private void sendVoteMessage(String selectedParticipant) {
        GData.G_Vote voteMessage = new GData.G_Vote(
                new GData.G_Base(roomId, Constant.G_VOTE, clientId, "host", System.currentTimeMillis()),
                null,
                selectedParticipant);
        Gson gson = new Gson();
        String voteJson = gson.toJson(voteMessage);
        liarGameMain.send(liarGameMain.getTopic2(), voteJson);
    }

    public void showResultDialog(boolean isLiarCorrect, String votedLiar, String actualLiar, int host_flag) {
        JFrame resultFrame = new JFrame("Result");
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(300, 200);
        resultFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        String resultMessage;
        if (isLiar && !isLiarCorrect || !isLiar && isLiarCorrect) {
            resultMessage = "승리했습니다!";
        } else {
            resultMessage = "오답입니다!";
        }
        JLabel resultLabel = new JLabel(resultMessage);
        JLabel votedLabel = new JLabel("가장많이 지목당한 플레이어: " + votedLiar);
        JLabel actualLabel = new JLabel("실제 라이어: " + actualLiar);

        panel.add(resultLabel);
        panel.add(votedLabel);
        panel.add(actualLabel);

        JButton confirmButton = new JButton("확인");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    liarGameMain.getClient().unsubscribe(liarGameMain.getTopic2());
                } catch (MqttException ex) {
                    throw new RuntimeException(ex);
                }
                resultFrame.dispose();
                gameFrame.dispose(); // 게임 창 닫기
            }
        });


        panel.add(confirmButton);

        resultFrame.add(panel, BorderLayout.CENTER);
        resultFrame.setVisible(true);
    }

    public void receiveVote(String selectedParticipant) {
    }

}
