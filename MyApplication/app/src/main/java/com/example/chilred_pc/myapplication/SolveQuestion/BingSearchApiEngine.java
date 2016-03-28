package com.example.chilred_pc.myapplication.SolveQuestion;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;


/**
 * Created by Chilred-pc on 09.03.2016.
 */
public class BingSearchApiEngine extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "DBG_" + BingSearchApiEngine.class.getName();

    @Override
    protected JSONObject doInBackground(String... params) {
        String result = "";
        JSONObject json = null;
        JSONObject results = null;
        try {
            final String query = URLEncoder.encode(params[0], Charset.defaultCharset().name());
            String APILink = "https://api.datamarket.azure.com/Data.ashx/Bing/Search/v1/Composite?Sources=%27web%27&Query=%27"
                    + query
                    + "%27&$format=JSON";
            String API_KEY = "1tTLpOFN+cMvpLSRhCyimicP5dFpgQmDSWr6kbmP8y0=";

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(APILink);
            String auth = API_KEY + ":" + API_KEY;
            String encodedAuth = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
            //Log.e("", encodedAuth);
            httpget.addHeader("Authorization", "Basic " + encodedAuth);
            //Execute and get the response.
            HttpResponse response = null;

            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                inputStream = entity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                       // Log.d(TAG, line);
                        //appendLog(line);
                        result += line;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                json = new JSONObject(result);
                JSONObject d = json.getJSONObject("d");
                results = d.getJSONArray("results").getJSONObject(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }
    public void appendLog(String log)
    {
        File logFile = new File(Environment.getExternalStorageDirectory().getPath() +"/bingsearch.file");

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
