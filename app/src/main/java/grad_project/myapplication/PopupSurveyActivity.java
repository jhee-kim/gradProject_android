package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class PopupSurveyActivity extends Activity {
    Button bt_apply, bt_cancel;
    private String SURVEY_LINK = "www.i815.or.kr/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_survey);

        Intent intent = getIntent();
        SURVEY_LINK = intent.getStringExtra("URL");

        bt_apply = findViewById(R.id.bt_apply);
        bt_cancel = findViewById(R.id.bt_cancel);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !(event.getAction() == MotionEvent.ACTION_OUTSIDE);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_apply :
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + SURVEY_LINK));
                startActivity(intent);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.bt_cancel :
                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
