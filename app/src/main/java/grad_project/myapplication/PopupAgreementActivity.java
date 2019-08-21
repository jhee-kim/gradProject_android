package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class PopupAgreementActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_agreement);
    }

    public void onClick(View v) {
        Intent intent = new Intent(PopupAgreementActivity.this, RegistActivity.class);

        switch (v.getId()) {
            case R.id.bt_apply:
                setResult(RESULT_OK, intent);
                Toast.makeText(getApplicationContext(), "개인정보 수집 및 이용에 동의하셨습니다.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.bt_cancel:
                setResult(RESULT_CANCELED, intent);
                Toast.makeText(getApplicationContext(), "개인정보 수집 및 이용에 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                finish();
        }
    }
}
