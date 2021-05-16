package com.adsoleware.remotepifm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ControlActivity extends AppCompatActivity {

    String wavFiles;
    String mp3files;
    String mp3files_check;
    String selectedFile;

    TextView status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        status = (TextView)findViewById(R.id.textbox_status);


        getFiles();
    }


    public void getFiles(){
            new Thread(new Runnable() {
                public void run() {

                    try {
                        wavFiles = cmd("find /home/pi/Music/*.wav");

                        ListView lv = (ListView) findViewById(R.id.listbox_files);

                        ArrayList list = new ArrayList<>();

                        ControlActivity.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {

                                try{
                                    BufferedReader bufReader = new BufferedReader(new StringReader(wavFiles));
                                    String line=null;
                                    while( (line=bufReader.readLine()) != null )
                                    while( (line=bufReader.readLine()) != null )
                                    {
                                        String replaced = line.replace("/home/pi/Music/", "").replace("_", " ").replace(".wav", "");
                                        String statustext_replaced = replaced;

                                        if(statustext_replaced.length() > 50){
                                            statustext_replaced = statustext_replaced.substring(0, 50);
                                        }

                                        list.add(replaced);
                                        status.setText("Added '" + statustext_replaced + "'");
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
                        Log.e("Some Nice Error", e.getMessage());
                    }

                }
            }).start();
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
            Log.e("CMD ERROR", e.getMessage());

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

        if(selectedFile != null && selectedFile.length() > 0){
            EditText freq = (EditText)findViewById(R.id.textbox_freq);
            String text = "Playing '" + selectedFile + "'";

            Log.e("Play", "Playing '" + selectedFile + "'");

            int i = text.length();

            if(i > 50){
                i = 50;
            }

            text = text.substring(0, i);
            status.setText(text);

            new Thread(new Runnable() {
                public void run() {
                    try {

                        Log.e("playsound", "Started Broadcast");
                        cmd("sudo ./PiFmRds/src/pi_fm_rds -freq " + freq.getText().toString() + " -audio Music/" + selectedFile + " -ps 'Pi FM' -rt 'Broadcast powered by Remote Pi FM app' &");

                    } catch (Exception e) {
                        Log.e("playSound: ", e.getMessage());
                    }
                }
            }).start();
        }
        else{

        }
    }


    public void convertFile(View view){
        try {


            new Thread(new Runnable() {
                public void run() {

                    try {
                        mp3files = cmd("find /home/pi/Music/*.mp3");
                        ListView lv = (ListView) findViewById(R.id.listbox_files);
                        ArrayList list = new ArrayList<>();

                        try{
                            BufferedReader bufReader = new BufferedReader(new StringReader(mp3files));
                            String line=null;
                            while( (line=bufReader.readLine()) != null ) {
                                String replaced = line.replace(".mp3", ".wav").replace(" ", "_").replace("[", "_").replace("]", "_")
                                        .replace("(", "_").replace(")", "_").replace("&", "").replace("#", "");


                                Log.e("INFO", cmd("ffmpeg -y -i '" + line + "' '" + replaced + "'"));
                                Log.e("FFMPEG", "ffmpeg -y -i '" + line + "' '" + replaced + "'");


                                ControlActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {

                                        getFiles();

                                        String statustext_replaced = replaced.replace("/home/pi/Music/", "").replace(".wav", "");
                                        if (statustext_replaced.length() > 50) {
                                            statustext_replaced = statustext_replaced.substring(0, 50);
                                        }

                                        status.setText("Converting '" + statustext_replaced + "'");

                                    }
                                });

                            }
                        } catch (Exception ex){
                            Log.e("Converting Error", ex.getMessage());
                        }


                    } catch (Exception e) {
                        Log.e("Some Nice Error", e.getMessage());
                    }

                    ControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            status.setText("Finished loading Files :)");
                        }
                    });

                }
            }).start();

        } catch (Exception e) {
            Log.e("SomeTag", e.getMessage());
        }
        getFiles();
    }

    public void stopSound(View view) {
        status.setText("Stopped Broadcast");

        new Thread(new Runnable() {
            public void run() {

                try {
                    cmd("sudo pkill pi_fm_rds");
                    Log.e("stopSound", "Stopped Broadcast");
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
                    jsch.setKnownHosts("~/.ssh/known_hosts");
                    java.util.Properties config = new java.util.Properties();
                    config.put("StrictHostKeyChecking", "no");
                    jschSession.setConfig(config);

                } catch (JSchException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        jschSession.setPassword(MainActivity._pass);
        jschSession.connect();

        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    int requestcode = 1;
    public void onActivityResult(int requestcode, int resultCode, Intent data){
        super.onActivityResult(requestcode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(data == null){
                return;
            }

            if ((data != null) && (data.getData() != null)){

                Uri myUri = data.getData();
                String path = myUri.getPath();

                uploadFile(getRealPath(this, myUri));

                //Uri uri = data.getData();
                //uploadFile(getRealPathFromURI_API11to18(this, uri));
            }

        }
    }


    public static final int REQ_PICK_AUDIO = 10001;
    public void openfilechooser(View view){

        //Toast.makeText(ControlActivity.this, "Feature not implemented yet", Toast.LENGTH_SHORT).show();

        //Throws error, not fixed, therefore "not implemented"
        Intent audio_picker_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(audio_picker_intent, REQ_PICK_AUDIO);
    }

    public void uploadFile(String file) {
        new Thread(new Runnable() {
            public void run() {

                ChannelSftp channelSftp = null;

                try {
                    channelSftp = setupJsch();
                    channelSftp.connect();

                } catch (JSchException e) {
                    Log.e("SFTP: ",e.getMessage());
                }

                String path = file;
                String filename = path.substring(path.lastIndexOf("/")+1);

                String remoteDir = "/home/pi/Music/";

                try {
                    channelSftp.put(file, remoteDir + filename);

                    ControlActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(ControlActivity.this, "File successfully uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (SftpException e) {
                    ControlActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(ControlActivity.this, "Unable to Upload File", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.e("uploadFile-put", e.getMessage());
                }

                channelSftp.exit();
            }
        }).start();
    }

    public static String getRealPath(Context context, Uri fileUri) {
        String realPath;
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            realPath = getRealPathFromURI_BelowAPI11(context, fileUri);
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            realPath = getRealPathFromURI_API11to18(context, fileUri);
        }
        // SDK > 19 (Android 4.4) and up
        else {
            realPath = getRealPathFromURI_API19(context, fileUri);
        }
        return realPath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String result = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
