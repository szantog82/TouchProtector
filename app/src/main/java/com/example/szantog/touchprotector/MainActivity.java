package com.example.szantog.touchprotector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));

        Intent serviceIntent = new Intent(this, TouchService.class);
        startService(serviceIntent);
        finish();

    }
}


