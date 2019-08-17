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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

public class RegistActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private String s_id;
    private String s_name = "";
    private int i_division = -1;
    private int i_participation = -1;
    private String s_temper = "";
    private String s_number = "";
    private String s_number_0 = "";
    private String s_number_1 = "";
    private String s_phone = "";
    private String s_phone_0 = "";
    private String s_phone_1 = "";
    private String s_phone_2 = "";
    private String s_destination = "";
    EditText et_name, et_temper, et_number_1, et_phone_1, et_phone_2, et_destination;
    Spinner sp_number_0, sp_phone_0, sp_division;
    CheckBox cb_check;
    boolean check_temp;
    RadioGroup rg_participation, rg_division;
    LinearLayout ll_agreement;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    public static final String ADD_AUDIENCE = BASE_PATH + "add_audience.php";            //관람등록(성공 1, 실패 0 반환)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        et_name = findViewById(R.id.et_name);
        et_temper = findViewById(R.id.et_temper);
        sp_number_0 = findViewById(R.id.sp_number_0);
        et_number_1 = findViewById(R.id.et_number_1);
        sp_phone_0 = findViewById(R.id.sp_phone_0);
        et_phone_1 = findViewById(R.id.et_phone_1);
        et_phone_2 = findViewById(R.id.et_phone_2);
        et_destination = findViewById(R.id.et_destination);
        cb_check = findViewById(R.id.cb_check);
        sp_division = findViewById(R.id.sp_division);
        rg_participation = findViewById(R.id.rg_participation);
        ll_agreement = findViewById(R.id.ll_agreement);

        check_temp = false;
        cb_check.setChecked(false);

        sp_number_0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_number_0 = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        sp_phone_0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_phone_0 = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sp_division.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                i_division = position;
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ll_agreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistActivity.this, PopupAgreementActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        cb_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check_temp) {
                    cb_check.setChecked(false);
                    Intent intent = new Intent(RegistActivity.this, PopupAgreementActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(getApplicationContext(), "개인정보 수집 및 이용에 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                    check_temp = false;
                }
            }
        });

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
        int participation_selected = rg_participation.getCheckedRadioButtonId();

        s_name = et_name.getText().toString().trim();
        s_temper = et_temper.getText().toString().trim();
        s_number_1 = et_number_1.getText().toString().trim();
        s_number = s_number_0 + s_number_1;
        s_phone_1 = et_phone_1.getText().toString().trim();
        s_phone_2 = et_phone_2.getText().toString().trim();
        s_phone = s_phone_0 + s_phone_1 + s_phone_2;
        s_destination = et_destination.getText().toString().trim();
        boolean b_check = cb_check.isChecked();
        String temp_phone = s_phone_0 + "-" + s_phone_1 + "-" + s_phone_2;
        String temp_number = s_number_0 + "-" + s_number_1;

        switch (participation_selected) {
            case R.id.rb_normal:
                i_participation = 0;
                break;
            case R.id.rb_narr:
                i_participation = 1;
                break;
            default:
                i_participation = -1;
                break;
        }

        Log.i("NAME", s_name);
        Log.i("DIVISION", Integer.toString(i_division));
        Log.i("TEMPER", s_temper);
        Log.i("NUMBER", s_number);
        Log.i("PHONE", s_phone);

        if (s_name.equals("")) {
            Toast.makeText(RegistActivity.this,"이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_name.requestFocus();
        } else if (s_number.equals("")) {
            Toast.makeText(RegistActivity.this,"군번을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_number_1.requestFocus();
        } else if (s_phone.equals("")) {
            Toast.makeText(RegistActivity.this,"휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_phone_1.requestFocus();
        } else if (i_participation == -1) {
            Toast.makeText(RegistActivity.this, "관람 방법을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if(i_division == -1) {
            Toast.makeText(RegistActivity.this,"구분을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if (s_temper.equals("")) {
            Toast.makeText(RegistActivity.this,"소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
        } else if (s_destination.equals("")) {
            Toast.makeText(RegistActivity.this,"행선지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
        } else if (!b_check) {
            Toast.makeText(RegistActivity.this,"개인정보 수집 및 이용에 동의해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RegistActivity.this, PopupRegistActivity.class);
            intent.putExtra("NAME", s_name);
            intent.putExtra("NUMBER", temp_number);
            intent.putExtra("PHONE", temp_phone);
            intent.putExtra("DIVISION", i_division);
            intent.putExtra("TEMPER", s_temper);
            intent.putExtra("DESTINATION", s_destination);
            intent.putExtra("PARTICIPATION", i_participation);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 입력된 정보 최종 확인이 되었을 경우
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = infoData.edit();
                InsertData task = new InsertData(this);
                try {
                    String result = task.execute(ADD_AUDIENCE, s_number, s_name, Integer.toString(i_participation), Integer.toString(i_division), s_temper, s_phone, s_destination).get();
                    if (result.equals("0")) {
                        Toast.makeText(RegistActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    } else {
                        s_id = result;
                        Toast.makeText(RegistActivity.this, "등록 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                        editor.putBoolean("IS_AUTOLOGIN", true);
                        editor.putString("ID", s_id);
                        editor.putString("NAME", s_name);
                        editor.apply();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 개인정보 수집 활용 동의/미동의시
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                cb_check.setChecked(true);
                check_temp = true;
            }
            else if (resultCode == RESULT_CANCELED) {
                cb_check.setChecked(false);
                check_temp = false;
            }
        }
    }

    /***** 서버 통신 *****/
    public static class InsertData extends AsyncTask<String, Void, String> {
        private WeakReference<RegistActivity> activityReference;
        ProgressDialog progressDialog;

        InsertData(RegistActivity context) {
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
            String participation = params[3];
            String division = params[4];
            String temper = params[5];
            String phone = params[6];
            String destination = params[7];
            String postParameters = "&number=" + number + "&name=" + name + "&participation=" + participation + "&division=" + division + "&temper=" + temper + "&phone=" + phone + "&destination=" + destination;
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
