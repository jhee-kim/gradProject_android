package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class PopupMapActivity extends Activity {

    ImageView MuseImage;
    TextView MuseTitle, MuseSub;
    String Mus_Title, Mus_Sub;
    Button QR_Button;
    private IntentIntegrator qrScan;
    private String[] qrArr = {"1", "2", "3", "4", "5", "6"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_map);
        qrScan = new IntentIntegrator(this);

        Intent intent = getIntent();
        int TagNumCheck = intent.getIntExtra("TagNum", -1);

        //인텐트로 각 qr URL 받아서 qrArr 배열에 저장

        MuseImage = (ImageView) findViewById(R.id.imageView);
        MuseTitle = (TextView) findViewById(R.id.museumTitle);
        MuseSub = (TextView) findViewById(R.id.museumSub);

        MuseImage.setImageResource(R.drawable.ic_alarm_24dp);

        QR_Button = findViewById(R.id.QRbutton);

        switch (TagNumCheck) {
            case 1:
                Mus_Title = "제1전시관 겨레의뿌리";
                Mus_Sub = "";
                break;
            case 2:
                Mus_Title = "제2전시관 교체중";
                Mus_Sub = "";
                break;
            case 3:
                Mus_Title = "제3전시관 겨레의함성";
                Mus_Sub = "";
                break;
            case 4:
                Mus_Title = "제4전시관 평화누리";
                Mus_Sub = "";
                break;
            case 5:
                Mus_Title = "제5전시관 나라되찾기";
                Mus_Sub = "";
                break;
            case 6:
                Mus_Title = "제6전시관 새나라세우기";
                Mus_Sub = "";
                break;
            case 7:
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
        qrScan.setBeepEnabled(false);
        qrScan.setPrompt("전시관 QR코드를 스캔해주세요.");
        qrScan.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // QR코드/ 바코드를 스캔한 결과
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // result.getFormatName() : 바코드 종류
        // result.getContents() : 바코드 값
        String qrUrl = result.getContents();
        Log.d("QR_URL : ", qrUrl);

        if (qrUrl == null) {
            Toast.makeText(getApplicationContext(), "내용이 존재하지 않습니다.", Toast.LENGTH_LONG).show();
        } else { //QR코드, 내용 존재
            try {
                int resultExhibitionNum = findCorrespondExhibition(qrUrl);
                if(resultExhibitionNum >= 1 && resultExhibitionNum <= 6) {
                    Toast.makeText(getApplicationContext(), resultExhibitionNum + "전시관의 QR코드와 일치합니다!", Toast.LENGTH_LONG).show();

                    //Intent로 찍힌 QR코드 전시관 번호 보내기(1~6)
                    Intent intent = new Intent(PopupMapActivity.this, NormalActivity.class);
                    intent.putExtra("finish_exhibition_num",resultExhibitionNum); /*송신*/
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "QR코드가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                }
                Log.d("qrcode Contents : ", qrUrl);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Exception : ", "QR code fail");
            }
        }
    }

    public int findCorrespondExhibition(String qrUrl) { //QR코드를 비교, 일치하는 전시관이 있으면 전시관 번호 반환 / 아니면 -1 반환
        int resultExhibition = -1;
        for(int i = 0 ; i < 6 ; i++) {   //URL 비교(전체 전시관)
            if(qrUrl.equals(qrArr[i])) {
                resultExhibition = i + 1;
                Log.d("Debug i : ", qrUrl);
                break;
            }
        }
        //결과반환
        return resultExhibition;
    }

}
