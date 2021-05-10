package com.adsoleware.remotepifm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        try {
            String wavFiles = cmd("ls -l /home/pi/Music/*.wav");
            String[] wavFiles_lines = wavFiles.split(System.getProperty("line.separator"));


            final ListView lv = (ListView) findViewById(R.id.listbox_files);
            final List<String> final_filelist = new ArrayList<String>(Arrays.asList(wavFiles_lines));

            // Create an ArrayAdapter from List
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_list_item_1, final_filelist);

            // DataBind ListView with items from ArrayAdapter
            lv.setAdapter(arrayAdapter);


            for(String s : wavFiles_lines){
                final_filelist.add(s);
            }

        } catch (Exception e) {
            Log.e("SomeTag", e.getMessage());
        }
    }

    public EditText host = (EditText)findViewById(R.id.textbox_host);
    public EditText user = (EditText)findViewById(R.id.textbox_user);
    public EditText pass = (EditText)findViewById(R.id.textbox_pass);
    public EditText port = (EditText)findViewById(R.id.textbox_port);

    public String cmd(
            String command) throws Exception {

        try{

            String username = user.getText().toString();
            String hostname = host.getText().toString();
            String password = pass.getText().toString();
            String _port = port.getText().toString();

            JSch jsch = new JSch();
            Session session = jsch.getSession(username, hostname, Integer.parseInt(_port));
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

            // Execute command
            //channelssh.setCommand("sudo ./PiFmRds/src/pi_fm_rds -freq 105.3 -audio Music/sound.wav");
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
            Log.e("Some Tag", e.getMessage());

            return "ERROR";
        }

    }

    public void showToast(String text){
        ControlActivity.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(ControlActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}