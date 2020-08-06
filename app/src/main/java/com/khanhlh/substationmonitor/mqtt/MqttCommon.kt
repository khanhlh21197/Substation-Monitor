package com.khanhlh.substationmonitor.mqtt

import android.app.Activity
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID

class MqttCommon(
    activity: Activity?,
    subTopic: String?,
    receiveMessage: ReceiveMessage
) {
    private val PRODUCTKEY = "a11xsrW****"
    private val DEVICENAME = "paho_android"
    private val DEVICESECRET = "tLMT9QWD36U2SArglGqcHCDK9rK9****"
    fun publishMessage(pubTopic: String?, payload: String) {
        try {
            if (!mqttAndroidClient.isConnected) {
                mqttAndroidClient.connect()
            }
            val message =
                MqttMessage()
            message.payload = payload.toByteArray()
            message.qos = 0
            mqttAndroidClient.publish(
                pubTopic,
                message,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.i(TAG, "publish succeed! ")
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        Log.i(TAG, "publish failed!")
                    }
                })
        } catch (e: MqttException) {
            Log.e(TAG, e.toString())
            e.printStackTrace()
        }
    }

    interface ReceiveMessage {
        fun onSuccess(message: String?)
    }

    companion object {
        private var userName = ""
        private var passWord = ""
        private const val host = "tcp://45.119.82.186:1234"
        private const val TAG = "MqttClient"
        private lateinit var mqttAndroidClient: MqttAndroidClient
        fun subscribeTopic(topic: String?) {
            try {
                mqttAndroidClient.subscribe(
                    topic,
                    0,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.i(TAG, "subscribed succeed")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {
                            Log.i(TAG, "subscribed failed")
                        }
                    })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    init {
        var clientId = UUID.randomUUID().toString()
        /* Obtain the MQTT connection information clientId, username, and password. */
        val aiotMqttOption =
            AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET)
        if (aiotMqttOption == null) {
            Log.e("AiotMqttOption", "device info error")
        } else {
            clientId = aiotMqttOption.clientId
            userName = aiotMqttOption.username
            passWord = aiotMqttOption.password
        }

        /* Create an MqttConnectOptions object and configure the username and password. */
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.userName = userName
        mqttConnectOptions.password = passWord.toCharArray()

        /* Create an MqttAndroidClient object and set a callback interface. */
        mqttAndroidClient =
            MqttAndroidClient(activity, host, clientId)
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Log.i(TAG, "connection lost")
            }

            @Throws(Exception::class)
            override fun messageArrived(
                topic: String,
                message: MqttMessage
            ) {
                receiveMessage.onSuccess(String(message.payload))
                Log.i(
                    TAG,
                    "topic: " + topic + ", msg: " + String(message.payload)
                )
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.i(TAG, "msg delivered")
            }
        })

        /* Establish an MQTT connection */try {
            mqttAndroidClient.connect(
                mqttConnectOptions,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.i(TAG, "connect succeed")
                        subscribeTopic(subTopic)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        Log.i(TAG, "connect failed")
                    }
                })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}