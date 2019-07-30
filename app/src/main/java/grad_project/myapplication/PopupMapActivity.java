package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PopupMapActivity extends Activity {

    ImageView MuseImage;
    TextView MuseTitle, MuseSub;
    String Mus_Title, Mus_Sub;
    Button QR_Button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_map);

        Intent intent = getIntent();

        int TagNumCheck = intent.getIntExtra("TagNum", -1);

        MuseImage = (ImageView) findViewById(R.id.imageView);
        MuseTitle = (TextView) findViewById(R.id.museumTitle);
        MuseSub = (TextView) findViewById(R.id.museumSub);

        MuseImage.setImageResource(R.drawable.ic_alarm_24dp);

        QR_Button = findViewById(R.id.QRbutton);

        switch (TagNumCheck) {
            case 0:
                Mus_Title = "제1전시관 겨레의뿌리";
                Mus_Sub = "";
                break;
            case 1:
                Mus_Title = "제2전시관 교체중";
                Mus_Sub = "";
                break;
            case 2:
                Mus_Title = "제3전시관 겨레의함성";
                Mus_Sub = "";
                break;
            case 3:
                Mus_Title = "제4전시관 평화누리";
                Mus_Sub = "";
                break;
            case 4:
                Mus_Title = "제5전시관 나라되찾기";
                Mus_Sub = "";
                break;
            case 5:
                Mus_Title = "제6전시관 새나라세우기";
                Mus_Sub = "";
                break;
            case 6:
                Mus_Title = "제7전시관 없음";
                Mus_Sub = "";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }

        MuseTitle.setText(Mus_Title);
        MuseSub.setText(Mus_Sub);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.y = 800;

        //getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout((int)(width*0.95), (int)(height * 0.2));
    }

    //QR을 위한 액티비티 연결 필요
    public void QRClick(View view){
        Toast.makeText(getApplicationContext(), "QR연결 요구", Toast.LENGTH_SHORT).show();
    }
}
