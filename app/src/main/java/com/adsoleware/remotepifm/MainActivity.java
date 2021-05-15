package com.adsoleware.remotepifm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port,
            String command) throws Exception {

        try{
            hostname = hostname.replace(" ", "");


            JSch jsch = new JSch();
            Session session = jsch.getSession(username, hostname, 22);
            session.setPassword(password);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();

            // SSH Channel
            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            channelssh.setCommand(command);

            channelssh.connect();

            while(true){
                if(channelssh.isClosed()){
                    break;
                }
            }

            channelssh.disconnect();

            return baos.toString();
        } catch (Exception e){
            Log.e("MainActivity-Login:", e.getMessage());

            return "ERROR";
        }

    }

    public void showToast(String text){
        MainActivity.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String _host;
    public static String _user;
    public static String _pass;
    public static String _port;
    public static String _freq;

    public void sshTest(View view) {
        new Thread(new Runnable() {
            public void run() {

                try {
                    EditText host = (EditText)findViewById(R.id.textbox_host);
                    EditText user = (EditText)findViewById(R.id.textbox_user);
                    EditText pass = (EditText)findViewById(R.id.textbox_pass);
                    EditText port = (EditText)findViewById(R.id.textbox_port);



                    _host = host.getText().toString().replace(" ", "");
                    _user = user.getText().toString();
                    _pass = pass.getText().toString();
                    _port = port.getText().toString();


                    String result = executeRemoteCommand(user.getText().toString(), pass.getText().toString(), host.getText().toString(), Integer.parseInt(port.getText().toString()), "pwd");

                    if(result.contains("/home/pi")){

                        showToast("Successfully connected");

                        Intent intent = new Intent (MainActivity.this, ControlActivity.class);
                        startActivity(intent);
                    }
                    else if(result.contains("Connection refused")){
                        showToast("Connection was refused");
                    }
                    else{
                        showToast("Looks like your System is not a raspberry pi");

                    }

                    Log.e("sometag", result);
                } catch (Exception e) {
                    Log.e("Some Tag", e.getMessage());
                }

            }
        }).start();
    }
}