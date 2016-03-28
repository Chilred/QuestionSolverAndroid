package com.example.chilred_pc.myapplication.SolveQuestion;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Chilred-pc on 15.03.2016.
 */
public class BingSearch extends AsyncTask<String, Void, String> {
    private static final String TAG = "DBG_" + GoogleSearchEngine.class.getName();

    @Override
    protected String doInBackground(String... question) {
        String result = "";
        String url = "http://www.bing.com/search?q=";
        String charset = "UTF-8";
        String key = question[0];
        String query = null;
        try {
            query = String.format("%s", URLEncoder.encode(key, charset));

            URLConnection con = new URL(url + query).openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result = result + inputLine;
            }
            in.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}