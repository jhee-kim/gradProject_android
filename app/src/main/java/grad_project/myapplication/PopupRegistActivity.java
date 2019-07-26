package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.security.PublicKey;

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
        s_name = intent.getExtras().getString("NAME");
        s_number = intent.getExtras().getString("NUMBER");
        s_phone = intent.getExtras().getString("PHONE");
        s_temper = intent.getExtras().getString("TEMPER");
        s_destination = intent.getExtras().getString("DESTINATION");
        i_division = intent.getExtras().getInt("DIVISION", -1);
        i_participation = intent.getExtras().getInt("PARTICIPATION", -1);

        switch (i_division) {
            case 0 :
                s_division = "육군";
                break;
            case 2 :
                s_division = "공군";
                break;
            case 1 :
                s_division = "해군";
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
                s_participation = "전시 해설";
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

//        ll_buttons.setVisibility(View.INVISIBLE);
//        bt_apply.setEnabled(false);
//        bt_cancel.setEnabled(false);
//        tv_question.setText("입력하신 내용을 확인해주세요.");
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_question.setText("정확하게 입력하셨나요?");
//                        ll_buttons.setVisibility(View.VISIBLE);
//                        bt_apply.setEnabled(true);
//                        bt_cancel.setEnabled(true);
//                    }
//                }, 5000);
//            }
//        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
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
