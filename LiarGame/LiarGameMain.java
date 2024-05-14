package LiarGame;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.swing.*;
import java.awt.*;

public class LiarGameMain{
    //mqttclient 선언
    private MqttClient client;
    public LiarGameMain()
    {
        this.client = new MqttClient();

        this.setTitle("라이어게임");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Initialize.initializeLoginInterface();
    }

    public static void main(String[] args) {

    }
    public boolean send(String topic, String msg){//메세지를 string을 보내는 함수
        try {
            //broker로 전송할 메세지 생성 -MqttMessage
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes()); //실제 broker로 전송할 메세지
            client.publish(topic,message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void close(){//mqttclient disconnect와 close를 진행
        if(client != null){
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}

