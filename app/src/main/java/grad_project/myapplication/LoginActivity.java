package grad_project.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private boolean is_correct = false;
    private String s_id, input_name;
    Button bt_login, bt_registration;
    Spinner sp_number_0;
    EditText et_name, et_number_1;
    String s_number_0, s_number_1;

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
            } else {
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

        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(dbConnect.LOGIN, "", input_number, input_name).get();
            Log.d("LOGIN", result);
            if (!result.equals("-1")) {
                if (result.equals("0")) {
                    is_correct = false;
                } else {
                    s_id = result;
                    is_correct = true;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
