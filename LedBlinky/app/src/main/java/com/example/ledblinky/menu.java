package com.example.ledblinky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class menu extends Activity implements View.OnClickListener{

    private Button spi;
    private Button back;
    private Button mqtt;
    private Button temp;
    private TextView menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        spi = (Button)findViewById(R.id.spi);
        back = (Button)findViewById(R.id.back);
        mqtt = (Button)findViewById(R.id.mqtt);
        temp = (Button)findViewById(R.id.temp);
        menu = (TextView)findViewById(R.id.menu);

        spi.setOnClickListener(this);
        back.setOnClickListener(this);
        temp.setOnClickListener(this);
        //mqtt.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if (view == spi) {
            Intent intent = new Intent(menu.this,spi.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
        if (view == back) {
            Intent intent = new Intent(menu.this,login.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

     /*   if (view == mqtt) {
            Intent intent = new Intent(menu.this,mqtt.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }*/

        if (view == temp) {
            Intent intent = new Intent(menu.this,MainActivity.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

    }
}
