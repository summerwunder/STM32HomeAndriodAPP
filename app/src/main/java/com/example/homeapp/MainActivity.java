package com.example.homeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.KeyEventDispatcher;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private final String TAG="MainActivity";
    private ImageView ledStatus;
    private ImageView buzzerStatus;

    private RadioButton radioButtonOff;
    private RadioButton radioButtonLow;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonHigh;
    private ImageView speedImage;
    private ImageView connectImage;

    private TextView humi;
    private TextView temp;
    private TextView smoke;
    private TextView coDense;

    private boolean isLedOn=false;
    private boolean isBuzzerOn=false;

    //有关于联网
    //private final String host="tcp://broker.emqx.io:1883";
    private final String host="tcp://10.79.217.111:1883";
    //private final String host="tcp://192.168.43.222:1883";
    private final String userName = "android";
    private final String passWord = "android";
    private final String mqtt_id = "admin01";
    private final String mqtt_sub_topic = "esp/#";
    private final String mqtt_pub_topic = "control/1";

    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UI_init();
        Mqtt_init();
        startReconnect();

        handler=new Handler(Looper.myLooper()) {
            @SuppressLint({"SetTextIl8n", "HandlerLeak"})
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        //Toast.makeText(MainActivity.this,msg.obj.toString() ,Toast.LENGTH_SHORT).show();

                        String str=msg.obj.toString();
                        String jsonData="";
                        try {
                            String[] parts = str.split("---");
                            if (parts.length == 2) {
                                jsonData = parts[1].trim();
                                // 创建JSONObject实例并传入JSON字符串
                            }
                            JSONObject json = new JSONObject(jsonData);

                            double humiValue = json.getDouble("humi");
                            double tempValue = json.getDouble("temp");
                            double coValue=json.getDouble("coValue");
                            int smokeValue=json.getInt("smokeValue");

                            temp.setText(tempValue+"C");
                            humi.setText(humiValue+"%");
                            coDense.setText(coValue+"%");
                            smoke.setText(smokeValue+"ppm");
                        } catch (JSONException e) {
                            // 处理JSON解析异常
                            e.printStackTrace();
                        }

                        break;
                    case 30:  //连接失败
                        Toast.makeText(MainActivity.this,"连接失败" ,Toast.LENGTH_SHORT).show();
                        connectImage.setImageResource(R.mipmap.close);
                        break;
                    case 31:   //连接成功
                        Toast.makeText(MainActivity.this,"连接成功" ,Toast.LENGTH_SHORT).show();
                        connectImage.setImageResource(R.mipmap.open);
                        try {
                            client.subscribe(mqtt_sub_topic,1);

                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }





    private void UI_init(){
        ledStatus=this.findViewById(R.id.led_button);
        buzzerStatus=this.findViewById(R.id.buzzer_button);
        radioButtonOff = findViewById(R.id.off_button);
        radioButtonLow = findViewById(R.id.low_button);
        radioButtonMedium = findViewById(R.id.medium_button);
        radioButtonHigh = findViewById(R.id.high_button);
        speedImage=findViewById(R.id.speed_img);
        connectImage=findViewById(R.id.connnect_button);
        temp=findViewById(R.id.temp_text);
        humi=findViewById(R.id.humi_text);
        coDense=findViewById(R.id.codense);
        smoke=findViewById(R.id.smoke);

        radioButtonHigh.setOnCheckedChangeListener(radioListener);
        radioButtonOff.setOnCheckedChangeListener(radioListener);
        radioButtonLow.setOnCheckedChangeListener(radioListener);
        radioButtonMedium.setOnCheckedChangeListener(radioListener);

        radioButtonOff.setChecked(true);
        ledStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            //TODO 发送LED开关的JSON数据
            public void onClick(View v) {
                if(isLedOn){
                    isLedOn=false;
                    ledStatus.setImageResource(R.mipmap.led_off);
                }else{
                    isLedOn=true;
                    ledStatus.setImageResource(R.mipmap.led_on);
                }
            }
        });
        buzzerStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            //TODO 发送BUZZER开关的JSON数据
            public void onClick(View v) {
                if(isBuzzerOn){
                    isBuzzerOn=false;
                    buzzerStatus.setImageResource(R.mipmap.led_off);
                }else{
                    isBuzzerOn=true;
                    buzzerStatus.setImageResource(R.mipmap.led_on);
                }
            }
        });

    }
    private void Mqtt_init()
    {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                   Log.i(TAG,"connectionLost----------");
                   connectImage.setImageResource(R.mipmap.close);
                   startReconnect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    Log.i(TAG,"deliveryComplete---------" + token.isComplete());
                    Toast.makeText(MainActivity.this,"发送信息",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    Log.i(TAG,"messageArrived----------");
                    Message msg = new Message();
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!(client.isConnected()) )  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }


    private void startReconnect() {
        //创建单线程的定时执行器
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);//任务将在启动后立即执行，然后每隔 10 秒执行一次
    }

    private CompoundButton.OnCheckedChangeListener radioListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                int buttonId = buttonView.getId();

                if (buttonId == R.id.off_button) {
                    speedImage.setImageResource(R.mipmap.fan_off);
                } else if (buttonId == R.id.low_button) {
                    speedImage.setImageResource(R.mipmap.fan_low);
                } else if (buttonId == R.id.medium_button) {
                    speedImage.setImageResource(R.mipmap.fan_medium);
                } else if (buttonId == R.id.high_button) {
                    speedImage.setImageResource(R.mipmap.fan_high);
                }
            }
        }
    };
}




