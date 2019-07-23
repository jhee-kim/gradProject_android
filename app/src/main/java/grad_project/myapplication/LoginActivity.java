package grad_project.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private boolean is_registered = false;
    private SharedPreferences infoData;
    private String name, serial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        loadInfo();

        if (is_registered) {
            EditText et_name = findViewById(R.id.et_login_name);
            EditText et_serial = findViewById(R.id.et_login_serial);
            et_name.setText(name);
            et_serial.setText(serial);
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(false);
        }

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
            EditText et_serial = findViewById(R.id.et_login_serial);
            String input_name = et_name.getText().toString();
            String input_serial = et_serial.getText().toString();
            if (input_name.equals("") || input_serial.equals("")) {
                Toast.makeText(getApplicationContext(), "정보를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show();
            }
            else if (input_name.equals(name) && input_serial.equals(serial)) {
                Intent in_login = new Intent(LoginActivity.this, MainActivity.class);
                setResult(RESULT_OK, in_login);
                in_login.putExtra("LOGIN", true);
                Toast.makeText(LoginActivity.this,"로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(LoginActivity.this,"로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
        if (v == bt_registration) {
            Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }
    private void loadInfo() {
        is_registered = infoData.getBoolean("IS_REGISTERED", false);
        name = infoData.getString("NAME", "");
        serial = infoData.getString("SERIAL", "");
    }
}
