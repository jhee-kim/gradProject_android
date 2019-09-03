package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopupRegistActivity extends Activity {
    TextView tv_name, tv_number, tv_phone, tv_division, tv_temper, tv_participation, tv_destination, tv_question;
    LinearLayout ll_buttons;
    Button bt_apply, bt_cancel;
    String s_name, s_number, s_phone, s_division, s_temper, s_participation, s_destination;
    int i_division, i_participation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_regist);

        tv_name = findViewById(R.id.tv_name);
        tv_number = findViewById(R.id.tv_number);
        tv_phone = findViewById(R.id.tv_phone);
        tv_division = findViewById(R.id.tv_division);
        tv_temper = findViewById(R.id.tv_temper);
        tv_participation = findViewById(R.id.tv_participation);
        tv_destination = findViewById(R.id.tv_destination);
        tv_question = findViewById(R.id.tv_question);

        ll_buttons = findViewById(R.id.ll_buttons);
        bt_apply = findViewById(R.id.bt_apply);
        bt_cancel = findViewById(R.id.bt_cancel);

        Intent intent = getIntent();
        s_name = intent.getStringExtra("NAME");
        s_number = intent.getStringExtra("NUMBER");
        s_phone = intent.getStringExtra("PHONE");
        s_temper = intent.getStringExtra("TEMPER");
        s_destination = intent.getStringExtra("DESTINATION");
        i_division = intent.getIntExtra("DIVISION", -1);
        i_participation = intent.getIntExtra("PARTICIPATION", -1);

        switch (i_division) {
            case 0 :
                s_division = "육군";
                break;
            case 1 :
                s_division = "해군";
                break;
            case 2 :
                s_division = "공군";
                break;
            case 3 :
                s_division = "해병대";
                break;
            case -1 :
                s_division = "오류";
                break;
        }
        switch (i_participation) {
            case 0 :
                s_participation = "일반 관람";
                break;
            case 1 :
                s_participation = "해설 관람";
                break;
            case -1 :
                s_participation = "오류";
                break;
        }

        tv_name.setText(s_name);
        tv_number.setText(s_number);
        tv_phone.setText(s_phone);
        tv_division.setText(s_division);
        tv_temper.setText(s_temper);
        tv_participation.setText(s_participation);
        tv_destination.setText(s_destination);

        bt_apply.setEnabled(false);
        bt_cancel.setEnabled(false);
        tv_question.setText("입력하신 내용을 확인해주세요.");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv_question.setText("정확하게 입력하셨나요?");
                        bt_apply.setBackgroundColor(getResources().getColor(R.color.applyButton));
                        bt_apply.setTextColor(getResources().getColor(R.color.unfocusText));
                        bt_cancel.setBackgroundColor(getResources().getColor(R.color.cancelButton));
                        bt_cancel.setTextColor(getResources().getColor(R.color.unfocusText));
                        bt_apply.setEnabled(true);
                        bt_cancel.setEnabled(true);
                    }
                }, 5000);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !(event.getAction() == MotionEvent.ACTION_OUTSIDE);
    }

    @Override
    public void onBackPressed() {
    }

    public void onClick(View v) {

        if (v == bt_apply) {
            Intent intent = new Intent(PopupRegistActivity.this, RegistActivity.class);
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (v == bt_cancel) {
            Intent intent = new Intent(PopupRegistActivity.this, RegistActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }
}
