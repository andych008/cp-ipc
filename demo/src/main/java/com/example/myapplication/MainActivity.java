package com.example.myapplication;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i("btn_call click");

                Timber.i("end-----------btn_call");

            }
        });


        findViewById(R.id.btn_asyncCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i("btn_asyncCall click");

                Timber.i("end-----------btn_asyncCall");
            }
        });


        findViewById(R.id.btn_asyncCall2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i("btn_asyncCall2 click");

                Timber.i("end-----------btn_asyncCall2");
            }
        });
    }

    public static void printResult(Bundle result) {
        if (result != null) {
            StringBuilder sb = new StringBuilder();
            for (String key : result.keySet()) {
                sb.append(String.format("[%s : %s]\n", key, result.get(key)));
            }
            Timber.i("result = %s", sb);
        }
    }
}
