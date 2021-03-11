package com.paul623.android.imageaiclassification;

/**
 * Created by Android Studio.
 * User: paul623
 * Date: 2021/3/9
 * Time: 15:50
 * Email:zhubaoluo@outlook.com
 */
public class TimeCounter {
    public long startMills;
    public long totoalMills;
    public void start(){
        startMills=System.nanoTime();
    }
    public String stop(){
        String aa="";
        totoalMills=System.nanoTime() - startMills;
        long mills=totoalMills/1000000000;
        if(mills>=60){
            long minute=mills/60;
            aa=minute+" minute(s) "+mills%60+" second(s)";
        }else {
            aa=mills+" second(s)";
        }
        return aa;
    }
}
