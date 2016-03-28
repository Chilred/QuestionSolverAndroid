package com.example.chilred_pc.myapplication;

/**
 * Created by Chilred-pc on 15.03.2016.
 */
public class Question {
    String question;
    String[] answear = new String[4];

    public Question(String question, String answear1, String answear2, String answear3, String answear4){
        this.question = question;
        this.answear[0]= answear1;
        this.answear[1]= answear2;
        this.answear[2]= answear3;
        this.answear[3]= answear4;
    }

    public String getQuestion() {return this.question;}

    public String[] getAnswear() {return this.answear;}
}
