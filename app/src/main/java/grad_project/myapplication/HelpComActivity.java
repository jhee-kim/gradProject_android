package grad_project.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HelpComActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpcom);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        TextView tv1 = findViewById(R.id.tv_red1);
        String str1 = "● 지참물 : 휴가증 필참\n                 (육군은 외박, 외출 제외)";
        SpannableStringBuilder ssb1 = new SpannableStringBuilder(str1);
        ssb1.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 8, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv1.setText(ssb1);

        TextView tv2 = findViewById(R.id.tv_red2);
        String str2 = "● 인원제한 : 선착순으로 등록이 진행됨";
        SpannableStringBuilder ssb2 = new SpannableStringBuilder(str2);
        ssb2.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv2.setText(ssb2);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }
}