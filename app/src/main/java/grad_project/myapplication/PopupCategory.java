package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupCategory extends Activity {

    ImageView MuseImage;
    ImageView MarkerImage;
    TextView MuseTitle, MuseSub;
    String Mus_Title, Mus_Sub;

    private int TagNumCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_cat);


        Intent intent = getIntent();

        int MarkerState = intent.getIntExtra("MarkerState", -1);
        TagNumCheck = intent.getIntExtra("TagNum", -1);

        MuseImage = (ImageView) findViewById(R.id.imageView);
        MuseTitle = (TextView) findViewById(R.id.museumTitle);
        MuseSub = (TextView) findViewById(R.id.museumSub);

        MuseImage.setImageResource(R.drawable.ic_alarm_24dp);



        switch (MarkerState) {
            case 1:
                showOutside();
                break;
            case 2:
                showShop();
                break;
            case 3:
                showPark();
                break;
            case -1:
                break;
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.y = 400;

        getWindow().setLayout((int)(width*0.95), (int)(height * 0.2));

    }


    public void showOutside () {
        switch (TagNumCheck) {
            case 1:
                MuseImage.setImageResource(R.drawable.out_1st);
                Mus_Title = "겨레의 탑";
                Mus_Sub = "민족의 비상을 표현하고 있는 겨레의 탑. 과거, 미래, 현재에 걸친 영원 불멸의 민족 기상을 나타냅니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.out_2nd);
                Mus_Title = "태극기 한마당";
                Mus_Sub = "민족의 독립정신과 자주의식을 계승하고 나라사랑의 마음을 가지며 광복 60년을 상징하고자 태극기 815기를 연중 게양하는 태극기마당을 조성하였습니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.out_3rd);
                Mus_Title = "겨레의 큰마당";
                Mus_Sub = "겨레의 집 앞 광장으로 기념행사나 열린 음악회같은 큰 행사를 치르기 위한 장소입니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.out_4th);
                Mus_Title = "겨레의 집";
                Mus_Sub = "독립기념관의 상징이자 중심 기념 홀로서 고려시대 수덕사 대웅전을 본떠 설계한 건물입니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.out_5th);
                Mus_Title = "105인 층계";
                Mus_Sub = "독립기념과 추모의 자리에 오르는 계단으로 105개의 계단은 일제의 애국지사 탄압 사건인 105인 사건을 상징합니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.out_6th);
                Mus_Title = "추모의 자리";
                Mus_Sub = "애국선열들의 고귀한 뜻을 기리고 겨레의 영원한 번영을 다짐하는 공간으로 독립기념관 상단에 위치해 있습니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.out_7th);
                Mus_Title = "조선총독부 철거부재 전시공원";
                Mus_Sub = "1910년 식민통치기구로 설치된 조선총독부를 1995년 광복 50주년을 맞아 철거후 독립기념관으로 이전하여 일제 식민통치 몰락과 식민잔재 극복 및 청산을 알립니다.";
                break;
            case 8:
                MuseImage.setImageResource(R.drawable.out_8th);
                Mus_Title = "통일염원의 동산";
                Mus_Sub = "1995년 광복절에 준공되어 통일염원의 탑을 세운 후 중심에 통일의 종을 설치, 통일실현의 의지를 담은 조형물을 구성하고 통일 염원의 국민 동산을 조성하였습니다.";
                break;
            case 9:
                MuseImage.setImageResource(R.drawable.out_9th);
                Mus_Title = "C-47수송기 전시장";
                Mus_Sub = "제 2차세계대전 당시 미군이 사용하던 수송기로 광복 직후 1945년 11월 23일 김구 주석 외 대한민국 임시정부 요인들을 중국에서 환국시킨 수송기 입니다.";
                break;
            case 10:
                MuseImage.setImageResource(R.drawable.out_10th);
                Mus_Title = "광개토대왕릉비";
                Mus_Sub = "일본의 역사 왜곡과 중국의 동북공정에 대한 우리 고대사의 왜곡, 날조에 대응하기위해 겨레의 큰마당에 재현되었습니다.";
                break;
            case 11:
                MuseImage.setImageResource(R.drawable.out_11th);
                Mus_Title = "밀레니엄 숲";
                Mus_Sub = "21세기 남북통일과 민족화합을 염원하여 2003년 조성사벙을 완료하였으며, 철도레일을 설치하고 통일열차를 전시하고 있습니다.";
                break;
            case 12:
                MuseImage.setImageResource(R.drawable.out_12th);
                Mus_Title = "백련못";
                Mus_Sub = "흑성산 산정으로부터 개수를 통해 유입된 연못으로 자연석을 배치하고 녹지와 나무그늘로 만든 아름다운 휴식공간입니다.";
                break;
            case 13:
                MuseImage.setImageResource(R.drawable.out_13th);
                Mus_Title = "통일의 길";
                Mus_Sub = "겨레의 집, 전시관, 백련못을 둘러싸고 있는 약 2km 길이의 길로 태극열차와 태극버스가 운행되고 있습니다.";
                break;
            case 14:
                MuseImage.setImageResource(R.drawable.out_14th);
                Mus_Title = "단풍나무 숲길";
                Mus_Sub = "조선총독부 철거부재 전시공원에서부터 시작하여 통일염원의 동산 입구까지 약 4km 길이의 단풍나무 길로 가을의 정취를 만끽할 수 있습니다.";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }

        MuseTitle.setText(Mus_Title);
        MuseTitle.setClickable(false);
        MuseSub.setText(Mus_Sub);

    }

    public void showShop () {
        switch (TagNumCheck) {
            case 1:
                MuseImage.setImageResource(R.drawable.shop_img1);
                Mus_Title = "종합식당가";
                Mus_Sub = "종합안내센터 앞에 위치한 건물로 한정식, 퓨전 레스토랑, 롯데리아가 있으며 카페와 편의점이 위치해있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.shop_img2);
                Mus_Title = "(구) 매표소 앞";
                Mus_Sub = "음료와 호두과자를 파는 호두과자점과 24시 이마트 편의점이 위치해있습니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.shop_img3);
                Mus_Title = "백련못 매점";
                Mus_Sub = "백련못 옆 매점 입니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.shop_img4);
                Mus_Title = "태극열차 종점 매점";
                Mus_Sub = "태극열차가 멈추는 종점에 위치한 소규모 매점입니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.shop_img5);
                Mus_Title = "겨레카페";
                Mus_Sub = "겨레의 집 왼편에 있는 카페입니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.shop_img6);
                Mus_Title = "중앙식당";
                Mus_Sub = "겨레의 집 뒷편에 있는 식당으로 카페테리아입니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.shop_img7);
                Mus_Title = "기념품점";
                Mus_Sub = "제 1 전시관 출구 나가기전 옆에 위치하여 기념품을 구매할 수 있습니다.";
                break;
            case 8:
                MuseImage.setImageResource(R.drawable.shop_img8);
                Mus_Title = "종합편의점 및 스낵코너";
                Mus_Sub = "제 2 전시관과 제 3 전시관 사이에 있으며 스낵코너에서는 닭강정, 떡볶이도 먹을 수 있습니다.";
                break;
            case 9:
                MuseImage.setImageResource(R.drawable.shop_img9);
                Mus_Title = "푸른정원";
                Mus_Sub = "제 3 전시관과 제 4 전시관 사이에 위치한 가게입니다.";
                break;
            case 10:
                MuseImage.setImageResource(R.drawable.shop_img10);
                Mus_Title = "문화상품관";
                Mus_Sub = "제 4 전시관과 제 5 전시관 사이에 위치한 가게입니다.";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }

        MuseTitle.setText(Mus_Title);
        MuseTitle.setClickable(false);
        MuseSub.setText(Mus_Sub);

    }

    public void showPark () {
        switch (TagNumCheck) {
            case 1:
                MuseImage.setImageResource(R.drawable.information_center);
                Mus_Title = "종합안내센터";
                Mus_Sub = "국군 병사 휴가프로그램 접수처로, 유모차 및 휠체어 대여, 관람 안내 등의 서비스를 받을 수 있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.support_center);
                Mus_Title = "고객지원센터";
                Mus_Sub = "겨레의 집 내부에 있으며 유실물 방송, 해설지원, 음성안내기 대여, 물품보관함, 카페테리아를 이용할 수 있으며, 의무실이 준비되어있습니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.parking_lot);
                Mus_Title = "주차장";
                Mus_Sub = "주차관제 시스템 설치로 관람객이 안전하고 편리하게 주차할 수 있도록 운영, 관리하고 있습니다.";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }

        MuseTitle.setText(Mus_Title);
        MuseTitle.setClickable(false);
        MuseSub.setText(Mus_Sub);

    }

}
