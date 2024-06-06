package LiarGame;
import com.google.gson.Gson;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameServer {
    private String id;
    private List<String> participants;
    private Gson gson;
    private String topic;
    private LiarGameMain liarGameMain;

    public GameServer(String id, String topic, List<String> participants, LiarGameMain liarGameMain) {
        this.id = id;
        this.topic = topic;
        this.participants = participants;
        this.liarGameMain = liarGameMain;
        this.gson = new Gson();
    }

    public void FirstOpinionTimer() {
        // 스케줄링 타이머 생성 및 시작
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SendFirstOpinionTask(), 0, Constant.T_FIRSTOPINION); // 180초마다 실행
    }

    private class SendFirstOpinionTask extends TimerTask {
        private int currentIndex = 0;

        @Override
        public void run() {
            if (currentIndex < participants.size()) {
                String participantClientId = participants.get(currentIndex);
                GData.G_FirstOpinion firstOpinion = new GData.G_FirstOpinion(
                        new GData.G_Base(id, Constant.G_FIRSTOPINION, "host", participantClientId, System.currentTimeMillis()), "0");
                String firstOpinionMsg = gson.toJson(firstOpinion);
                liarGameMain.send(liarGameMain.getTopic2(), firstOpinionMsg);

                currentIndex++;
            } else {
                cancel(); // 모든 참여자에게 메시지를 보냈으면 타이머 종료
                sendChatStartMessage(); // 모든 클라이언트가 단어 설명을 마쳤으므로 채팅 시작 메시지 전송
            }
        }
    }

    public void ChatTimer() {
        Timer timer = new Timer();
        timer.schedule(new SendVoteStartTask(), Constant.T_CHAT);
    }

    private class SendVoteStartTask extends TimerTask {
        @Override
        public void run() {
            sendVoteStartMessage();
        }
    }

    private void sendChatStartMessage() {
        GData.G_Chat chatStartMessage = new GData.G_Chat(new GData.G_Base(id, Constant.G_CHAT, "host", "all", System.currentTimeMillis()), "0");
        String chatStartMsg = gson.toJson(chatStartMessage);
        liarGameMain.send(liarGameMain.getTopic2(), chatStartMsg);
    }

    private void sendVoteStartMessage() {
        GData.G_Vote voteStartMessage = new GData.G_Vote(new GData.G_Base(id, Constant.G_VOTE, "host", "host", System.currentTimeMillis()), null, null);
        String voteStartMsg = gson.toJson(voteStartMessage);
        liarGameMain.send(liarGameMain.getTopic2(), voteStartMsg);
    }
}
