package grad_project.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class PersonalActivity extends AppCompatActivity {
    String s_id, s_name, s_number, s_phone, s_temper, s_destination, s_participation, s_division;
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

        SharedPreferences infoData;
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");

        tv_name = findViewById(R.id.tv_name);
        tv_number = findViewById(R.id.tv_number);
        tv_phone = findViewById(R.id.tv_phone);
        tv_division = findViewById(R.id.tv_division);
        tv_temper = findViewById(R.id.tv_temper);
        tv_destination = findViewById(R.id.tv_destination);
        tv_participation = findViewById(R.id.tv_participation);

        if (getUserData()) {
            tv_name.setText(s_name);
            tv_number.setText(s_number);
            tv_phone.setText(s_phone);
            String str_temper = "";
            switch (s_division) {
                case "0":
                    str_temper += "육군 / ";
                    break;
                case "1":
                    str_temper += "해군 / ";
                    break;
                case "2":
                    str_temper += "공군 /  ";
                    break;
                case "3":
                    str_temper += "해병대 / ";
                    break;
                default:
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
                    tv_participation.setText("해설 관람");
                    break;
                default:
                    tv_participation.setText("E");
            }
        } else {
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }
    public boolean getUserData() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(DdConnect.GET_AUDIENCE, s_id).get();
            Log.d("GET_AUDIENCE", result);
            if (!result.equals("-1")) {
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
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
