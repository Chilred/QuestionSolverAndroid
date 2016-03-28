package com.example.chilred_pc.myapplication.SolveQuestion;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Chilred-pc on 10.03.2016.
 */
public class WikiSearchEngine extends AsyncTask<String, Void, String> {
    private static final String TAG = "DBG_" + WikiSearchEngine.class.getName();
    @Override
    protected String doInBackground(String... answear) {
        String action = "&action=query";
        String prop = "&prop=revisions&rvprop=content";
        String urlwiki = "https://de.wikipedia.org/w/api.php?format=json";
        String wiki = urlwiki + action + prop + "&titles=" + answear[0];
        StringBuilder result = new StringBuilder();
        URL url = null;
        try {
            url = new URL(wiki);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
