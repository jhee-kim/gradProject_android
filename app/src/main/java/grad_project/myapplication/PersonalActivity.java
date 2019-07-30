package grad_project.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PersonalActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    String s_name, s_number, s_phone, s_temper, s_destination, s_participation, s_division;
    TextView tv_name, tv_number, tv_phone, tv_destination, tv_participation, tv_division, tv_temper;

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
        loadInfo();

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

    // 저장된 값 가져오기
    private void loadInfo() {
        s_number = infoData.getString("NUMBER", "");
        s_name = infoData.getString("NAME", "");
        s_phone = infoData.getString("PHONE", "");
        s_division = infoData.getString("DIVISION", "");
        s_temper = infoData.getString("TEMPER", "");
        s_destination = infoData.getString("DESTINATION", "");
        s_participation = infoData.getString("PARTICIPATION", "");
    }
}
