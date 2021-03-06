package com.adsoleware.remotepifm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

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
            Session session = jsch.getSession(username, hostname, port);
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

    private void writeToFile(String data, Context context, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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

                    Log.e("sshTest", result);

                    if(result.contains("/home/pi")){

                        String fileContent = host.getText() + "-" +
                                             user.getText() + "-" +
                                             pass.getText() + "-" +
                                             port.getText() + "-";

                        String filename = user.getText() + "@" + host.getText() + ".txt";


                        writeToFile(fileContent, MainActivity.this, filename);

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

    public void openUrl(String url){
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void openGithub(View view) {
        openUrl("https://github.com/hackthedev/remotepifm");
    }
}