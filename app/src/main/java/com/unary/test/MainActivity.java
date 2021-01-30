package com.unary.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.unary.digitalrain.DigitalRain;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DigitalRain digitalRain = findViewById(R.id.digitalrain);

        // Change all the things
        //digitalRain.setTextColor(0xFFFFFF00);
        //digitalRain.setTextSize(12 * getResources().getDisplayMetrics().density);
        //digitalRain.setBackgroundColor(0xFF000000);

        //digitalRain.setRainSpeed(80);
        //digitalRain.setRainAlpha(32f / 255);
        //digitalRain.setRainIntensity(5);
        //digitalRain.setRainDepth(3);

        // Confused robot background
        //digitalRain.setTextColor(0x8A000000);
        //digitalRain.setBackgroundResource(R.drawable.ic_robot_confused);
        //digitalRain.getBackground().setAlpha(64);
    }
}