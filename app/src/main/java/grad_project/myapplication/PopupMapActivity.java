package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
    private String[] qrCodeUrlArr = new String[6];
    private String exhibitionState; //해당 전시관이 열렸는지 여부


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_map);
        qrScan = new IntentIntegrator(this);

        Intent intent = getIntent();
        int TagNumCheck = intent.getIntExtra("TagNum", -1);
        qrCodeUrlArr = intent.getStringArrayExtra("exhibitionQrCode");
        exhibitionState = intent.getStringExtra("exhibitionState");

        //인텐트로 각 qr URL 받아서 qrArr 배열에 저장

        MuseImage = (ImageView) findViewById(R.id.imageView);
        MuseTitle = (TextView) findViewById(R.id.museumTitle);
        MuseSub = (TextView) findViewById(R.id.museumSub);

        MuseImage.setImageResource(R.drawable.ic_alarm_24dp);

        QR_Button = findViewById(R.id.QRbutton);
        setQrButton();

        MuseTitle.setPaintFlags(MuseTitle.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        switch (TagNumCheck) {
            case 1:
                MuseImage.setImageResource(R.drawable.exhibit_img01);
                Mus_Title = "제1전시관 겨레의뿌리";
                Mus_Sub = "제1관'겨레의뿌리'는 선사시대부터 조선시대 후기인 1860년대까지 우리 민족의 찬란한 문화유산과 불굴의 민족혼을 살펴볼 수 있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.exhibit_img02);
                Mus_Title = "제2전시관 겨례의시련";
                Mus_Sub = "제2관'겨례의시련'은 우리 민족의 긴 역사가 일제의 침략으로 단절되고 국권을 상실한 일제강점기의 시련을 살펴볼 수 있습니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.exhibit_img03);
                Mus_Title = "제3전시관 겨레의함성";
                Mus_Sub = "제3관'겨레의함성'은 3·1운동과 대중투쟁에 참여했던 민중의 모습을 통해 우리 모두가 역사의 주인공임을 확인할 수 있는 전시관입니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.exhibit_img04);
                Mus_Title = "제4전시관 평화누리";
                Mus_Sub = "제4관'평화누리'는 민족의 자유와 독립을 위한 투쟁이자 인류 보편의 가치인 자유와 정의, 평화를 지향한 독립운동의 참뜻을 공감하고 나누는 공간입니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.exhibit_img05);
                Mus_Title = "제5전시관 나라되찾기";
                Mus_Sub = "제5관'나라되찾기'는 일제강점기 조국 독립을 되찾기 위해 국내외 각지에서 전개된 독립전쟁을 살펴볼 수 있습니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.exhibit_img06);
                Mus_Title = "제6전시관 새나라세우기";
                Mus_Sub = "제6관'새나라세우기'는 일제강점기 민족문화 수호운동과 민중의 항일, 그리고 대한민국임시정부의 활동을 살펴볼 수 있습니다.";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }

        MuseTitle.setText(Mus_Title);
        MuseTitle.setClickable(false);
        MuseSub.setText(Mus_Sub);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.y = 400;

        getWindow().setLayout((int)(width*0.95), (int)(height * 0.2));
    }

    public void setQrButton() {     //열리지 않은 전시관의 QR버튼이 나타나지 않도록
        if(exhibitionState.equals("0")) {
            QR_Button.setVisibility(View.GONE);
        }
    }

    //QR을 위한 액티비티 연결 필요
    public void QRClick(View view){
        qrScan.setCaptureActivity(QrActivity.class);
        qrScan.setBeepEnabled(false);
        qrScan.setPrompt("전시관의 QR코드를 스캔해주세요.");
        qrScan.setCameraId(0);
        qrScan.setOrientationLocked(false);
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

        if (qrUrl == null) {
            //Toast.makeText(getApplicationContext(), "내용이 존재하지 않습니다.", Toast.LENGTH_LONG).show();
        } else { //QR코드, 내용 존재
            try {
                Log.d("QR_URL : ", qrUrl);
                int resultExhibitionNum = findCorrespondExhibition(qrUrl);
                if(resultExhibitionNum >= 1 && resultExhibitionNum <= 6) {
                    //Intent로 찍힌 QR코드 전시관 번호 보내기(1~6)
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("finish_exhibition_num",resultExhibitionNum); /*송신*/
                    setResult(RESULT_OK, resultIntent);
                    finish();
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
            if(qrUrl.equals(qrCodeUrlArr[i])) {
                resultExhibition = i + 1;
                Log.d("Debug i : ", qrUrl);
                break;
            }
        }
        //결과반환
        return resultExhibition;
    }

}
