package com.swolo.lpy.pysx.main.recycler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.swolo.lpy.pysx.R;

public class MainOneActivity extends AppCompatActivity {

    private Button btnLinerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_one);
        btnLinerView =  findViewById(R.id.btn_liner_view);
        btnLinerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainOneActivity.this, MainTwoActivity.class);
                startActivity(intent);
            }
        });


    }
}
