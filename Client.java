import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Client {
	// 기존 Client 클래스 변수들...
	private String userId;
	private boolean hostFlag;
	private boolean roomFlag;
	private String roomID;
	private String gameTopic;
	private boolean liar;
	private MqttClient mqttClient;
	String brokerUrl = "tcp://test.mosquitto.org:1883";
	private Host hostRoom;
	public Client(String userId) {
		this.userId = userId;
		try {
			// MQTT 클라이언트 초기화
			mqttClient = new MqttClient(brokerUrl, userId, null);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			mqttClient.connect(connOpts);
			// 콜백 설정
			mqttClient.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable cause) {
					System.out.println("Connection lost: " + cause.getMessage());
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String jsonMessage = new String(message.getPayload());
					Gson gson = new Gson();
					GamePayload.GameType payload = gson.fromJson(jsonMessage, GamePayload.GameType.class);

					switch (payload.getBase().getType()) {
						case "gamesetting":
							GamePayload.GameSettingPayload gameSettingPayload = gson.fromJson(jsonMessage, GamePayload.GameSettingPayload.class);
							handleGameSetting(gameSettingPayload);
							break;
						case "gamestart":
							GamePayload.GameStartPayload gameStartPayload = gson.fromJson(jsonMessage, GamePayload.GameStartPayload.class);
							handleGameStart(gameStartPayload);
							break;
						case "firstopinion":
							GamePayload.FirstOpinionPayload firstOpinionPayload = gson.fromJson(jsonMessage, GamePayload.FirstOpinionPayload.class);
							handleFirstOpinion(firstOpinionPayload);
							break;
						case "chat":
							GamePayload.ChatPayload chatPayload = gson.fromJson(jsonMessage, GamePayload.ChatPayload.class);
							handleChat(chatPayload);
							break;
						case "vote":
							GamePayload.VotePayload votePayload = gson.fromJson(jsonMessage, GamePayload.VotePayload.class);
							handleVote(votePayload);
							break;
						case "result":
							GamePayload.ResultPayload resultPayload = gson.fromJson(jsonMessage, GamePayload.ResultPayload.class);
							handleResult(resultPayload);
							break;
					}
				}

				// 게임 설정 처리
				private void handleGameSetting(GamePayload.GameSettingPayload payload) {
					gameTopic = payload.getKeyword();
					liar = payload.getLiar().equals(userId);
					if (liar)
						//내가 라이어임을 출력
						// 게임 설정을 받았으므로 준비 완료 메시지를 발행
						publish("game", new Gson().toJson(new GamePayload.GameType(new G_Base(1, "gamesetting", userId, "All", ""))));
				}

				// 게임 시작 처리
				private void handleGameStart(GamePayload.GameStartPayload payload) {
					// 게임이 시작됐으므로 게임에 관련된 UI를 업데이트
				}

				// 첫 번째 의견 처리
				private void handleFirstOpinion(GamePayload.FirstOpinionPayload payload) {
					// 첫 번째 의견을 받았으므로 UI 업데이트
				}

				// 채팅 처리
				private void handleChat(GamePayload.ChatPayload payload) {
					// 채팅 메시지를 받았으므로 UI 업데이트
				}

				// 투표 처리
				private void handleVote(GamePayload.VotePayload payload) {
					// 투표 결과를 받았으므로 UI 업데이트
				}

				// 결과 처리
				private void handleResult(GamePayload.ResultPayload payload) {
					// 결과를 받았으므로 UI 업데이트
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// 메시지 전송이 완료되었을 때의 처리
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private class Host {
		private boolean gameStartFlag = false;
		private List<String> gameTopics = new ArrayList<>();
		private List<String> userList = new ArrayList<>();
		private int gameTime = 180;
		private String whoIsLiar;
		private String gameTopic;

		public Host(List<String> userList, String gameTopic) {
			this.userList = userList;
			this.gameTopic = gameTopic;
		}
	}

	// 메시지를 발행하는 메소드
	public void publish(String topic, String content) {
		try {
			MqttMessage message = new MqttMessage(content.getBytes());
			mqttClient.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	// 특정 토픽을 구독하는 메소드
	public void subscribe(String topic) {
		try {
			mqttClient.subscribe(topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() throws MqttException {
		if (mqttClient != null) {
			mqttClient.disconnect();
		}
	}
}