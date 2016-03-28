package com.example.chilred_pc.myapplication.SolveQuestion;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class GoogleAPISearchEngine extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "DBG_" + GoogleAPISearchEngine.class.getName();
    @Override
    protected JSONObject doInBackground(String... params) {
        String key = "AIzaSyDPwDgbiJjU_q5Ok9qCvjY75bHFSQLb81I";
        String qry = params[0];
        String result = "";
        JSONObject results = null;
        try {
            qry = URLEncoder.encode(qry, Charset.defaultCharset().name());
            URL url = new URL(
                    "https://www.googleapis.com/customsearch/v1?key="+key+ "&cx=006825905863574405091:f6xbqi01via&q="+ qry + "&alt=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            //Log.d(TAG,"Output from Server .... ");
            while ((output = br.readLine()) != null) {
                result = result + output;
            }
            conn.disconnect();
            results = new JSONObject(result);
            appendLog(result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }
    public void appendLog(String log)
    {
        File logFile = new File(Environment.getExternalStorageDirectory().getPath() +"/googleapi.file");

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(log);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

