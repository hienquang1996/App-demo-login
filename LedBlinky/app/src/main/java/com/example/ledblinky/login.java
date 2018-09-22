package com.example.ledblinky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class login extends Activity implements View.OnClickListener {

    private Button login;
    private EditText name;
    private EditText pass;
    private TextView forgot;
    private int b=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText)findViewById(R.id.name);
        pass = (EditText)findViewById(R.id.pass);
        login = (Button)findViewById(R.id.login);
        forgot = (TextView)findViewById(R.id.forgot);


        forgot.setText("Number of attempts remainning: 5");
    }

    private void verify(String id , String mk){
        if((id.equals("a")) && (mk.equals("a"))){
            Intent intent = new Intent(login.this,menu.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
        else{
            b--;
            forgot.setText("Number of attemps remaining: "+ String.valueOf(b));
            Toast.makeText(getApplicationContext(),"false",Toast.LENGTH_LONG).show();
            if(b==0){
                login.setEnabled(false);
                forgot.setText("Ăn lìn roài");
                //Toast.makeText(getApplicationContext(),"an lin roi",Toast.LENGTH_LONG).show();
            }
        }
    }

        @Override
        public void onClick(View view) {
            if (view == login) {
                verify(name.getText().toString().trim(), pass.getText().toString().trim());
            }

        }
}

