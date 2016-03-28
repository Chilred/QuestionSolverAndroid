package com.example.chilred_pc.myapplication.SolveQuestion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.chilred_pc.myapplication.Config;
import com.example.chilred_pc.myapplication.OCR.Dictionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Chilred-pc
 */
public class AnswearQuestionAlg {
    private static final String TAG = "DBG_" + AnswearQuestionAlg.class.getName();
    private String rightAnswear = "";
    private String searchMethod;
    private Context context;
    private String endtime = "";
    private String question;

    public AnswearQuestionAlg(Context context, String question, String[] answear, String searchMethod) throws Exception {
        this.searchMethod = searchMethod;
        this.question = question;
        Answear[] allAnswears = new Answear[4];
        this.context = context;
        for (int i = 0; i < answear.length; i++) {
            allAnswears[i] = new Answear(answear[i], 0);
        }
        switch (searchMethod) {
            case Config.BING:
                bingSearch(allAnswears);
                break;
            case Config.GOOGLE:
               // googleSearch(question, allAnswears,true);
                googleCustomSearch(allAnswears);
                break;
            case Config.WIKI:
                wikiSearch(allAnswears);
                break;
        }
    }

    private void bingSearch(Answear[] allAnswears) throws JSONException, IOException, ExecutionException, InterruptedException {
        //find with apostrophe
        Log.d("Question", this.question);
        Log.d(TAG, "Bing");
        Log.d(TAG,"lets try to find with apostrophe");
        JSONObject webResultJSON = searchRequestBing(this.question, "");
        for (int i = 0; i < allAnswears.length; i++){
            allAnswears[i].setWebTotal(webResultJSON.getInt("WebTotal"));
            int webLen = webResultJSON.getJSONArray("Web").length();
            allAnswears[i].setNewUrlStringDescription(webLen, webLen);
            for (int j = 0; j < webLen; j++) {
                allAnswears[i].setUrlTitel(webResultJSON.getJSONArray("Web").getJSONObject(j).get("Title").toString(), j);
                allAnswears[i].setDescription(webResultJSON.getJSONArray("Web").getJSONObject(j).get("Description").toString(), j);
            }
        }
        findWordsInWebResult(allAnswears);

        Log.d(TAG, "Okay, let me search again");
        webResultJSON = new org.json.JSONObject();
        for (int i = 0; i < allAnswears.length; i++) {
            String answear = allAnswears[i].getTitle();
            webResultJSON = searchRequestBing(this.question, answear);
            allAnswears[i].setWebTotal(webResultJSON.getInt("WebTotal"));
            //allAnswears[i].setUrls(webResultJSON.getJSONArray("Web").toString());
            int webLen = webResultJSON.getJSONArray("Web").length();
            allAnswears[i].setNewUrlStringDescription(webLen, webLen);
            for (int j = 0; j < webLen; j++) {
                allAnswears[i].setUrlTitel(webResultJSON.getJSONArray("Web").getJSONObject(j).get("Title").toString(), j);
                allAnswears[i].setDescription(webResultJSON.getJSONArray("Web").getJSONObject(j).get("Description").toString(), j);
            }
        }
        //  findWebtotal(allAnswears);
        findWordsInWebResult(allAnswears);
        String result = printPoints(allAnswears);
        appendLog(this.question, allAnswears, result);
    }

    private void googleCustomSearch(Answear[] allAnswears) throws JSONException {
        Log.d("Question", question);
        JSONObject webResultJSON = searchRequestGoogleJSON(this.question, "");
        for (int i = 0; i < allAnswears.length; i++){
            JSONArray items = webResultJSON.getJSONArray("items");
            JSONObject searchInfo = webResultJSON.getJSONObject("searchInformation");
            //allAnswears[i].setWebTotal(searchInfo.getInt("totalResults"));
            int webLen = items.length();
            allAnswears[i].setNewUrlStringDescription(webLen, webLen);
            for (int j = 0; j < webLen; j++) {
                allAnswears[i].setUrlTitel(items.getJSONObject(j).get("title").toString(), j);
                allAnswears[i].setDescription(items.getJSONObject(j).get("snippet").toString(), j);
            }
        }
        findWordsInWebResult(allAnswears);
        Log.d(TAG,"Okay, let me search again");
        webResultJSON = new org.json.JSONObject();
            for (int i = 0; i < allAnswears.length; i++) {
                String answear = allAnswears[i].getTitle();
                webResultJSON = searchRequestGoogleJSON(this.question, answear);
                JSONArray items = webResultJSON.getJSONArray("items");
                JSONObject searchInfo = webResultJSON.getJSONObject("searchInformation");
                allAnswears[i].setWebTotal(searchInfo.getInt("totalResults"));
                Log.d(TAG, String.valueOf(allAnswears[i].getWebTotal()));
                int webLen = items.length();
                allAnswears[i].setNewUrlStringDescription(webLen, webLen);
                for (int j = 0; j < webLen; j++) {
                    allAnswears[i].setUrlTitel(items.getJSONObject(j).get("title").toString(), j);
                    allAnswears[i].setDescription(items.getJSONObject(j).get("snippet").toString(), j);
                }
            }
            //findWebtotal(allAnswears);
            findWordsInWebResult(allAnswears);
            String result = printPoints(allAnswears);
            appendLog(this.question, allAnswears, result);
    }

    private void googleSearch(String askQuestion, Answear[] allAnswears , boolean firstRun) throws IOException {
        Log.d("Question", askQuestion);
        Log.d(TAG, "Google");
        if(firstRun) {
            askQuestion = "\"" + askQuestion + "\" ";
        }else{
            askQuestion = askQuestion.replace("\"","");
        }
        int countAnswears = 0;
        boolean apostropheWorks = false;
        for (int i = 0; i < allAnswears.length; i++) {
            String htmlString = searchRequestGoogle(askQuestion, allAnswears[i].getTitle());
            //how many results
            if (!htmlString.isEmpty()) {
                String resultStats = "id=\"resultStats\"";
                int divStart = htmlString.indexOf(resultStats);
                int divEnd = htmlString.indexOf("</div>", divStart);
                String cutWebtotal = htmlString.substring(divStart, divEnd);
                int webtotal = Integer.parseInt(cutWebtotal.replaceAll("[\\D]", ""));
                allAnswears[i].setWebTotal(webtotal);
                if (webtotal == 1) {
                    Log.d(TAG, "Maybe the right answear");
                    //TODO make it flexible
                    if(htmlString.contains("quizduell")){
                        if(countAnswears < 1){
                            countAnswears++;
                            apostropheWorks = true;
                            rightAnswear = allAnswears[i].getTitle();
                        }else{
                            apostropheWorks = false;
                            rightAnswear = "";
                            Log.d(TAG,"More than one answear has just one link");
                        }
                    }else{
                        Log.d(TAG, "Not a special solution site");
                    }
                }
                if(!apostropheWorks){
                    // Find words in the text
                    String searchDiv = "<div class=\"g\">";
                    divStart = htmlString.indexOf(searchDiv);
                    divEnd = htmlString.length();
                    String cutString = htmlString.substring(divStart, divEnd);
                    divEnd = cutString.indexOf("</ol>");
                    cutString = cutString.substring(0, divEnd);

                    cutString = cutString.replaceAll("\\<.*?>", "");
                    allAnswears[i].setUrls(cutString);
                }else{
                    Log.d("Right Result", rightAnswear);
                }
            } else{
                Log.d(TAG, "Cant get informationen");
                Toast.makeText(context, "Google stops the research", Toast.LENGTH_SHORT).show();
            }
        }
        findWordsInWebResult(allAnswears);

        if(firstRun) {
            googleSearch(askQuestion, allAnswears, false);
        }else {
            String result = printPoints(allAnswears);
            appendLog(askQuestion, allAnswears, result);
        }
    }

    private void wikiSearch(Answear[] allAnswears) throws Exception {
        for (int i = 0; i < allAnswears.length; i++) {
            String wikiString = searchRequestWiki(allAnswears[i].getTitle());
            wikiString = wikiString.replace("\\u201", "Ü");
            wikiString = wikiString.replace("\\u00fc", "ü");
            wikiString = wikiString.replace("\\u00C4", "Ä");
            wikiString = wikiString.replace("\\u00e4", "ä");
            wikiString = wikiString.replace("\\u00D6", "Ö");
            wikiString = wikiString.replace("\\u00f6", "ö");
            wikiString = wikiString.replace("\\00DF", "ß");
            wikiString = wikiString.replace("\\n", "");
            wikiString = wikiString.replace("&nbsp", "");
            allAnswears[i].setUrls(wikiString);
        }
        String[] questionsWords = this.question.split(" ");
        findWordsInWebResult(allAnswears);
        for (int i = 0; i < questionsWords.length; i++) {
            String wikiString = searchRequestWiki(questionsWords[i]);
            wikiString = wikiString.replace("\\u201", "Ü");
            wikiString = wikiString.replace("\\u00fc", "ü");
            wikiString = wikiString.replace("\\u00C4", "Ä");
            wikiString = wikiString.replace("\\u00e4", "ä");
            wikiString = wikiString.replace("\\u00D6", "Ö");
            wikiString = wikiString.replace("\\u00f6", "ö");
            wikiString = wikiString.replace("\\00DF", "ß");
            wikiString = wikiString.replace("\\n", "");
            wikiString = wikiString.replace("&nbsp", "");
            wikiString = wikiString.toLowerCase();
            for (int j = 0; j < allAnswears.length; j++) {
                String[] answear = allAnswears[j].getTitle().toLowerCase().split(" ");
                for (int x = 0; x < answear.length; x++){
                    if (!containsWord(answear[x])) {
                        answear[x] = answear[x].replaceAll("[^A-Za-z0-9]/", "");
                        if(wikiString.contains(answear[x])){
                            int count = allAnswears[j].getWebTotal() + countSubstring(answear[x], wikiString);
                            allAnswears[j].setWebTotal(count);
                        }
                    }
                }
            }
        }
        String result = printPoints(allAnswears);
        appendLog(this.question, allAnswears, result);
    }

    public static boolean containsWord(String test) {
        for (Dictionary.MostCommonWords c : Dictionary.MostCommonWords.values()) {
            if (c.name().equals(test)) {
                return true;
            }
        }
        return false;
    }

    private void findWebtotal(Answear[] allAnswears) {
        int bestAnswear = -1;

        int bestWebtotal = 0;
        for (int i = 0; i < allAnswears.length; i++) {
            for (int j = 0; j < allAnswears.length; j++) {
                if (allAnswears[i].getWebTotal() > bestWebtotal) {
                    bestWebtotal = allAnswears[i].getWebTotal();
                    bestAnswear = i;
                }
            }
        }
        allAnswears[bestAnswear].setPoints();
        for (int i = 0; i < allAnswears.length; i++) {
            allAnswears[i].setIntermediateResult(allAnswears[i].getPoints());
        }
    }

    private void findWordsInWebResult(Answear[] allAnswears) {
        int[] answearPoints = new int[4];
        switch (searchMethod) {
            case Config.GOOGLE:
            case Config.BING:
                for (int i = 0; i < allAnswears.length; i++) {
                    String[] description = allAnswears[i].getDescription();
                    String[] urlTitel = allAnswears[i].getUrlTitel();
                    for (int j = 0; j < description.length; j++) {
                        description[j] = description[j].toLowerCase();
                        answearPoints[i] += countSubstring(allAnswears[i].getTitle().toLowerCase(), description[j]);
                        String title = allAnswears[i].getTitle().toLowerCase();
                        String currentUrl = urlTitel[j].toLowerCase();
                        if (currentUrl.contains(title) && currentUrl.contains("wikipedia")) {
                            allAnswears[i].setWikiFound(true);
                            //allAnswears[i].setPoints();
                           // allAnswears[i].setPoints();
                        }
                    }
                }
                break;
            case Config.WIKI:
                String[] questionsWords = this.question.split(" ");
                for (int i = 0; i < allAnswears.length; i++) {
                    String urlsString = allAnswears[i].getUrls().toLowerCase();
                    for (int j = 0; j < questionsWords.length; j++) {
                        questionsWords[j] = questionsWords[j].toLowerCase();
                        if (!containsWord(questionsWords[j])) {
                            if(urlsString.contains(questionsWords[j])){
                                answearPoints[i] += countSubstring(questionsWords[j], urlsString);
                            }
                        }
                    }
                }
                break;
            //case Config.GOOGLE:
                /*
                for (int i = 0; i < allAnswears.length; i++) {
                    String urlsString = allAnswears[i].getUrls();
                    urlsString = urlsString.toLowerCase();
                    String title = allAnswears[i].getTitle().toLowerCase();
                    answearPoints[i] += countSubstring(title, urlsString);
                    if(urlsString.contains(title + " - " + "wikipedia")){
                        allAnswears[i].setWikiFound(true);
                       // allAnswears[i].setPoints();
                       // allAnswears[i].setPoints();
                    }
                }
                break;*/
        }
        int bestAnswear = -1;
        int bestResult = 0;
        for (int i = 0; i < answearPoints.length; i++) {
           // Log.d(TAG, String.valueOf(answearPoints[i]));
            allAnswears[i].setWordsInWeb(answearPoints[i]);
            if (answearPoints[i] > bestResult) {
                bestAnswear = i;
                bestResult = answearPoints[i];
            }
        }
        if (bestAnswear != -1) {
            allAnswears[bestAnswear].setPoints();
            allAnswears[bestAnswear].setPoints();
        } else {
            System.out.println("Keine potenzielle Antwort gefunden in Textvergleich");
        }
    }

    private String printPoints(Answear[] allAnswears) {
        //Log.d(TAG, "printPoints");
        for (int i = 0; i < allAnswears.length; i++) {
            Log.d(TAG, "Antwort: " + allAnswears[i].getTitle() + " Points:" + allAnswears[i].getPoints());
        }
        int bestResult = 0;
        Answear result = null;
        int countCompleteScore = 0;
        for (int i = 0; i < allAnswears.length; i++) {
            countCompleteScore += allAnswears[i].getPoints();
            if (allAnswears[i].getPoints() > bestResult) {
                bestResult = allAnswears[i].getPoints();
                result = allAnswears[i];
            }
        }
        Log.d(TAG, "countComplete: " + String.valueOf(countCompleteScore));
        int procent = 0;
        if(countCompleteScore != 0) {
            procent = 100 / countCompleteScore * result.getPoints();

        }
        if (result != null) {
            Log.d("Ergebnis", "Antwort: " + result.getTitle() + " Points:" + result.getPoints());
            Toast.makeText(context.getApplicationContext(), result.getTitle() + " "+ procent + "%", Toast.LENGTH_LONG).show();
            Long tsLong = System.currentTimeMillis() / 1000;
            String tsEnd = tsLong.toString();
            Log.d("Timer end", tsEnd);
            SharedPreferences settings = context.getSharedPreferences(Config.SHARED_PREFS_FILE, 0);
            String tsStart = settings.getString(Config.TIMER, "");
            Long time = Long.parseLong(tsEnd) - Long.parseLong(tsStart);
            Log.d("TIMESTAMP", time.toString());
            endtime = time.toString();
        }else{
            result = new Answear("No winner", 0);
        }

        return result.getTitle();
    }

    private JSONObject searchRequestBing(String question, String answear) throws JSONException, IOException, ExecutionException, InterruptedException {
        BingSearchApiEngine getSearch = new BingSearchApiEngine();
        org.json.JSONObject js = null;
        try {
            js = getSearch.execute(question + "" + answear).get();
            //Log.d("jsResult", js.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return js;
    }

    private JSONObject searchRequestGoogleJSON(String question, String answear){
        GoogleAPISearchEngine gase = new GoogleAPISearchEngine();
        JSONObject js = null;
        try {
           js = gase.execute(question + "" + answear).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return js;
    }

    private String searchRequestGoogle(String question, String answear) throws IOException {
        GoogleSearchEngine googleSearchEngine = new GoogleSearchEngine();
        String gs = "wrong";
        try {
            gs = googleSearchEngine.execute(question + "" + answear).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return gs;
    }

    private String searchRequestWiki(String answear) throws Exception {
        WikiSearchEngine wikiSearchEngine = new WikiSearchEngine();
        String ws = null;
        try {
            ws = wikiSearchEngine.execute(answear).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return ws;
    }

    public void appendLog(String question, Answear[] allAnswears, String result) {
        String log = question + "\r\n";
        for (int i = 0; i < allAnswears.length; i++){
            log = log + allAnswears[i].getTitle() + "\t" + allAnswears[i].getWebTotal() + "\t" + allAnswears[i].getWordsInWeb()
                    + "\t" + allAnswears[i].getPoints() + "\t" + allAnswears[i].isWikiFound()
                    + "\r\n";
        }
        log = log + "\r\n" + "Time:\t" + endtime + "\r\n";
        log = log + "\r\n" + "Result:\t" + result + "\r\n";

        File logFile = new File(Environment.getExternalStorageDirectory().getPath() +"/log.file");

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

    public static int countSubstring(String subStr, String str){
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }

    // old stuff
    /*
    /*
        for (int i = 0; i < allAnswears.length; i++){
            String requestAnswear = " \"" + allAnswears[i].getTitle() + "\"";
            webResultJSON = searchRequestBing(apostropheQuestion, requestAnswear);
            org.json.JSONArray web = webResultJSON.getJSONArray("Web");
            if(webResultJSON != null){
                if (webResultJSON.getInt("WebTotal") == 1) {
                    //Does it contain words like quizduell etc.
                    String title = webResultJSON.getJSONArray("Web").getJSONObject(0).get("Title").toString().toLowerCase();
                    String description = webResultJSON.getJSONArray("Web").getJSONObject(0).get("Description").toString().toLowerCase();
                    //TODO make it flexible
                    if (title.contains("quizduell") || description.contains("quizduell")) {
                        countAnswears++;
                        apostropheWorks = true;
                        rightAnswear = allAnswears[i].getTitle();
                        apho++;
                    } else{
                        Log.d(TAG, "Not a special solution site");
                    }
                    if (apostropheWorks) {
                        Log.d(TAG, "Maybe the right answear");
                    }
                } else {
                    Log.d(TAG, "Doesnt work with apostrophe");
                }
            }else{
                Log.d(TAG, "Cant find information");
            }
        }
        if (countAnswears > 1) {
            Log.d(TAG,"More than one answear has just one link");
            apostropheWorks = false;
            rightAnswear = "";
        }
     */
}
