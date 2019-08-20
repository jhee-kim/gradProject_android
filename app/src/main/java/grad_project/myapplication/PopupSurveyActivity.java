package grad_project.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class PopupSurveyActivity extends Activity {
    Button bt_apply, bt_cancel;
    private static String SURVEY_LINK = "https://www.i815.or.kr/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_survey);

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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SURVEY_LINK));
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
