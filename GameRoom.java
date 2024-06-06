package LiarGame;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameRoom {
    private JFrame roomFrame;
    private String roomId;
    private String host;
    private String game_topic;
    private LiarGameMain liarGameMain;
    private int host_flag;
    private List<String> participants_name = new ArrayList<>();
    private JPanel participantsPanel;
    private JLabel topicField;

    public GameRoom(String host, String roomId, String game_topic,LiarGameMain liarGameMain, int host_flag) {
        this.host = host;
        this.roomId = roomId;
        this.game_topic = game_topic;
        this.liarGameMain = liarGameMain;
        this.host_flag = host_flag;
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
        JButton startButton = new JButton("게임시작");
        bottomPanel.add(startButton);

        JButton exitButton = new JButton("게임나가기");
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

    public void startGame() {
        if (participants_name.size() >= Constant.MIN_PARTICIPANTS && participants_name.size() <= Constant.MAX_PARTICIPANTS) {
            Data.C_gameroomstart startMessage = new Data.C_gameroomstart(
                    new Data.C_Base(Constant.C_GAMEROOMSTART, liarGameMain.getClientId(), "all", System.currentTimeMillis(), roomId),
                    participants_name, game_topic);
            Gson gson = new Gson();
            String message = gson.toJson(startMessage);
            liarGameMain.send(liarGameMain.getTopic1(), message);
        } else {
            JOptionPane.showMessageDialog(roomFrame, "플레이어가 부족합니다.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void closeRoom() {
        if (roomFrame != null) {
            roomFrame.dispose();
            roomFrame = null;
        }
    }

    private void exitRoom() {
        // 방 나가기 로직 구현
        Gson gson = new Gson();
        System.out.println(host_flag);
        if(host_flag == 1) {
            System.out.println("나가기2");
            Data.C_gameroomcancel m = new Data.C_gameroomcancel(new Data.C_Base(Constant.C_GAMEROOMCANCEL, liarGameMain.getClientId(), host, System.currentTimeMillis(), this.roomId));
            String message = gson.toJson(m);
            liarGameMain.send(liarGameMain.getTopic1(),message);
        }
        else if(host_flag == 2) {
            System.out.println("나가기3");
            Data.C_gameroomexit m = new Data.C_gameroomexit(new Data.C_Base(Constant.C_GAMEROOMEXIT, liarGameMain.getClientId(), host, System.currentTimeMillis(), this.roomId));
            String message = gson.toJson(m);
            liarGameMain.send(liarGameMain.getTopic1(),message);
        }
        roomFrame.dispose();
        liarGameMain.setEnterRoom(null);
        liarGameMain.setHostFlag(0);
    }
    public void cancelRoom(){
        roomFrame.dispose();
        liarGameMain.setEnterRoom(null);
        liarGameMain.setHostFlag(0);
    }

    public void addParticipant(String participant) {
        if (participants_name.size() < 8) {
            participants_name.add(participant);
            updateParticipantsArea();
        } else {
            JOptionPane.showMessageDialog(roomFrame, "The room is full. Cannot add more participants.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void deleteParticipant(String participant) {
        participants_name.remove(participant);
        updateParticipantsArea();
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
    public String getGame_topic(){
        return this.game_topic;
    }
    public String getRoomId(){
        return this.roomId;
    }
}
