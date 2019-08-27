package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class PopupOcrActivity extends Activity {
    Button bt_apply, bt_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_ocr);

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
                Intent intent = new Intent(PopupOcrActivity.this, OcrActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.bt_cancel :
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            // 인식 완료되면
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(PopupOcrActivity.this, RegistActivity.class);
                setResult(RESULT_OK, intent);
                intent.putExtra("result", data.getStringExtra("result"));
                finish();
            }
            // 사용자가 인식 기능 사용 취소하면
            else if (resultCode == RESULT_CANCELED) {
                Intent intent = new Intent(PopupOcrActivity.this, RegistActivity.class);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }
    }
}
