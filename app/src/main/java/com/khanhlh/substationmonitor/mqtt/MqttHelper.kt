package com.khanhlh.substationmonitor.mqtt

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.set
import io.reactivex.Observable
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper(private val context: Context) {
    private var userName = ""
    private var passWord = ""
    private val host = "tcp://45.119.82.186:1234"
    private val TAG = "MqttClient"
    private var clientId = ""
    private var mqttConnectOptions: MqttConnectOptions

    private val PRODUCTKEY = "a11xsrW****"
    private val DEVICENAME = "paho_android"
    private val DEVICESECRET = "tLMT9QWD36U2SArglGqcHCDK9rK9****"
    private var gson = Gson()
    val isConnected = MutableLiveData<Boolean>(false)

    init {
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

        /* Create an MqttConnectOptions object and configure the username and password. */
        mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.userName = userName
        mqttConnectOptions.password = passWord.toCharArray()
    }

    private val client by lazy {
        val clientId = MqttClient.generateClientId()
        MqttAndroidClient(context, host, clientId)
    }

    companion object {
        const val TAG = "MqttClient"
    }

    fun connect(vararg topics: String, messageCallBack: MessageCallBack) {
        try {
            client.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    isConnected.set(true)
                    topics.forEach {
                        subscribeTopic(it)
                    }
                    logD("mqttHelper Connect Succeed")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    logD("mqttHelper Connect Failed")
                    isConnected.set(false)
                }
            })
            client.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
//                    topics.forEach {
//                        subscribeTopic(it)
//                    }
                    Log.d(TAG, "mqttHelper Connected to: $serverURI")
                    isConnected.set(true)
                }

                override fun connectionLost(cause: Throwable) {
                    Log.d(TAG, "mqttHelper The Connection was lost.")
                    messageCallBack.onError(cause)
                    isConnected.set(false)
                }

                @Throws(Exception::class)
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(TAG, "mqttHelper Incoming message from $topic: " + message.toString())

                    messageCallBack.onSuccess(message.toString())
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {

                }
            })


        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

//    fun publishMessage(topic: String, msg: String) {
//
//        try {
//            val message = MqttMessage()
//            message.payload = msg.toByteArray()
//            client.publish(topic, message.payload, 0, true)
//            Log.d(TAG, "$msg published to $topic")
//        } catch (e: MqttException) {
//            Log.d(TAG, "Error Publishing to $topic: " + e.message)
//            e.printStackTrace()
//        }
//
//    }

    @SuppressLint("CheckResult")
    fun publishMessage(topic: String, msg: String): Observable<String> {
        return Observable.create<String> { emitter ->
            try {
                if (!client.isConnected) client.connect()
                val message = MqttMessage()
                message.qos = 0
                message.payload = msg.toByteArray()

                client.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        logD("mqttHelper Publish succeed!")
                        logD("mqttHelper $topic - $message")
                        emitter.onNext("0")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        logD("mqttHelper Publish Failed!")
                        emitter.onNext("1")
                    }

                })
            } catch (e: MqttException) {
                Log.d(TAG, "mqttHelper Error Publishing to $topic: " + e.message)
                e.printStackTrace()
            }
        }

    }

    fun subscribeTopic(topic: String, qos: Int = 0) {
        client.subscribe(topic, qos).actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.d(TAG, "mqttHelper Subscribed to $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.d(TAG, "mqttHelper Failed to subscribe to $topic")
                exception.printStackTrace()
            }
        }
    }

    fun close() {
        client.apply {
            unregisterResources()
            close()
        }
    }

    interface MessageCallBack {
        fun onSuccess(message: String)
        fun onError(error: Throwable)
    }
}