package grad_project.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class HelpActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    TimerHandler timerhandler;
    private static int MESSAGE_TIMER_START = 100;
    private boolean is_start = false;
    private String s_id;
    Long startDate;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 1, 실패 0 반환)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        TextView tv = (TextView)findViewById(R.id.tv_red);
        String str = "● 지참물 : 휴가증(외박증, 외출증 미적용)";
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

    // 3초 단위로 시작 여부 서버에서 받아옴
    // 관람 시작되었을 경우 자동으로 맵 액티비티로 넘어감
    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if  (msg.what == MESSAGE_TIMER_START) {
                GetIsStartTask startTask = new GetIsStartTask(HelpActivity.this);
                try {
                    String result = startTask.execute(GET_ISSTART, s_id).get();
                    is_start = !result.equals("0");
                    if (is_start) {
                        SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        startDate = df.parse(result).getTime();
                        Intent intent = new Intent(HelpActivity.this, NormalActivity.class);
                        intent.putExtra("Time", startDate);
                        startActivity(intent);
                        finish();
                    }
                    Log.d("ISSTART", Boolean.toString(is_start));
                    Log.d("ISSTART", String.valueOf(startDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                this.sendEmptyMessageDelayed(MESSAGE_TIMER_START, 3000);
            }
        }
    }

    // 관람 시작 여부 받아오는 부분
    public static class GetIsStartTask extends AsyncTask<String, Void, String> {
        private WeakReference<HelpActivity> activityReference;

        GetIsStartTask(HelpActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            /*출력값*/
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String id = params[1];
            String postParameters = "&id=" + id;
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                int responseStatusCode = httpURLConnection.getResponseCode();
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
}
