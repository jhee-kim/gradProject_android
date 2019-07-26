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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static grad_project.myapplication.MainActivity.ADD_AUDIENCE;

public class RegistActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private String s_name = "";
    private int i_division = -1;
    private int i_participation = -1;
    private String s_temper = "";
    private String s_number = "";
    private String s_phone = "";
    private String s_destination = "";
    private boolean b_check = false;
    EditText et_name, et_temper, et_number, et_phone, et_destination;
    CheckBox cb_check;
    RadioGroup rg_participation, rg_division;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        et_name = findViewById(R.id.et_name);
        et_temper = findViewById(R.id.et_temper);
        et_number = findViewById(R.id.et_number);
        et_phone = findViewById(R.id.et_phone);
        et_destination = findViewById(R.id.et_destination);
        cb_check = findViewById(R.id.cb_check);

        rg_participation = findViewById(R.id.rg_participation);
        rg_division = findViewById(R.id.rg_division);

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
        int division_selected = rg_division.getCheckedRadioButtonId();

        s_name = et_name.getText().toString().trim();
        s_temper = et_temper.getText().toString().trim();
        s_number = et_number.getText().toString().trim();
        s_phone = et_phone.getText().toString().trim();
        s_destination = et_destination.getText().toString().trim();
        b_check = cb_check.isChecked();

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

        switch (division_selected) {
            case R.id.rb_army:
                i_division = 0;
                break;
            case R.id.rb_airforce:
                i_division = 2;
                break;
            case R.id.rb_navy:
                i_division = 1;
                break;
            case R.id.rb_mc:
                i_division = 3;
                break;
            default:
                i_division = -1;
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
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_number.equals("")) {
            Toast.makeText(RegistActivity.this,"군번을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_number.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_phone.equals("")) {
            Toast.makeText(RegistActivity.this,"휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_phone.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (i_participation == -1) {
            Toast.makeText(RegistActivity.this, "관람 방법을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if(i_division == -1) {
            Toast.makeText(RegistActivity.this,"구분을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if (s_temper.equals("")) {
            Toast.makeText(RegistActivity.this,"소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_destination.equals("")) {
            Toast.makeText(RegistActivity.this,"소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (!b_check) {
            Toast.makeText(RegistActivity.this,"개인정보 수집 활용에 동의해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RegistActivity.this, PopupRegistActivity.class);
            intent.putExtra("NAME", s_name);
            intent.putExtra("NUMBER", s_number);
            intent.putExtra("PHONE", s_phone);
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
//                registrationInfo();
                Toast.makeText(RegistActivity.this, "등록 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                intent.putExtra("H_NAME", s_name);
                intent.putExtra("H_NUMBER", s_number);
                intent.putExtra("H_PHONE", s_phone);
                intent.putExtra("H_DIVISION", Integer.toString(i_division));
                intent.putExtra("H_TEMPER", s_temper);
                intent.putExtra("H_DESTINATION", s_destination);
                intent.putExtra("H_PARTICIPATION", Integer.toString(i_participation));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

//    private void registrationInfo() {
//        SharedPreferences.Editor editor = infoData.edit();
//
//        editor.putInt("PARTICIPATION", i_participation);
//        editor.putString("NAME", s_name);
//        editor.putInt("DIVISION", i_division);
//        editor.putString("TEMPER", s_temper);
//        editor.putString("NUMBER", s_number);
//        editor.putString("PHONE", s_phone);
//        editor.putString("DESTINATION", s_destination);
//        editor.putBoolean("AGREE", b_check);
//        editor.putBoolean("IS_REGISTERED", true);
//        editor.putBoolean("AUTOLOGIN", false);
//        editor.apply();
//    }

}
