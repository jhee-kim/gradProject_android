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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private boolean is_correct = false;
    private ArrayList<String> personalData = new ArrayList<>(8);  // [0]:id, [1]:number, [2]:name, [3]:participation, [4]:division, [5]:temper, [6]:phone, [7]:destination

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    public static final String GET_AUDIENCE = BASE_PATH + "get_audience.php";             //로그인(성공 id, 실패 0 반환)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void onClick(View v) {
        Button bt_login = findViewById(R.id.bt_login);
        Button bt_registration = findViewById(R.id.bt_registration);
        if (v == bt_login) {
            EditText et_name = findViewById(R.id.et_login_name);
            EditText et_number = findViewById(R.id.et_login_number);
            String input_name = et_name.getText().toString();
            String input_number = et_number.getText().toString();

            if (input_name.equals("") || input_number.equals("")) {
                Toast.makeText(getApplicationContext(), "정보를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show();
            } else {
                LoginTask task = new LoginTask(this);
                try {
                    String result = task.execute(GET_AUDIENCE, input_number, input_name).get();
                    if(result.equals("0")) {
                        is_correct = false;
                    } else {
                        try {
                            personalData.clear();
                            JSONObject jResult = new JSONObject(result);
                            JSONArray jArray = jResult.getJSONArray("result");
                            JSONObject jObject = jArray.getJSONObject(0);
                            personalData.add(jObject.getString("id"));
                            personalData.add(jObject.getString("number"));
                            personalData.add(jObject.getString("name"));
                            personalData.add(jObject.getString("participation"));
                            personalData.add(jObject.getString("division"));
                            personalData.add(jObject.getString("temper"));
                            personalData.add(jObject.getString("phone"));
                            personalData.add(jObject.getString("destination"));
                            is_correct = true;
                            for (int i = 0; i < 8; i++) {
                                Log.d("EXHIBITION", personalData.get(i));
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (is_correct) {
                    Intent in_login = new Intent(LoginActivity.this, MainActivity.class);
                    setResult(RESULT_OK, in_login);
                    SharedPreferences.Editor editor = infoData.edit();
                    editor.putBoolean("IS_AUTOLOGIN", true);
                    editor.putString("ID", personalData.get(0));
                    editor.putString("NUMBER", personalData.get(1));
                    editor.putString("NAME", personalData.get(2));
                    editor.putString("PARTICIPATION", personalData.get(3));
                    editor.putString("DIVISION", personalData.get(4));
                    editor.putString("TEMPER", personalData.get(5));
                    editor.putString("PHONE", personalData.get(6));
                    editor.putString("DESTINATION", personalData.get(7));
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (v == bt_registration) {
            Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    /***** 서버 통신 *****/
    public static class LoginTask extends AsyncTask<String, Void, String> {
        private WeakReference<LoginActivity> activityReference;
        ProgressDialog progressDialog;

        LoginTask(LoginActivity context) {
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
            String number = params[1];
            String name = params[2];
            String postParameters = "&number=" + number + "&name=" + name;
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
