package com.example.chilred_pc.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.example.chilred_pc.myapplication.OCR.OCR_new;
import com.example.chilred_pc.myapplication.OCR.ObserveScreenshotsAct;
import com.example.chilred_pc.myapplication.SolveQuestion.AnswearQuestionAlg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener {

    static final String TAG = "DBG_" + MainActivity.class.getName();
    String choosenEngine = Config.BING;
    CheckBox cbGoogle;
    CheckBox cbWiki;
    CheckBox cbBing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#06B75A")));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "pressed");
        Intent intent;
        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREFS_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (v.getId()){
            case R.id.Camera_start_button:
                intent = new Intent(this,OCR_new.class);
                startActivity(intent);
                break;
            case R.id.btnObserve:
                Log.d(TAG, "engine: " + choosenEngine);
                editor.putString(Config.CHOOSENENGINE, choosenEngine);
                editor.apply();
                intent = new Intent(this,ObserveScreenshotsAct.class);
                startActivity(intent);
                break;
            case R.id.btnSolution:
                Question[] questions =readQuestionFile();
                for (int i = 24; i < questions.length; i++){
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    Log.d("Timer start", ts);
                    settings = getSharedPreferences(Config.SHARED_PREFS_FILE, 0);
                    editor = settings.edit();
                    editor.putString(Config.TIMER, ts);
                    editor.apply();
                    try {
                        AnswearQuestionAlg aqa = new AnswearQuestionAlg(this, questions[i].getQuestion(), questions[i].getAnswear(),Config.GOOGLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case R.id.cbBing:
                Log.d(TAG, Config.BING);
                choosenEngine = Config.BING;
                cbGoogle = (CheckBox)findViewById(R.id.cbGoogle);
                cbWiki = (CheckBox)findViewById(R.id.cbWiki);
                cbGoogle.setChecked(false);
                cbWiki.setChecked(false);
                break;
            case R.id.cbGoogle:
                Log.d(TAG, Config.GOOGLE);
                choosenEngine = Config.GOOGLE;
                cbBing = (CheckBox)findViewById(R.id.cbBing);
                cbWiki = (CheckBox)findViewById(R.id.cbWiki);
                cbBing.setChecked(false);
                cbWiki.setChecked(false);
                break;
            case R.id.cbWiki:
                Log.d(TAG, Config.WIKI);
                choosenEngine = Config.WIKI;
                cbBing = (CheckBox)findViewById(R.id.cbBing);
                cbGoogle = (CheckBox)findViewById(R.id.cbGoogle);
                cbGoogle.setChecked(false);
                cbBing.setChecked(false);
                break;
        }
    }

    public Question[] readQuestionFile(){
        StringBuilder text = new StringBuilder();
        Question[] questions = new Question[102];
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard,"fragen.txt");

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }

        String[] temp = text.toString().split("\n");
        int j = 0;
        for(int i = 0; i < temp.length-5; i+=5){
            questions[j] = new Question(temp[i], temp[i+1], temp[i+2], temp[i+3], temp[i+4]);
            j++;
        }
        return questions;
    }
}
