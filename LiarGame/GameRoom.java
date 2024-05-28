package LiarGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private JFrame roomFrame;
    private String roomId;
    private String host;
    private String game_topic;
    private List<String> participants_name = new ArrayList<>();
    private JPanel participantsPanel;
    private JLabel topicField;

    public GameRoom(String host, String roomId, String game_topic) {
        this.host = host;
        this.roomId = roomId;
        this.game_topic = game_topic;
        createAndShowRoomGUI();
    }

    private void createAndShowRoomGUI() {
        roomFrame = new JFrame("Game Room - " + roomId);
        roomFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        roomFrame.setSize(400, 300);
        roomFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        JLabel hostLabel = new JLabel("Host: " + host);
        topPanel.add(hostLabel);

        topicField = new JLabel("게임주제 : "+game_topic); // Show the selected topic
        topPanel.add(topicField);
        roomFrame.add(topPanel, BorderLayout.NORTH);

        participantsPanel = new JPanel(new GridLayout(8, 1)); // 8 rows, 1 column
        roomFrame.add(new JScrollPane(participantsPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton startButton = new JButton("Start Game");
        bottomPanel.add(startButton);

        JButton exitButton = new JButton("Exit Room");
        bottomPanel.add(exitButton);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitRoom();
            }
        });

        roomFrame.add(bottomPanel, BorderLayout.SOUTH);
        roomFrame.setVisible(true);
    }

    private void startGame() {
        String topic = topicField.getText();
        if (!topic.isEmpty()) {
            // 게임 시작 로직 구현
            JOptionPane.showMessageDialog(roomFrame, "Game started with topic: " + topic, "Game Started", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(roomFrame, "Please enter a topic before starting the game", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitRoom() {
        // 방 나가기 로직 구현
        roomFrame.dispose();
    }

    public void addParticipant(String participant) {
        if (participants_name.size() < 8) {
            participants_name.add(participant);
            updateParticipantsArea();
        } else {
            JOptionPane.showMessageDialog(roomFrame, "The room is full. Cannot add more participants.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateParticipantsArea() {
        participantsPanel.removeAll();
        for (int i = 0; i < participants_name.size(); i++) {
            String participant = participants_name.get(i);
            participantsPanel.add(new JLabel((i + 1) + ". " + participant));
        }
        participantsPanel.revalidate();
        participantsPanel.repaint();
    }

    public List<String> getAllParticipants() {
        return new ArrayList<>(participants_name);
    }
    public int getParticipants_num(){
        return participants_name.size();
    }
}
