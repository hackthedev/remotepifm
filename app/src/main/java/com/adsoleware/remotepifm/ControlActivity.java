package com.adsoleware.remotepifm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ControlActivity extends AppCompatActivity {

    String wavFiles;
    String mp3files;
    String selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        getFiles();
    }


    public void getFiles(){
        try {


            new Thread(new Runnable() {
                public void run() {

                    try {
                        wavFiles = cmd("find /home/pi/Music/*.wav");

                        ListView lv = (ListView) findViewById(R.id.listbox_files);
                        ArrayList<String> ListElements = new ArrayList<>();

                        ArrayList list = new ArrayList<>();
                        String[] ary = wavFiles.split("\r\n");

                        ControlActivity.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {

                                try{
                                    BufferedReader bufReader = new BufferedReader(new StringReader(wavFiles));
                                    String line=null;
                                    while( (line=bufReader.readLine()) != null )
                                    {
                                        list.add(line.replace("/home/pi/Music/", ""));
                                    }
                                } catch (Exception ex){

                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ControlActivity.this, android.R.layout.simple_list_item_1, list);

                                lv.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                                        String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
                                        try {

                                            selectedFile = selectedFromList;

                                            Toast.makeText(ControlActivity.this, "Selected File " + selectedFromList, Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        });



                    } catch (Exception e) {
                        Log.e("Some Tag", e.getMessage());
                    }

                }
            }).start();


        } catch (Exception e) {
            Log.e("SomeTag", e.getMessage());
        }
    }

    public String cmd(
            String command) throws Exception {

        try{

            String username = MainActivity._user;
            String hostname = MainActivity._host;
            String password = MainActivity._pass;
            String _port = MainActivity._port;

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

    public void playsound(View view) {
        EditText freq = (EditText)findViewById(R.id.textbox_freq);

        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.e("Playback", cmd("sudo ./PiFmRds/src/pi_fm_rds -freq " + freq.getText().toString() + " -audio Music/" + selectedFile + " &"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void convertFile(View view) {

        ListView lv = (ListView) findViewById(R.id.listbox_files);
        ArrayList list = new ArrayList<>();

        new Thread(new Runnable() {
            public void run() {
                try {
                    mp3files = cmd("find /home/pi/Music/*.mp3");

                    String[] ary = mp3files.split("\r\n");

                    try{
                        BufferedReader bufReader = new BufferedReader(new StringReader(mp3files));
                        String line=null;
                        while( (line=bufReader.readLine()) != null )
                        {
                            cmd("ffmpeg -i '" + line + "' '" + line.replace(".mp3", "").replace(" ", "") + ".wav'");
                            Log.e("CONVERT", "Trying to convert " + line);
                            Log.e("INFO", "ffmpeg -i '" + line + "' '" + line.replace(".mp3", "").replace(" ", "") + ".wav'");
                        }
                    } catch (Exception ex){

                    }

                } catch (Exception e) {
                    Log.e("Some Tag", e.getMessage());
                }
            }
        }).start();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ControlActivity.this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
                try {

                    selectedFile = selectedFromList;

                    Toast.makeText(ControlActivity.this, "Selected File " + selectedFromList, Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getFiles();
    }

    public void stopSound(View view) {
        new Thread(new Runnable() {
            public void run() {

                try {
                    cmd("sudo pkill pi_fm_rds");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
