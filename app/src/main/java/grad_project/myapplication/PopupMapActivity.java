package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class PopupMapActivity extends Activity {
    TextView museumTitle;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_map);

        museumTitle = findViewById(R.id. museumTitle);

        Intent intent = getIntent();
        number = intent.getStringExtra("AUDIENCE");

        museumTitle.setText(number);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)width, (int)(height * 0.25));
    }
}
