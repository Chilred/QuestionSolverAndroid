package com.example.chilred_pc.myapplication.OCR;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.chilred_pc.myapplication.Config;
import com.example.chilred_pc.myapplication.MainActivity;
import com.example.chilred_pc.myapplication.OCR.Core.TessTool.TessAsyncEngine;
import com.example.chilred_pc.myapplication.R;
import com.example.chilred_pc.myapplication.SolveQuestion.Answear;
import com.example.chilred_pc.myapplication.SolveQuestion.AnswearQuestionAlg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ObserveScreenshotsAct extends Activity implements IOCR, View.OnClickListener  {
    static final String TAG = "DBG_" + ObserveScreenshotsAct.class.getName();
    private String result;
    private String choosenEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observe_screenshots);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#06B75A")));
        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREFS_FILE, 0);
        choosenEngine = settings.getString(Config.CHOOSENENGINE, "");
        Log.d(TAG, "choosenEngine: " + choosenEngine);
        observeSceenshot();
    }

    private void observeSceenshot(){
        HandlerThread handlerThread = new HandlerThread("content_observer");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                new ContentObserver(handler) {
                    @Override
                    public boolean deliverSelfNotifications() {
                        Log.d(TAG, "deliverSelfNotifications");
                        return super.deliverSelfNotifications();
                    }

                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        Log.d(TAG, "onChange " + uri.toString());
                        if (uri.toString().matches(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/[0-9]+")) {

                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(uri, new String[] {
                                        MediaStore.Images.Media.DISPLAY_NAME,
                                        MediaStore.Images.Media.DATA
                                }, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    final String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                                    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                    Log.d(TAG, "screen shot added " + fileName + " " + path);
                                    onPictureTaken(path);
                                }
                            } finally {
                                if (cursor != null)  {
                                    cursor.close();
                                }
                            }
                        } else{
                            Log.d(TAG, "Nothing happen here");
                        }
                        super.onChange(selfChange, uri);
                    }
                }
        );
    }

    public void onPictureTaken(final String path) {
        File imgFile = new  File(path);
        if(imgFile.exists()){
            Log.d(TAG, "exists");
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            TessAsyncEngine tae = new TessAsyncEngine(this);
            tae.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this, myBitmap, this);
           // cutString(this.result);

        }else {
            Log.d(TAG, "does not exists");
        }
    }

    @Override
    public void setResult(String result) {
        //Log.d("SETResult", result);
        this.result = result;
    }

    @Override
    public void cutString() throws Exception {
        String cutString = this.result;
        cutString = cutString.toLowerCase();
        String[] StringArray = cutString.split("\r\n|\r|\n");
        int pos;
        int countLines = 0;
        String question = "";
        String answear[] = new String[4];
        int anspos = 0;
        for (int i = 0; i < StringArray.length;i++){
            if(!StringArray[i].isEmpty()){
                if(contains(StringArray[i])){
                    pos = i;
                    question = StringArray[i];
                    countLines = 0;
                    for (int j = pos; j < StringArray.length; j++){
                        if(StringArray[j].contains("?") || StringArray[j].contains("...")){
                            if (j != pos) {
                                question = question + StringArray[j] + " ";
                            }
                            countLines = j;
                            break;
                        } else{
                            if(j != pos){
                                question = question + StringArray[j] + " ";
                            }
                        }
                    }
                }

            }
        }
        boolean questionmark = !question.contains("?") || question.contains("...");
        if(question.equals("") || questionmark){
            question = "No question found";
            Log.d("No question", question);
            Toast.makeText(getApplicationContext(), question,
                    Toast.LENGTH_LONG).show();
        }
        else{
            for (int i = countLines+1; i < StringArray.length; i++){
                if(!StringArray[i].isEmpty()){
                    if(anspos < answear.length){
                        answear[anspos] = StringArray[i];
                        anspos++;
                    }
                }
            }
            /*
            Log.d("question", question);
            Log.d("answear[0]", answear[0]);
            Log.d("answear[1]", answear[1]);
            Log.d("answear[2]", answear[2]);
            Log.d("answear[3]", answear[3]);*/
            Log.d("GetAnswear", "Start");
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            Log.d("Timer start", ts);
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREFS_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Config.TIMER, ts);
            editor.apply();
            AnswearQuestionAlg aqa = new AnswearQuestionAlg(this,question, answear, choosenEngine);
           // AnswearQuestionAlg aqa2 = new AnswearQuestionAlg(this,question, answear, Config.GOOGLE);


        }

    }

    public static boolean contains(String line) {
        for (Dictionary.QuestionWords c : Dictionary.QuestionWords.values()) {
            if (line.contains(c.name())) {
                return true;
            }
        }
        return false;
    }

    public void appendLog(String log) {

        File logFile = new File(Environment.getExternalStorageDirectory().getPath() +"/log.file");
        log = "\r\n" + log + "\r\n"+  "\r\n";
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.stopobserver:
                this.onStop();
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}
