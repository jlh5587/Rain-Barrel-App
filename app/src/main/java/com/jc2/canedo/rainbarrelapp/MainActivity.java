package com.jc2.canedo.rainbarrelapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;



public class MainActivity extends Activity {
    public class FeedData {
        public FeedData(String name, double data) {
            feedName = name;
            last_data = data;
        }
        public String feedName;
        public double last_data;
    }
    TextView responseView;

    Button update;
    EditText channel;
    ProgressDialog progress;

    final Handler mHandler = new Handler();
    FeedData mFD = new FeedData("m", 0);
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResults();
        }
    };
    final Runnable mStartProgressBar = new Runnable() {
        public void run() {
            startProgressBar();
        }
    };
    final Runnable mStopProgressBar = new Runnable() {
        public void run() {
            stopProgressBar();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseView = (TextView)findViewById(R.id.response);
        channel = (EditText)findViewById(R.id.channel);
        update = (Button)findViewById(R.id.update);
        progress = new ProgressDialog(this);


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread() {
                    public void run() {
                        mHandler.post(mStartProgressBar);
                        mFD = sendRequest();
                        mHandler.post(mUpdateResults);
                        mHandler.post(mStopProgressBar);
                    }
                };
                t.start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void updateResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("Channel Name: " + mFD.feedName + "\n");
        sb.append("Last Value:" + mFD.last_data+ "\n");
        responseView.setText(sb);

    }

    private void startProgressBar() {
        progress = ProgressDialog.show(MainActivity.this, "", "Requesting...", true);
    }
    private void stopProgressBar() {
        progress.cancel();
    }

    private MainActivity.FeedData sendRequest() {
        FeedData fd = new FeedData("", 0);

        try {
            URL url = new URL("https://thingspeak.com/channels/"+channel.getText()+"/field/1.json");
            URLConnection urlConnection = url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            JSONObject jo = new JSONObject(br.readLine());
            String name = jo.getJSONObject("channel").getString("name");
            fd.feedName = name;

            JSONArray feeds = jo.getJSONArray("feeds");
            JSONObject last_feed = feeds.getJSONObject(feeds.length() - 1);
            System.out.println(last_feed.toString());
            double data = last_feed.getDouble("field1");

            fd.last_data = data;
            return fd;
        } catch (Exception e) {
            fd.last_data = 1.1;
            e.printStackTrace();
            return fd;
        }
    }




}
