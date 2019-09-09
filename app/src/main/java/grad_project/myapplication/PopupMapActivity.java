package grad_project.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupMapActivity extends Activity {

    ImageView MuseImage;
    TextView MuseTitle, MuseSub;
    String Mus_Title, Mus_Sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_map);
        MuseImage = (ImageView) findViewById(R.id.imageView);
        MuseTitle = (TextView) findViewById(R.id.museumTitle);
        MuseSub = (TextView) findViewById(R.id.museumSub);

        Intent intent = getIntent();
        int Mode = intent.getIntExtra("Mode", -1);
        int TagNumCheck = intent.getIntExtra("TagNum", -1);

        switch (Mode) {
            case 0:
                in_exhibition(TagNumCheck);
                break;
            case 1:
                out_exhibition(TagNumCheck);
                break;
            case 2:
                facilities(TagNumCheck);
                break;
            case 3:
                store(TagNumCheck);

                break;
            case -1:
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
        MuseTitle.setPaintFlags(MuseTitle.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }
    public void in_exhibition(int TagNumCheck) {
        switch (TagNumCheck) {
            case 0:
                MuseImage.setImageResource(R.drawable.in_exhibition_img00);
                Mus_Title = "제1전시관 겨레의뿌리";
                Mus_Sub = "제1관'겨레의뿌리'는 선사시대부터 조선시대 후기인 1860년대까지 우리 민족의 찬란한 문화유산과 불굴의 민족혼을 살펴볼 수 있습니다.";
                break;
            case 1:
                MuseImage.setImageResource(R.drawable.in_exhibition_img01);
                Mus_Title = "제2전시관 겨례의시련";
                Mus_Sub = "제2관'겨례의시련'은 우리 민족의 긴 역사가 일제의 침략으로 단절되고 국권을 상실한 일제강점기의 시련을 살펴볼 수 있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.in_exhibition_img02);
                Mus_Title = "제3전시관 겨레의함성";
                Mus_Sub = "제3관'겨레의함성'은 3·1운동과 대중투쟁에 참여했던 민중의 모습을 통해 우리 모두가 역사의 주인공임을 확인할 수 있는 전시관입니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.in_exhibition_img03);
                Mus_Title = "제4전시관 평화누리";
                Mus_Sub = "제4관'평화누리'는 민족의 자유와 독립을 위한 투쟁이자 인류 보편의 가치인 자유와 정의, 평화를 지향한 독립운동의 참뜻을 공감하고 나누는 공간입니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.in_exhibition_img04);
                Mus_Title = "제5전시관 나라되찾기";
                Mus_Sub = "제5관'나라되찾기'는 일제강점기 조국 독립을 되찾기 위해 국내외 각지에서 전개된 독립전쟁을 살펴볼 수 있습니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.in_exhibition_img05);
                Mus_Title = "제6전시관 새나라세우기";
                Mus_Sub = "제6관'새나라세우기'는 일제강점기 민족문화 수호운동과 민중의 항일, 그리고 대한민국임시정부의 활동을 살펴볼 수 있습니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.in_exhibition_img06);
                Mus_Title = "홍보관";
                Mus_Sub = "국민성금 모금운동의 결실로 건립된 독립기념관의 30년 발자취를 돌아볼 수 있도록 전시한 공간입니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.in_exhibition_img07);
                Mus_Title = "입체영상관";
                Mus_Sub = "대형 스크린과 바람시스템, 진동의자, 번개, 입체안경 등으로 보다 다이나믹한 영상과 특수 효과를 체험할 수 있습니다.";
                break;
            case 8:
                MuseImage.setImageResource(R.drawable.in_exhibition_img08);
                Mus_Title = "특별기획전시관";
                Mus_Sub = "특별기획전시물을 전시하는 전시관 입니다.";
                break;
            case -1:
                Mus_Title = "오류";
                Mus_Sub = "";
                break;
        }
    }
    public void out_exhibition(int TagNumCheck) {
        switch (TagNumCheck) {
            case 0:
                MuseImage.setImageResource(R.drawable.out_exhibition_img00);
                Mus_Title = "겨레의 탑";
                Mus_Sub = "민족의 비상을 표현하고 있는 겨레의 탑. 과거, 미래, 현재에 걸친 영원 불멸의 민족 기상을 나타냅니다.";
                break;
            case 1:
                MuseImage.setImageResource(R.drawable.out_exhibition_img01);
                Mus_Title = "태극기 한마당";
                Mus_Sub = "민족의 독립정신과 자주의식을 계승하고 나라사랑의 마음을 가지며 광복 60년을 상징하고자 태극기 815기를 연중 게양하는 태극기마당을 조성하였습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.out_exhibition_img02);
                Mus_Title = "겨레의 집";
                Mus_Sub = "독립기념관의 상징이자 중심 기념 홀로서 고려시대 수덕사 대웅전을 본떠 설계한 건물입니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.out_exhibition_img03);
                Mus_Title = "105인 층계";
                Mus_Sub = "독립기념과 추모의 자리에 오르는 계단으로 105개의 계단은 일제의 애국지사 탄압 사건인 105인 사건을 상징합니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.out_exhibition_img04);
                Mus_Title = "추모의 자리";
                Mus_Sub = "애국선열들의 고귀한 뜻을 기리고 겨레의 영원한 번영을 다짐하는 공간으로 독립기념관 상단에 위치해 있습니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.out_exhibition_img05);
                Mus_Title = "조선총독부 철거부재 전시공원";
                Mus_Sub = "1910년 식민통치기구로 설치된 조선총독부를 1995년 광복 50주년을 맞아 철거후 독립기념관으로 이전하여 일제 식민통치 몰락과 식민잔재 극복 및 청산을 알립니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.out_exhibition_img06);
                Mus_Title = "통일염원의 동산";
                Mus_Sub = "1995년 광복절에 준공되어 통일염원의 탑을 세운 후 중심에 통일의 종을 설치, 통일실현의 의지를 담은 조형물을 구성하고 통일 염원의 국민 동산을 조성하였습니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.out_exhibition_img07);
                Mus_Title = "C-47수송기 전시장";
                Mus_Sub = "제 2차세계대전 당시 미군이 사용하던 수송기로 광복 직후 1945년 11월 23일 김구 주석 외 대한민국 임시정부 요인들을 중국에서 환국시킨 수송기 입니다.";
                break;
            case 8:
                MuseImage.setImageResource(R.drawable.out_exhibition_img08);
                Mus_Title = "광개토대왕릉비";
                Mus_Sub = "일본의 역사 왜곡과 중국의 동북공정에 대한 우리 고대사의 왜곡, 날조에 대응하기위해 겨레의 큰마당에 재현되었습니다.";
                break;
            case 9:
                MuseImage.setImageResource(R.drawable.out_exhibition_img09);
                Mus_Title = "밀레니엄 숲";
                Mus_Sub = "21세기 남북통일과 민족화합을 염원하여 2003년 조성사벙을 완료하였으며, 철도레일을 설치하고 통일열차를 전시하고 있습니다.";
                break;
            case 10:
                MuseImage.setImageResource(R.drawable.out_exhibition_img10);
                Mus_Title = "백련못";
                Mus_Sub = "흑성산 산정으로부터 개수를 통해 유입된 연못으로 자연석을 배치하고 녹지와 나무그늘로 만든 아름다운 휴식공간입니다.";
                break;
            case 11:
                MuseImage.setImageResource(R.drawable.out_exhibition_img11);
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
    public void facilities(int TagNumCheck) {
        switch (TagNumCheck) {
            case 0:
                MuseImage.setImageResource(R.drawable.facilities_img00);
                Mus_Title = "태극열차";
                Mus_Sub = "독립기념관 순환 도로를 따라 겨례의 집 앞까지 가실 수 있습니다.";
                break;
            case 1:
                MuseImage.setImageResource(R.drawable.facilities_img01);
                Mus_Title = "겨례쉼터";
                Mus_Sub = "1,700여권의 도서와 지역별 독립운동 자료집이 비치되어 있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.facilities_img02);
                Mus_Title = "의무실";
                Mus_Sub = "문의전화 : 041-560-0504";
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                MuseImage.setImageResource(R.drawable.facilities_img03);
                Mus_Title = "음수시설";
                Mus_Sub = "물을 마실 수 있는 음수대가 있습니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.facilities_img04);
                Mus_Title = "유아놀이방";
                Mus_Sub = "이용시간 : 화 ~ 일요일 09:30 ~ 17:00 (동절기 09:30 ~ 16:00)";
                break;
            case 8:
            case 9:
                MuseImage.setImageResource(R.drawable.facilities_img05);
                Mus_Title = "수유실";
                Mus_Sub = "이용시간 :화 ~ 일요일 09:30 ~ 17:00 (동절기 09:30 ~ 16:00)";
                break;
            case 10:
                MuseImage.setImageResource(R.drawable.facilities_img06);
                Mus_Title = "종합안내센터";
                Mus_Sub = "국군 병사 휴가프로그램 접수처 및 유모차 및 휠체어 대여, 관람 안내 등의 서비스를 받을 수 있습니다.";
                break;
            case 11:
                MuseImage.setImageResource(R.drawable.facilities_img07);
                Mus_Title = "고객지원센터";
                Mus_Sub = "겨레의 집 내부에 있으며 유실물 방송, 해설지원, 음성안내기 대여, 물품보관함이 준비되어있습니다.";
                break;
            case 12:
                MuseImage.setImageResource(R.drawable.facilities_img08);
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
    public void store(int TagNumCheck) {
        switch (TagNumCheck) {
            case 0:
                MuseImage.setImageResource(R.drawable.store_img00);
                Mus_Title = "종합식당가";
                Mus_Sub = "종합안내센터 앞에 위치한 건물로 한정식, 퓨전 레스토랑, 롯데리아가 있으며 카페와 편의점이 위치해있습니다.";
                break;
            case 1:
                MuseImage.setImageResource(R.drawable.store_img01);
                Mus_Title = "(구) 매표소 앞";
                Mus_Sub = "음료와 호두과자를 파는 호두과자점과 24시 이마트 편의점이 위치해있습니다.";
                break;
            case 2:
                MuseImage.setImageResource(R.drawable.store_img02);
                Mus_Title = "백련못 매점";
                Mus_Sub = "백련못 옆 매점 입니다.";
                break;
            case 3:
                MuseImage.setImageResource(R.drawable.store_img03);
                Mus_Title = "태극열차 종점 매점";
                Mus_Sub = "태극열차가 멈추는 종점에 위치한 소규모 매점입니다.";
                break;
            case 4:
                MuseImage.setImageResource(R.drawable.store_img04);
                Mus_Title = "겨레카페";
                Mus_Sub = "겨레의 집 왼편에 있는 카페입니다.";
                break;
            case 5:
                MuseImage.setImageResource(R.drawable.store_img05);
                Mus_Title = "중앙식당";
                Mus_Sub = "겨레의 집 뒷편에 있는 식당으로 카페테리아입니다.";
                break;
            case 6:
                MuseImage.setImageResource(R.drawable.store_img06);
                Mus_Title = "기념품점";
                Mus_Sub = "제 1 전시관 출구 나가기전 옆에 위치하여 기념품을 구매할 수 있습니다.";
                break;
            case 7:
                MuseImage.setImageResource(R.drawable.store_img07);
                Mus_Title = "종합편의점 및 스낵코너";
                Mus_Sub = "제 2 전시관과 제 3 전시관 사이에 있으며 스낵코너에서는 닭강정, 떡볶이도 먹을 수 있습니다.";
                break;
            case 8:
                MuseImage.setImageResource(R.drawable.store_img08);
                Mus_Title = "푸른정원";
                Mus_Sub = "제 3 전시관과 제 4 전시관 사이에 위치한 가게입니다.";
                break;
            case 9:
                MuseImage.setImageResource(R.drawable.store_img09);
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
}
