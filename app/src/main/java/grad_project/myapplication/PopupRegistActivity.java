package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.security.PublicKey;

public class PopupRegistActivity extends Activity {
    TextView tv_name, tv_serial, tv_phone, tv_class, tv_corps, tv_type, tv_destination;
    String s_name, s_serial, s_phone, s_class, s_corps, s_type, s_destination;
    int i_class, i_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_regist);

        tv_name = findViewById(R.id.tv_name);
        tv_serial = findViewById(R.id.tv_serial);
        tv_phone = findViewById(R.id.tv_phone);
        tv_class = findViewById(R.id.tv_class);
        tv_corps = findViewById(R.id.tv_corps);
        tv_type = findViewById(R.id.tv_type);
        tv_destination = findViewById(R.id.tv_destination);

        Intent intent = getIntent();
        s_name = intent.getExtras().getString("NAME");
        s_serial = intent.getExtras().getString("SERIAL");
        s_phone = intent.getExtras().getString("PHONE");
        s_corps = intent.getExtras().getString("CORPS");
        s_destination = intent.getExtras().getString("DESTINATION");
        i_class = intent.getExtras().getInt("CLASS", -1);
        i_type = intent.getExtras().getInt("TYPE", -1);

        switch (i_class) {
            case 0 :
                s_class = "육군";
                break;
            case 2 :
                s_class = "공군";
                break;
            case 1 :
                s_class = "해군";
                break;
            case 3 :
                s_class = "해병대";
                break;
            case -1 :
                s_class = "오류";
                break;
        }
        switch (i_type) {
            case 0 :
                s_type = "일반 관람";
                break;
            case 1 :
                s_type = "전시 해설";
                break;
            case -1 :
                s_type = "오류";
                break;
        }

        tv_name.setText(s_name);
        tv_serial.setText(s_serial);
        tv_phone.setText(s_phone);
        tv_class.setText(s_class);
        tv_corps.setText(s_corps);
        tv_type.setText(s_type);
        tv_destination.setText(s_destination);
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
        Button bt_apply = findViewById(R.id.bt_apply);
        Button bt_cancel = findViewById(R.id.bt_cancel);

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
