package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class PersonalActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    String s_id, s_name, s_number, s_phone, s_temper, s_destination, s_participation, s_division;
    TextView tv_name, tv_number, tv_phone, tv_destination, tv_participation, tv_division, tv_temper;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    public static final String GET_AUDIENCE = BASE_PATH + "get_audience.php";             //사용자 정보 데이터 가져오기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

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
        SharedPreferences.Editor editor = infoData.edit();
        editor.putString("NAME", s_name);
        editor.apply();

        tv_name = findViewById(R.id.tv_name);
        tv_number = findViewById(R.id.tv_number);
        tv_phone = findViewById(R.id.tv_phone);
        tv_division = findViewById(R.id.tv_division);
        tv_temper = findViewById(R.id.tv_temper);
        tv_destination = findViewById(R.id.tv_destination);
        tv_participation = findViewById(R.id.tv_participation);

        tv_name.setText(s_name);
        tv_number.setText(s_number);
        tv_phone.setText(s_phone);
        String str_temper = "";
        switch (s_division) {
            case "0" :
                str_temper += "육군 / ";
                break;
            case "1" :
                str_temper += "해군 / ";
                break;
            case "2" :
                str_temper += "공군 /  ";
                break;
            case "3" :
                str_temper += "해병대 / ";
                break;
            default :
                str_temper += "E / ";
        }
        str_temper += s_temper;
        tv_temper.setText(str_temper);
        tv_destination.setText(s_destination);
        switch (s_participation) {
            case "0":
                tv_participation.setText("일반 관람");
                break;
            case "1":
                tv_participation.setText("전시 해설");
                break;
            default :
                tv_participation.setText("E");
        }
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
        private WeakReference<PersonalActivity> activityReference;
        ProgressDialog progressDialog;

        GetUserData(PersonalActivity context) {
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
