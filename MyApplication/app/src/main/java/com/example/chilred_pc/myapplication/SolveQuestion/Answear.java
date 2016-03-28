/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.chilred_pc.myapplication.SolveQuestion;

/**
 *
 * @author Chilred-pc
 */
public class Answear {
    private String title;
    private int points;
    private int webTotal;
    private String urls;
    private String[] urlTitel;
    private String[] description;
    private int intermediateResult = 0;
    private int wordsInWeb = 0;
    private boolean wikiFound = false;

    public Answear(String title, int points) {
        this.title = title;
        this.points = points;
    }
    
    public void setNewUrlStringDescription(int urlLen, int desLen){
        urlTitel = new String[urlLen];
        description = new String[desLen];
    }
    
    public void setDescription(String description, int pos) {
        this.description[pos] = description;
    }

    public String[] getDescription() {
        return description;
    }

    public void setWebTotal(int webTotal) {
        this.webTotal = webTotal;
    }

    public String[] getUrlTitel() {
        return urlTitel;
    }

    public void setUrlTitel(String urlTitel, int pos) {
        this.urlTitel[pos] = urlTitel;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getTitle() {
        return title;
    }

    public void setPoints() {
        this.points++;
    }

    public int getPoints() {
        return points;
    }

    public int getWebTotal() {
        return webTotal;
    }

    public String getUrls() {
        return urls;
    }

    public int getIntermediateResult() {return intermediateResult;}

    public void setIntermediateResult(int intermediateResult) {
        this.intermediateResult = intermediateResult;
    }

    public int getWordsInWeb() {return wordsInWeb;}

    public void setWordsInWeb(int wordsInWeb) {this.wordsInWeb = wordsInWeb;}

    public boolean isWikiFound() {
        return wikiFound;
    }

    public void setWikiFound(boolean wikiFound) {
        this.wikiFound = wikiFound;
    }
}
