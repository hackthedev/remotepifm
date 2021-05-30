package com.adsoleware.remotepifm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class ConnectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        traverse(ConnectionsActivity.this.getFilesDir());
    }

    public void traverse (File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();

            if(files.length == 0){
                openMain(this.findViewById(android.R.id.content).getRootView());
            }

            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    traverse(file);
                } else {
                    addList(file.getName().replace(".txt", ""));
                }
            }
        }
    }

    private String readFromFile(String file) {

        Context context = ConnectionsActivity.this;
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void openMain(View view){
        Intent intent = new Intent (ConnectionsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void openUrl(String url){
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void openGithub(View view) {
        openUrl("https://github.com/hackthedev/remotepifm");
    }

    public String selectedFile;
    public void addList(String Item){
        ListView lv = (ListView) findViewById(R.id.listbox_files);
        ArrayList list = new ArrayList<>();

        list.add(Item);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConnectionsActivity.this, android.R.layout.simple_list_item_1, list);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
                try {

                    selectedFile = ConnectionsActivity.this.getFilesDir() + "/" + Item + ".txt";

                    String content = readFromFile(Item + ".txt");
                    String[] splitted = content.split("-");

                    Log.e("test", content);
                    String host = splitted[0].replace(" ", "").replace("\n", "");
                    String user = splitted[1];
                    String pass = splitted[2];
                    String port = splitted[3];


                    Log.e("Host:", host);
                    Log.e("User:", user);
                    Log.e("Pass:", pass);
                    Log.e("Port:", port);

                    MainActivity._host = host;
                    MainActivity._user = user;
                    MainActivity._pass = pass;
                    MainActivity._port = port;

                    sshTest(host, user, pass, port);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                selectedFile = Item + ".txt";
                new File(ConnectionsActivity.this.getFilesDir() + "/" + selectedFile).delete();
                Toast.makeText(ConnectionsActivity.this, "Deleted File " + selectedFile, Toast.LENGTH_SHORT).show();


                list.remove(Item);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                return true;
            }
        });

        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            String port,
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
        ConnectionsActivity.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(ConnectionsActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sshTest(String host, String user, String pass, String port) {
        new Thread(new Runnable() {
            public void run() {

                try {

                    String result = executeRemoteCommand(user, pass, host, port, "pwd");

                    Log.e("sshTest", "Result is " + result);

                    if(result.contains("/home/pi")){

                        showToast("Successfully connected");

                        Intent intent = new Intent (ConnectionsActivity.this, ControlActivity.class);
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