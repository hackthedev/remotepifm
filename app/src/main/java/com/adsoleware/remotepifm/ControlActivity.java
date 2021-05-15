package com.adsoleware.remotepifm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

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

    TextView status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        status = (TextView)findViewById(R.id.textbox_status);

        if(checkMp3() == true){
            status.setText("Found .mp3 File! Click 'Convert'");
        }
        else{
            status.setText("No Files were Found. Try Uploading new .mp3/.wav files");
        }

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
                                        String replaced = line.replace("/home/pi/Music/", "").replace("_", " ").replace(".wav", "");
                                        String statustext_replaced = replaced;

                                        if(statustext_replaced.length() > 50){
                                            statustext_replaced = statustext_replaced.substring(0, 50);
                                        }
                                        status.setText("Added '" + statustext_replaced + "'");
                                        list.add(replaced);
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

                                            selectedFile = selectedFromList.replace(" ", "_") + ".wav";

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

        status.setText("Finished converting files :)");
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

        String text = "Playing '" + selectedFile + "'";

        int i = text.length();

        if(i > 50){
            i = 50;
        }

        text = text.substring(0, i);
        status.setText(text);

        new Thread(new Runnable() {
            public void run() {
                try {


                    Log.e("Playback", cmd("sudo ./PiFmRds/src/pi_fm_rds -freq " + freq.getText().toString() + " -audio Music/" + selectedFile + " -ps 'Pi FM' -rt 'Broadcast powered by Remote Pi FM app' &"));

                } catch (Exception e) {
                    Log.e("playSound: ", e.getMessage());
                }
            }
        }).start();
    }

    int checkMp3_i = 0;
    public boolean checkMp3() {

        ListView lv = (ListView) findViewById(R.id.listbox_files);
        ArrayList list = new ArrayList<>();


        try {
            mp3files = cmd("find /home/pi/Music/*.mp3");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] checkMp3_ary = mp3files.split("\r\n");

        for(String s : checkMp3_ary){
            checkMp3_i += 1;
            Log.e("Found CheckMp3", String.valueOf(checkMp3_i));
        }

        if(checkMp3_i > 0){

            return true;
        }

        return false;
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
                            String formatted = line.replace(".mp3", "").replace(" ", "_").replace("[", "_").replace("]", "_")
                                                    .replace("(", "_").replace(")", "_").replace("&", "").replace("#", "")
                                                    .replace("!", "");


                            status.post(new Runnable() {
                                public void run() {
                                    String text = "Converting '" + formatted.replace("/home/pi/Music/", "") + "'";

                                    if(text.length() > 50){
                                        text = text.substring(0, 50);
                                    }

                                    status.setText(text);
                                }
                            });


                            cmd("ffmpeg -i '" + line + "' '" + formatted + ".wav'");

                            getFiles();

                            Log.e("CONVERT", "Trying to convert " + line + " to " + formatted);
                            Log.e("INFO", "ffmpeg -i '" + line + "' '" + formatted + ".wav'");
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

                    status.setText("Stopped Broadcast");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private ChannelSftp setupJsch() throws JSchException {

        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(MainActivity._user, MainActivity._host);

        new Thread(new Runnable() {
            public void run() {


                try {


                    try {
                        jsch.setKnownHosts("/Users/john/.ssh/known_hosts");
                    } catch (JSchException e) {
                        e.printStackTrace();
                    }


                    jschSession.setPassword(MainActivity._pass);
                    jschSession.connect();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    int requestcode = 1;
    public void onActivityResult(int requestcode, int resultCode, Intent data){
        super.onActivityResult(requestcode, resultCode, data);

        if(requestcode == requestcode && resultCode == Activity.RESULT_OK){
            if(data == null){
                return;
            }
            Uri uri = data.getData();
            Toast.makeText(ControlActivity.this, uri.getPath(), Toast.LENGTH_SHORT).show();

            uploadFile(uri.getPath());
        }
    }

    public void openfilechooser(View view){

        Toast.makeText(ControlActivity.this, "Feature not implemented yet", Toast.LENGTH_SHORT).show();

        // Throws error, not fixed, therefore "not implemented"
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("*/*");
        // String[] mimetypes = {"audio/mp3", "audio/wav"};
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        // startActivityForResult(intent, requestcode);
    }

    public void uploadFile(String path) {


        new Thread(new Runnable() {
            public void run() {

                ChannelSftp channelSftp = null;

                try {
                    channelSftp = setupJsch();
                    channelSftp.connect();

                } catch (JSchException e) {
                    Log.e("SFTP: ",e.getMessage());
                }


                String localFile = path;
                String remoteDir = "/home/pi/Music";

                try {
                    channelSftp.put(localFile, remoteDir + "jschFile.txt");
                } catch (SftpException e) {
                    e.printStackTrace();
                }

                channelSftp.exit();
            }
        }).start();
    }
}
