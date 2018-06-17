package com.example.harpalpannu.nodechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {


    Socket socket;
    ListView mListView;
    ArrayAdapter adapter;
    ArrayList<String> mobileArray = new ArrayList<>();

//    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
//            "WebOS","Ubuntu","Windows7","Max OS X"};
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this, R.layout.activity_listview, mobileArray);
        mListView.setAdapter(adapter);

        try {
            socket = IO.socket("http://10.0.0.28:3000");
        } catch (URISyntaxException e) {
            Log.d("Hz",e.getMessage());
        }

    }

    public void sendMessage(View view) {
        EditText editText = findViewById(R.id.editText);
        EditText editText1 =  findViewById(R.id.editText2);
        JSONObject obj = new JSONObject();
        try {
            obj.put("Message", editText.getText().toString());
            obj.put("User", editText1.getText().toString());
        } catch (JSONException e) {
            Log.d("Hz",e.getMessage());
        }
        mobileArray.add("You : " + editText.getText().toString());
        adapter.notifyDataSetChanged();
        mListView.setSelection(adapter.getCount() - 1);
        editText.setText("");
        socket.emit("Msg",obj);
    }

    public void Connect(View view) {
        final EditText  editText = findViewById(R.id.editText2);
        final String User = editText.getText().toString();
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {



            @Override
            public void call(Object... args) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        editText.setEnabled(false);
                        Button button = findViewById(R.id.button2);
                        button.setEnabled(false);
                        Button button2 = findViewById(R.id.button);
                        button2.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Server Connected", Toast.LENGTH_LONG).show();
                    }
                });
                JSONObject obj = new JSONObject();
                try {
                    obj.put("User", User );
                } catch (JSONException e) {
                    Log.d("Hz",e.getMessage());
                }
                socket.emit("UserConnected", obj);
            }

        }).on("NewUser", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                final JSONObject data = (JSONObject) args[0];

                runOnUiThread(new Runnable() {
                    public void run() {

                        try {

                            String Msg = data.getString("User") + " => " + "Online";
                            mobileArray.add(Msg);
                            adapter.notifyDataSetChanged();
                            mListView.setSelection(adapter.getCount() - 1);
                        } catch (JSONException e) {
                            Log.d("Hz",e.getMessage());
                        }
                    }
                });
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Server Disconnected",Toast.LENGTH_LONG).show();
                    }
                });
            }

        }).on("NewMsg", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {

                final JSONObject data = (JSONObject) args[0];

                runOnUiThread(new Runnable() {
                    public void run() {

                        try {
                            String Msg = data.getString("User") + ": " + data.getString("Message");
                            mobileArray.add(Msg);
                            adapter.notifyDataSetChanged();
                            mListView.setSelection(adapter.getCount() - 1);
                        } catch (JSONException e) {
                            Log.d("Hz",e.getMessage());
                        }
                    }
                });
            }

        }).on("UserDis", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {

                final JSONObject data = (JSONObject) args[0];

                runOnUiThread(new Runnable() {
                    public void run() {

                        try {
                            String Msg = data.getString("disMsg");
                            mobileArray.add(Msg);
                            adapter.notifyDataSetChanged();
                            mListView.setSelection(adapter.getCount() - 1);
                        } catch (JSONException e) {
                            Log.d("Hz",e.getMessage());
                        }
                    }
                });
            }

        }).on("Online", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                final JSONObject data = (JSONObject) args[0];

                runOnUiThread(new Runnable() {
                    public void run() {

                        try {
                            Button button = findViewById(R.id.button2);
                            String Msg = "Online : " + data.getString("Online");
                            button.setText(Msg);
                        } catch (JSONException e) {
                            Log.d("Hz",e.getMessage());
                        }
                    }
                });
            }

        });
        socket.connect();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != Activity.RESULT_OK)
        {
            Log.d("z","COULD NOT GET A GOOD RESULT.");
            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if( result!=null)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;

        }
        if(requestCode == 101)
        {
            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            JSONObject obj = new JSONObject();
            try {
                obj.put("ID", result );
                EditText editText = findViewById(R.id.editText2);
                obj.put("username",editText.getText().toString() );
            } catch (JSONException e) {
                Log.d("Hz",e.getMessage());
            }
            socket.emit("gotID", obj);
        }
    }

    public void scan(View view) {
        Intent i = new Intent(MainActivity.this,QrCodeActivity.class);
        startActivityForResult( i,101);
    }
}
