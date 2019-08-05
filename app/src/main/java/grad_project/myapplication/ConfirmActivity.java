package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;

public class ConfirmActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    String s_id;
    String s_name;
    String s_number;
    String s_phone;
    String s_temper;
    String s_destination;
    String s_participation;
    String s_division;
    String s_startDate;
    TextView tv_participation, tv_name, tv_start, tv_date1, tv_date2;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    public static final String GET_AUDIENCE = BASE_PATH + "get_audience.php";             //사용자 정보 데이터 가져오기
    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 시작 시간, 실패 0 반환)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
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
        s_id = infoData.getString("ID", "");
        getUserData();
        getStartState();
        SharedPreferences.Editor editor = infoData.edit();
        editor.putString("NAME", s_name);
        editor.apply();

        tv_participation = findViewById(R.id.tv_participation);
        tv_name = findViewById(R.id.tv_name);
        tv_start = findViewById(R.id.tv_start);
        tv_date1 = findViewById(R.id.tv_date1);
        tv_date2  = findViewById(R.id.tv_date2);

        s_number = s_number.substring(0, 2) + "-" + s_number.substring(3, s_number.length());
        tv_name.setText(s_name+"\n("+s_number+")");
        switch (s_participation) {
            case "0":
                tv_participation.setText("전시 관람");
                break;
            case "1":
                tv_participation.setText("전시 해설");
                break;
            default :
                tv_participation.setText("E");
        }
        tv_start.setText(s_startDate);
        tv_date1.setText(DateFormat.format("yyyy년 MM월 dd일", Calendar.getInstance(Locale.KOREA)));
        tv_date2.setText(DateFormat.format("yyyy. MM. dd", Calendar.getInstance(Locale.KOREA)));
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }
    // DB에서 데이터 새로 받아오는 메소드
    public void getUserData() {
        GetUserData task = new GetUserData(this);
        try {
            String result = task.execute(GET_AUDIENCE, s_id).get();
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            JSONObject jObject = jArray.getJSONObject(0);
            s_number = jObject.getString("number");
            s_name = jObject.getString("name");
            s_phone = jObject.getString("phone");
            s_participation = jObject.getString("participation");
            s_division = jObject.getString("division");
            s_temper = jObject.getString("temper");
            s_destination = jObject.getString("destination");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* DB-서버 통신 파트 */
    // 관람 시작이 되었는지 여부 받아오는 메소드
    public void getStartState() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            s_startDate = startTask.execute(GET_ISSTART, s_id).get();
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s_startDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\n(HH:mm)");
            s_startDate = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    // 저장된 값 가져오기
//    private void loadInfo() {
//        s_number = infoData.getString("NUMBER", "");
//        s_name = infoData.getString("NAME", "");
//        s_phone = infoData.getString("PHONE", "");
//        s_division = infoData.getString("DIVISION", "");
//        s_temper = infoData.getString("TEMPER", "");
//        s_destination = infoData.getString("DESTINATION", "");
//        s_participation = infoData.getString("PARTICIPATION", "");
//    }

    /***** 서버 통신 *****/
    // 사용자 데이터 받아오는 부분
    public static class GetUserData extends AsyncTask<String, Void, String> {
        private WeakReference<ConfirmActivity> activityReference;
        ProgressDialog progressDialog;

        GetUserData(ConfirmActivity context) {
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
                return "Error: " + e.getMessage();
            }
        }
    }
    /***** 서버 통신 *****/
    // 관람 시작 여부 받아오는 부분
    public static class GetIsStartTask extends AsyncTask<String, Void, String> {
        private WeakReference<ConfirmActivity> activityReference;
        ProgressDialog progressDialog;

        GetIsStartTask(ConfirmActivity context) {
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
                return "Error: " + e.getMessage();
            }
        }
    }
}