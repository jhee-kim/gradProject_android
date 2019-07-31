package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopupTimeActivity extends Activity {
    private Chronometer mChronometer;
    Long startDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        startDate = intent.getLongExtra("Time", 0);

        setContentView(R.layout.popup_time);
        //mChronometer.setTextColor(Color.RED);

        mChronometer = (Chronometer)findViewById(R.id.chronometer);
        //time 초기화
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long now = System.currentTimeMillis() - 9*60*60*1000;
                long time = now - startDate;
                //if(time >= 2*60*60*1000) {mChronometer.setTextColor(Color.RED);}
                cArg.setText(DateFormat.format("관람시간 kk:mm:ss", time));
            }
        });
        mChronometer.start();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        getWindow().setLayout((int)(width*0.95), (int)(height * 0.2));
    }
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }
}
