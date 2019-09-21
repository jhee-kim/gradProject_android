package grad_project.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class HelpNorActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    TimerHandler timerhandler;
    private static int MESSAGE_TIMER_START = 100;
    private boolean is_start = false;
    private String s_id;
    Long startDate;

    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpnor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        TextView tv = findViewById(R.id.tv_red);
        String str = "● 지참물 : 휴가증 필참\n                 (육군은 외박, 외출 제외)";
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 8, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");

        timerhandler = new TimerHandler();
        timerhandler.sendEmptyMessage(MESSAGE_TIMER_START);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timerhandler.removeMessages(MESSAGE_TIMER_START);

    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    // 10초 단위로 시작 여부 서버에서 받아옴
    // 관람 시작되었을 경우 자동으로 맵 액티비티로 넘어감
    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if  (msg.what == MESSAGE_TIMER_START) {
                DdConnect dbConnect = new DdConnect(context);
                try {
                    String result = dbConnect.execute(DdConnect.GET_ISSTART, s_id).get();
                    Log.d("GET_ISSTART", result);
                    if(result.equals("-1")) {
                        Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        is_start = !result.equals("0");
                        if (is_start) {
                            SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            startDate = df.parse(result).getTime();
                            Intent intent = new Intent(HelpNorActivity.this, NormalActivity.class);
                            intent.putExtra("Time", startDate);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
                this.sendEmptyMessageDelayed(MESSAGE_TIMER_START, 10000);
            }
        }
    }
}