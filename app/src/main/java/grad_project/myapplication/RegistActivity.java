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
    private int i_class = -1;
    private int i_type = -1;
    private String s_corps = "";
    private String s_serial = "";
    private String s_phone = "";
    private String s_destination = "";
    private boolean b_check = false;
    EditText et_name, et_corps, et_serial, et_phone, et_destination;
    CheckBox cb_check;
    RadioGroup rg_type, rg_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        et_name = findViewById(R.id.et_name);
        et_corps = findViewById(R.id.et_corps);
        et_serial = findViewById(R.id.et_serial);
        et_phone = findViewById(R.id.et_phone);
        et_destination = findViewById(R.id.et_destination);
        cb_check = findViewById(R.id.cb_check);

        rg_type = findViewById(R.id.rg_type);
        rg_class = findViewById(R.id.rg_class);

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
        int type_selected = rg_type.getCheckedRadioButtonId();
        int class_selected = rg_class.getCheckedRadioButtonId();

        s_name = et_name.getText().toString().trim();
        s_corps = et_corps.getText().toString().trim();
        s_serial = et_serial.getText().toString().trim();
        s_phone = et_phone.getText().toString().trim();
        s_destination = et_destination.getText().toString().trim();
        b_check = cb_check.isChecked();

        switch (type_selected) {
            case R.id.rb_normal:
                i_type = 0;
                break;
            case R.id.rb_narr:
                i_type = 1;
                break;
            default:
                i_type = -1;
                break;
        }

        switch (class_selected) {
            case R.id.rb_army:
                i_class = 0;
                break;
            case R.id.rb_airforce:
                i_class = 2;
                break;
            case R.id.rb_navy:
                i_class = 1;
                break;
            case R.id.rb_mc:
                i_class = 3;
                break;
            default:
                i_class = -1;
                break;
        }

        Log.i("NAME", s_name);
        Log.i("CLASS", Integer.toString(i_class));
        Log.i("CORPS", s_corps);
        Log.i("SERIAL", s_serial);
        Log.i("PHONE", s_phone);

        if (s_name.equals("")) {
            Toast.makeText(RegistActivity.this,"이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_name.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_serial.equals("")) {
            Toast.makeText(RegistActivity.this,"군번을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_serial.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_phone.equals("")) {
            Toast.makeText(RegistActivity.this,"휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_phone.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (i_type == -1) {
            Toast.makeText(RegistActivity.this, "관람 방법을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if(i_class == -1) {
            Toast.makeText(RegistActivity.this,"구분을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if (s_corps.equals("")) {
            Toast.makeText(RegistActivity.this,"소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_corps.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (s_destination.equals("")) {
            Toast.makeText(RegistActivity.this,"소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_corps.requestFocus();
//
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else if (!b_check) {
            Toast.makeText(RegistActivity.this,"개인정보 수집 활용에 동의해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RegistActivity.this, PopupRegistActivity.class);
            intent.putExtra("NAME", s_name);
            intent.putExtra("SERIAL", s_serial);
            intent.putExtra("PHONE", s_phone);
            intent.putExtra("CLASS", i_class);
            intent.putExtra("CORPS", s_corps);
            intent.putExtra("DESTINATION", s_destination);
            intent.putExtra("TYPE", i_type);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 입력된 정보 최종 확인이 되었을 경우
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                registrationInfo();
                Toast.makeText(RegistActivity.this, "등록 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                intent.putExtra("H_NAME", s_name);
                intent.putExtra("H_NUMBER", s_serial);
                intent.putExtra("H_PHONE", s_phone);
                intent.putExtra("H_DIVISION", Integer.toString(i_class));
                intent.putExtra("H_TEMPER", s_corps);
                intent.putExtra("H_DESTINATION", s_destination);
                intent.putExtra("H_PARTICIPATION", Integer.toString(i_type));

                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private void registrationInfo() {
//        SharedPreferences.Editor editor = infoData.edit();
//
//        editor.putInt("TYPE", i_type);
//        editor.putString("NAME", s_name);
//        editor.putInt("CLASS", i_class);
//        editor.putString("CORPS", s_corps);
//        editor.putString("SERIAL", s_serial);
//        editor.putString("PHONE", s_phone);
//        editor.putString("DESTINATION", s_destination);
//        editor.putBoolean("AGREE", b_check);
//        editor.putBoolean("IS_REGISTERED", true);
//        editor.putBoolean("AUTOLOGIN", false);
//        editor.apply();

//        Toast.makeText(RegistActivity.this,"개인 정보를 등록했습니다.", Toast.LENGTH_SHORT).show();


    }

}
