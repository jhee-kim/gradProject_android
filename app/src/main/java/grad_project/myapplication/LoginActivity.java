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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private boolean is_correct = false;
    private String s_id, input_name;
    Button bt_login, bt_registration;
    Spinner sp_number_0;
    EditText et_name, et_number_1;
    String s_number_0, s_number_1;
    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    public static final String GET_ID = BASE_PATH + "login.php";             //로그인(성공 id, 실패 0 반환)

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

        bt_login = findViewById(R.id.bt_login);
        bt_registration = findViewById(R.id.bt_registration);
        et_name = findViewById(R.id.et_login_name);
        sp_number_0 = findViewById(R.id.sp_login_number_0);
        et_number_1 = findViewById(R.id.et_login_number_1);

        sp_number_0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_number_0 = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void onClick(View v) {
        if (v == bt_login) {
            if (tryLogin()) {
                if (is_correct) {
                    Intent in_login = new Intent(LoginActivity.this, MainActivity.class);
                    setResult(RESULT_OK, in_login);
                    SharedPreferences.Editor editor = infoData.edit();
                    editor.putBoolean("IS_LOGIN", true);
                    editor.putString("ID", s_id);
                    editor.putString("NAME", input_name);
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {    // 네트워크 통신 오류 예외처리
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            }
        }
        if (v == bt_registration) {
            Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    public boolean tryLogin() {
        input_name = et_name.getText().toString();
        s_number_1 = et_number_1.getText().toString();

        String input_number = s_number_0 + s_number_1;
        Log.d("INPUT NUMBER", input_number);
        if (input_name.equals("") || input_number.equals("")) {
            Toast.makeText(getApplicationContext(), "정보를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            LoginTask task = new LoginTask(this);
            try {
                String result = task.execute(GET_ID, input_number, input_name).get();
                Log.d("LOGIN RESULT", result);
                if (result.equals("ERROR")) {
                    return false;
                } else if (result.equals("0")) {
                    is_correct = false;
                } else {
                    s_id = result;
                    is_correct = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
                return "ERROR";
            }
        }
    }
}
