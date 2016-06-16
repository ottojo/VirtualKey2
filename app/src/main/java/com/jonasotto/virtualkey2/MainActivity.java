package com.jonasotto.virtualkey2;

import android.graphics.Outline;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabUnlock = (FloatingActionButton) findViewById(R.id.fabUnlock);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.unlock();
            }
        });
    }

    public void unlock(){

    }
}
