package LiarGame;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MqttHandler {
    private static final String LOG_TAG = "MqttHandler";
    private boolean isConnected = false;
    private boolean isTopicSubscribed = false;

    public IMqttActionListener getConnectListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println(LOG_TAG + ": CONNECTED TO BROKER");
                isConnected = true;
                // Add subscription calls here if needed
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println(LOG_TAG + ": CONNECTION TO BROKER FAILED");
                isConnected = false;
            }
        };
    }

    public IMqttActionListener getSubscribeListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                isTopicSubscribed = true;
                System.out.println(LOG_TAG + ": SUBSCRIPTION SUCCESSFUL");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                isTopicSubscribed = false;
                System.out.println(LOG_TAG + ": SUBSCRIPTION FAILED");
            }
        };
    }

    public IMqttActionListener getPublishListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println(LOG_TAG + ": PUBLISHED");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println(LOG_TAG + ": PUBLISH FAILED");
            }
        };
    }

    public IMqttActionListener getUnsubscribeListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                isTopicSubscribed = false;
                System.out.println(LOG_TAG + ": UNSUBSCRIPTION SUCCESSFUL");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                isTopicSubscribed = true;
                System.out.println(LOG_TAG + ": UNSUBSCRIPTION FAILED");
            }
        };
    }

    public IMqttActionListener getDisconnectListener() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println(LOG_TAG + ": DISCONNECTED");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println(LOG_TAG + ": DISCONNECTION FAILED");
            }
        };
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isTopicSubscribed() {
        return isTopicSubscribed;
    }
}
