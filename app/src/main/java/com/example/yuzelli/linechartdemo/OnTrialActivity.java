package com.example.yuzelli.linechartdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;


/**
 * Created by 51644 on 2017/7/13.
 */

public class OnTrialActivity extends Activity {
//    @BindView(R.id.textView3)
//    TextView textView3;
//    @BindView(R.id.textView4)
//    TextView textView4;

    private TextView textView3;
    private TextView textView4;
    String time = "2017-08-25 12:00:00";

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_ontail);
        textView3.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(OtherUtils.date2TimeStamp(time)> System.currentTimeMillis()/1000){
                    startActivity(new Intent(OnTrialActivity.this,MainActivity.class));
                    finish();
                }else {
                    textView4.setText("已过期");
                }
            }
        },3*1000);
    }




}
