package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    long l_nowTime, l_startTime, l_endTime, l_elapseTime, l_finishTime;
    long l_delayTime = 0;
    String s_nowTime, s_startTime, s_endTime, s_elapseTime, s_finishTime, s_id;
    boolean is_start;
    TextView tv_nowTime, tv_startTime, tv_endTime, tv_elapsedTime, tv_endTimeText, tv_elapsedTimeText;
    private String[] exhibitionState = new String[6];      // 전시관 오픈 여부(1 : open, 0 : close)
    LinearLayout[] ll_ex = new LinearLayout[6];
    ImageView[] iv_ex = new ImageView[6];
    boolean[] is_success = new boolean[6];
    Button bt_toMap, bt_finish;
    boolean surveyState = false;
    String surveyUrl = "";

    TimerHandler timerhandler;
    private static int MESSAGE_TIMER_START = 100;
    private static int REFRESH_TIMER_START = 200;
    private static int NOWTIME_REFRESH_TIMER_START = 199;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 1, 실패 0 반환)
    public static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";          //각 전시관 별 개설 여부(JSON 형식) - ex) { "number": "1", "isOpen": "1" }
    public static final String SET_ISEND = BASE_PATH + "set_isEnd.php";    //전시 종료 값 보내기(성공 1, 실패 0 반환)
    public static final String GET_ISEND = BASE_PATH + "get_isEnd.php";    //전시 종료 여부 받기(종료됨 : 시간, 종료안됨 : 0)
    public static final String GET_SURVEY = BASE_PATH + "get_survey.php";    //전시 종료 여부 받기(종료됨 : 시간, 종료안됨 : 0)
    public static final String GET_PARTICIPATION = BASE_PATH + "get_participation.php";    //0(일반관람), 1(전시해설)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }
        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        loadData();

        tv_nowTime = findViewById(R.id.tv_nowTime);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
        tv_elapsedTime = findViewById(R.id.tv_elapsedTime);
        tv_endTimeText = findViewById(R.id.tv_endTimeText);
        tv_elapsedTimeText = findViewById(R.id.tv_elapsedTimeText);
        ll_ex[0] = findViewById(R.id.layout_ex_1);
        ll_ex[1] = findViewById(R.id.layout_ex_2);
        ll_ex[2] = findViewById(R.id.layout_ex_3);
        ll_ex[3] = findViewById(R.id.layout_ex_4);
        ll_ex[4] = findViewById(R.id.layout_ex_5);
        ll_ex[5] = findViewById(R.id.layout_ex_6);
        iv_ex[0] = findViewById(R.id.iv_ex_1);
        iv_ex[1] = findViewById(R.id.iv_ex_2);
        iv_ex[2] = findViewById(R.id.iv_ex_3);
        iv_ex[3] = findViewById(R.id.iv_ex_4);
        iv_ex[4] = findViewById(R.id.iv_ex_5);
        iv_ex[5] = findViewById(R.id.iv_ex_6);
        bt_toMap = findViewById(R.id.bt_toMap);
        bt_finish = findViewById(R.id.bt_finish); 

        l_startTime = getStartTime();
        getTimeRefresh();
        getTimeData();
        if (!getExhibitionData()) {
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            finish();
        }

        timerhandler = new TimerHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadData();

        String endResult = getEndTime();
        Log.d("CHECK_ENDRESULT", endResult);
        if (endResult.equals("ERROR")) {
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            finish();
        } else if (endResult.equals("0")) {
            if (is_start) {
                timerhandler.sendEmptyMessage(MESSAGE_TIMER_START);
                for (int i = 0; i < 6; i++) {
                    if (exhibitionState[i].equals("0")) {
                        iv_ex[i].setImageResource(R.drawable.closed);
                        ll_ex[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getApplicationContext(), "개방되지 않은 전시관입니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        if (is_success[i]) {
                            iv_ex[i].setImageResource(R.drawable.complete);
                            ll_ex[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "관람 완료된 전시관입니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            iv_ex[i].setImageResource(R.drawable.progress);
                            ll_ex[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "관람하지 않은 전시관입니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
                bt_finish.setText("관람 종료하기");
            } else {
                for (int i = 0; i < 6; i++) {
                    ll_ex[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "관람 시작 전입니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                timerhandler.sendEmptyMessage(REFRESH_TIMER_START);
                bt_finish.setText("관람 시작 전입니다.");
                bt_finish.setEnabled(false);
            }

            if (getFinish()) {
                bt_finish.setEnabled(true);
            } else {
                bt_finish.setEnabled(false);
            }
        } else {
            Toast.makeText(getApplicationContext(), "이미 관람 완료된 사용자입니다.", Toast.LENGTH_SHORT).show();
            timerhandler.sendEmptyMessage(NOWTIME_REFRESH_TIMER_START);
            try {
                SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                l_finishTime = df.parse(endResult).getTime();
                Date finishTime = new Date(l_finishTime);
                SimpleDateFormat sdfFinish = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
                s_finishTime = sdfFinish.format(finishTime);
                l_elapseTime = l_finishTime - l_startTime - 32400000;
                Date elapseDate = new Date(l_elapseTime);
                SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
                s_elapseTime = sdfElapse.format(elapseDate);
                Log.d("ELAPSE TIME", s_elapseTime);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_endTimeText.setText("관람 종료 시간");
                        tv_endTime.setText(s_finishTime);
                        tv_elapsedTimeText.setText("총 관람 시간");
                        tv_elapsedTime.setText(s_elapseTime);
                        bt_finish.setText("관람 종료되었습니다");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 6; i++) {
                iv_ex[i].setImageResource(R.drawable.complete);
                ll_ex[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "관람 완료된 전시관입니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            bt_finish.setEnabled(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        timerhandler.removeMessages(MESSAGE_TIMER_START);
        timerhandler.removeMessages(REFRESH_TIMER_START);
        timerhandler.removeMessages(NOWTIME_REFRESH_TIMER_START);
    }


    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_toMap :
                if (getStartState()) {
                    if (is_start) {
                        Intent intent = new Intent(CheckActivity.this, NormalActivity.class);
                        intent.putExtra("Time", l_startTime);
                        startActivity(intent);
                    } else {
                        CheckActivity.participationData participationdata = new CheckActivity.participationData(this);
                        try {
                            String result = participationdata.execute(GET_PARTICIPATION, s_id).get();
                            Log.d("REGIST", result);
                            if (result.equals("-1")) {
                                Toast.makeText(CheckActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            } else if (result.equals("ERROR")) {
                                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                            } else {
                                if(result.equals("0")) {
                                    Intent intent = new Intent(CheckActivity.this, HelpNorActivity.class);
                                    startActivity(intent);
                                }
                                else if(result.equals("1")){
                                    Intent intent = new Intent(CheckActivity.this, HelpComActivity.class);
                                    startActivity(intent);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {    // 네트워크 통신 오류 예외처리
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
//                finish();
                break;
            case R.id.bt_finish :
                if (getFinish()) {
                    if (setFinish()) {
                        surveyUrl = getSurveyUrl();
                        if (surveyUrl.equals("ERROR")) {
                            Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else if (!surveyUrl.equals("0")) {
                            Intent in_survey = new Intent(CheckActivity.this, PopupSurveyActivity.class);
                            in_survey.putExtra("URL", surveyUrl);
                            startActivityForResult(in_survey, 0);
                        }
                        else if (surveyUrl.equals("0")) {
                            Intent intent = new Intent(CheckActivity.this, ConfirmActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "아직 관람이 끝나지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 설문조사 팝업 닫혔을 때
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(CheckActivity.this, ConfirmActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public String getEndTime() {
        FinishTask finishTask = new FinishTask(this);
        try {
            return finishTask.execute(GET_ISEND, s_id).get();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public void getTimeData() {
        if (!is_start) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_startTime.setText("관람 시작 전입니다.");
                    tv_endTime.setText("");
                    tv_elapsedTime.setText("");
                }
            });
        } else {
            Date startDate = new Date(l_startTime);
            SimpleDateFormat sdfStart = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            s_startTime = sdfStart.format(startDate);
            Log.d("START TIME", s_startTime);

            l_endTime = l_startTime + 7200000 + l_delayTime;
            Date endDate = new Date(l_endTime);
            SimpleDateFormat sdfEnd = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            s_endTime = sdfEnd.format(endDate);
            Log.d("END TIME", s_endTime);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_startTime.setText(s_startTime);
                    tv_endTime.setText(s_endTime);
                }
            });
        }
    }

    public void nowTimeRefresh() {
        getNowTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_nowTime.setText(s_nowTime);
            }
        });
    }

    public void getTimeRefresh() {
//        getNowTime();
        nowTimeRefresh();

        if (is_start) {
            l_elapseTime = l_nowTime - l_startTime - 32400000;
            Date elapseDate = new Date(l_elapseTime);
            SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            s_elapseTime = sdfElapse.format(elapseDate);
            Log.d("ELAPSE TIME", s_elapseTime);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tv_nowTime.setText(s_nowTime);
                    tv_elapsedTime.setText(s_elapseTime);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tv_nowTime.setText(s_nowTime);
                    tv_elapsedTime.setText("");
                }
            });
        }
    }

    public void getNowTime() {
        l_nowTime = System.currentTimeMillis();
        Date nowDate = new Date(l_nowTime);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        s_nowTime = sdfNow.format(nowDate);
        Log.d("CURRENT TIME", s_nowTime);
    }

    // 3초 단위로 시작 여부 서버에서 받아옴
    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if  (msg.what == MESSAGE_TIMER_START) {
                getTimeRefresh();
                this.sendEmptyMessageDelayed(MESSAGE_TIMER_START, 1000);
            }
            else if (msg.what == REFRESH_TIMER_START) {
                if (getStartState()) {
                    if (is_start) {
                        timerhandler.removeMessages(REFRESH_TIMER_START);
                        onPause();
                        l_startTime = getStartTime();
                        getTimeData();
                        onResume();
                        Log.d("REFRESH", "RUNNING");
                    } else {
                        this.sendEmptyMessageDelayed(REFRESH_TIMER_START, 1000);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (msg.what == NOWTIME_REFRESH_TIMER_START) {
                nowTimeRefresh();
                this.sendEmptyMessageDelayed(NOWTIME_REFRESH_TIMER_START, 1000);
            }
        }
    }

    public boolean getStartState() {
        GetIsStartTask startTask = new GetIsStartTask(CheckActivity.this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            if (result.equals("ERROR")) {
                return false;
            } else {
                is_start = !result.equals("0");
                getTimeRefresh();
                Log.d("ISSTART", Boolean.toString(is_start));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getFinish() {
        boolean state = false;

        if (is_start) {
            if (l_endTime <= l_nowTime) {
                state = true;
            }
            for (int i = 0; i < 6; i++) {
                if (exhibitionState[i].equals("1")) {
                    if (!is_success[i]) {
                        state = false;
                        break;
                    }
                }
            }
        }
        return state;
    }

    public void loadData() {
        s_id = infoData.getString("ID", "");
        for (int i = 1; i < 7; i++) {
            is_success[i-1] = infoData.getBoolean("IS_CHECK_" + i, false);
        }
    }

    /* DB-서버 통신 파트 */
    // 관람 시작이 되었는지 여부 받아오는 메소드
    public long getStartTime() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            is_start = !result.equals("0");
            if (result.equals("ERROR")) {
                is_start = false;
            }
            if (is_start) {
                SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                long time = sdfStart.parse(result).getTime();
                Log.d("ISSTART", Long.toString(time));
                return time;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 관람 종료 설정하는 메소드
    public boolean setFinish() {
        FinishTask finishTask = new FinishTask(this);
        try {
            String result = finishTask.execute(SET_ISEND, s_id).get();
            if (result.equals("0")) {
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if (result.equals("1")) {
                Toast.makeText(getApplicationContext(), "관람 종료 확인이 되었습니다.", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // DB 전시관 데이터 받아오기
    public boolean getExhibitionData() {
        // 전시관 오픈 여부
        GetExhibitionTask task = new GetExhibitionTask(this);
        try {
            String result = task.execute(GET_EXHIBITION).get();
            Log.d("Exhibition", result);
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            Log.d("ARRAY LENGTH", Integer.toString(jArray.length()));
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                exhibitionState[i] = jObject.getString("isOpen");
                Log.d("EXHIBITION", i + " : " + exhibitionState[i]);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // 설문조사 주소 받아오기
    public String getSurveyUrl() {
        // 전시관 오픈 여부
        GetSurveyTask task = new GetSurveyTask(this);
        try {
            String result = task.execute(GET_SURVEY).get();
            Log.d("SURVEY", result);
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            JSONObject jObject = jArray.getJSONObject(0);
            surveyState = jObject.getString("is_exist").equals("1");
            if (surveyState) {
                return jObject.getString("url");
            } else {
                return "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
    /***** 서버 통신 *****/
    // 관람 시작 여부 받아오는 부분
    public static class GetIsStartTask extends AsyncTask<String, Void, String> {
        private WeakReference<CheckActivity> activityReference;
        ProgressDialog progressDialog;

        GetIsStartTask(CheckActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
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
                return "ERROR";
            }
        }
    }

    // 전시관 오픈 여부 받아오는 부분
    public static class GetExhibitionTask extends AsyncTask<String, Void, String> {
        private WeakReference<CheckActivity> activityReference;
        ProgressDialog progressDialog;

        GetExhibitionTask(CheckActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            /*출력값*/
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
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

    // 관람 종료시 데이터 전송, 관람 종료 여부 정보 받아오기
    public static class FinishTask extends AsyncTask<String, Void, String> {
        private WeakReference<CheckActivity> activityReference;
        ProgressDialog progressDialog;

        FinishTask(CheckActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
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
                return "ERROR";
            }
        }
    }

    // 설문조사 여부 및 주소 받아오기
    public static class GetSurveyTask extends AsyncTask<String, Void, String> {
        private WeakReference<CheckActivity> activityReference;
        ProgressDialog progressDialog;

        GetSurveyTask(CheckActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
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
                return "ERROR";
            }
        }
    }
    public static class participationData extends AsyncTask<String, Void, String> {
        private WeakReference<CheckActivity> activityReference;
        ProgressDialog progressDialog;

        participationData(CheckActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
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
                return "ERROR";
            }
        }
    }
}
