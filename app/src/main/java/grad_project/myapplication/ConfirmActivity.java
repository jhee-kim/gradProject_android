package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class ConfirmActivity extends AppCompatActivity {
    String s_id, s_name, s_number, s_phone, s_temper, s_destination, s_participation, s_division, s_startDate;
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

        SharedPreferences infoData;
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");
        if (getStartState()) {
            if(getUserData()) {
                tv_participation = findViewById(R.id.tv_participation);
                tv_name = findViewById(R.id.tv_name);
                tv_start = findViewById(R.id.tv_start);
                tv_date1 = findViewById(R.id.tv_date1);
                tv_date2 = findViewById(R.id.tv_date2);
                if (s_number.length() > 3) {
                    s_number = s_number.substring(0, 2) + "-" + s_number.substring(2);
                }
                String temp_name = s_name + "\n(" + s_number + ")";
                tv_name.setText(temp_name);
                switch (s_participation) {
                    case "0":
                        tv_participation.setText("전시 관람");
                        break;
                    case "1":
                        tv_participation.setText("전시 해설");
                        break;
                    default:
                        tv_participation.setText("E");
                }
                tv_start.setText(s_startDate);
                tv_date1.setText(DateFormat.format("yyyy년 MM월 dd일", Calendar.getInstance(Locale.KOREA)));
                tv_date2.setText(DateFormat.format("yyyy. MM. dd", Calendar.getInstance(Locale.KOREA)));
            } else {    // 네트워크 통신 오류 예외처리
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {    // 네트워크 통신 오류 예외처리
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void onDownload(View v) {
        findViewById(R.id.content_confirm).setDrawingCacheEnabled(true);
        findViewById(R.id.content_confirm).buildDrawingCache();
        Bitmap saveBitmap =  findViewById(R.id.content_confirm).getDrawingCache();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss");

        String namePostfix = format.format(new Date());
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String basePath = sdPath + File.separator;
        File dir = new File(basePath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File saveFile = new File(basePath + File.separator + "foru_" + namePostfix + ".jpg");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(saveFile);
            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);
            Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_SHORT).show();
        } finally {
            if(output!=null) { try{output.close();}catch(Exception e){e.printStackTrace();}}
        }
    }

    public void onShare(View v) {

    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    // DB에서 데이터 새로 받아오는 메소드
    public boolean getUserData() {
        GetUserData task = new GetUserData(this);
        try {
            String result = task.execute(GET_AUDIENCE, s_id).get();
            if (result.equals("ERROR")) {
                return false;
            } else {
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
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /* DB-서버 통신 파트 */
    // 관람 시작이 되었는지 여부 받아오는 메소드
    public boolean getStartState() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            Log.d("CONFIRM result", result);
            if (result.equals("ERROR")) {
                return false;
            } else if (!result.equals("0")) {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(result);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\n(HH:mm)", Locale.KOREA);
                s_startDate = sdf.format(date);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CONNECTION ERROR", "start state");
        }
        return false;
    }

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
                return "ERROR";
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
                return "ERROR";
            }
        }
    }
}