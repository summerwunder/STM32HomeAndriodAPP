package com.example.homeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.KeyEventDispatcher;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {
    private ImageView ledStatus;
    private ImageView buzzerStatus;



    private RadioButton radioButtonOff;
    private RadioButton radioButtonLow;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonHigh;
    private ImageView speedImage;

    private boolean isLedOn=false;
    private boolean isBuzzerOn=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UI_init();


    }


    void UI_init(){
        ledStatus=this.findViewById(R.id.led_button);
        buzzerStatus=this.findViewById(R.id.buzzer_button);
        radioButtonOff = findViewById(R.id.off_button);
        radioButtonLow = findViewById(R.id.low_button);
        radioButtonMedium = findViewById(R.id.medium_button);
        radioButtonHigh = findViewById(R.id.high_button);
        speedImage=findViewById(R.id.speed_img);

        radioButtonHigh.setOnCheckedChangeListener(radioListener);
        radioButtonOff.setOnCheckedChangeListener(radioListener);
        radioButtonLow.setOnCheckedChangeListener(radioListener);
        radioButtonMedium.setOnCheckedChangeListener(radioListener);
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




