package test;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;

public class MQTTClientTest {

    private static final String BROKER_URL = "tcp://localhost:1883"; // 브로커 URL 설정
    private static final int CLIENT_COUNT = 1000; // 테스트할 클라이언트 수

    public static void main(String[] args) {
        List<IMqttClient> clients = new ArrayList<>();

        try {
            for (int i = 0; i < CLIENT_COUNT; i++) {
                // 클라이언트 ID를 단순한 패턴으로 설정
                String clientId = "simpleClient-" + i;
                IMqttClient client = new MqttClient(BROKER_URL, clientId);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                client.connect(options);
                clients.add(client);
                System.out.println("Connected: " + clientId+"연결 수 :"+i);
            }
            System.out.println("All clients connected successfully.");

        } catch (MqttException e) {
            System.err.println("Error connecting clients: " + e.getMessage());
        } finally {
            // 모든 클라이언트를 연결 해제
            for (IMqttClient client : clients) {
                try {
                    if (client.isConnected()) {
                        client.disconnect();
                    }
                    client.close();
                } catch (MqttException e) {
                    System.err.println("Error disconnecting client: " + e.getMessage());
                }
            }
            System.out.println("All clients disconnected.");
        }
    }
}
