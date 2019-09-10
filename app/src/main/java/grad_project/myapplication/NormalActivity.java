package grad_project.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NormalActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener {
    private static final String TAG = NormalActivity.class.getSimpleName();
    private SharedPreferences infoData;
    private final int ImgNumByExhibition = 2;
    private int totalImgNum;
    private boolean[] isCheckQrArr = new boolean[6];
    private boolean[][] isCheckImgArr = new boolean[6][ImgNumByExhibition];
    private int[][] randomImgNumArr = new int[6][ImgNumByExhibition];   //랜덤으로 뽑은 이미지 번호
    Long startDate;
    private String[] exhibitionState = new String[6];      // 전시관 오픈 여부(1 : open, 0 : close)
    private String[] exhibitionQrCode = new String[6];     // 전시관 QR코드 URL
    private int nowPosition;        //선택한 list 위치
    private boolean isShowTutorial;
    private TextView listCountTv;   //리스트뷰 개수
    private ViewGroup mapViewContainer;
    private MapView mapView;
    private int Mode;
    private boolean ToggleGps = false;
    private MapPoint curPosition;

    MapPOIItem markerGps = new MapPOIItem();
    String provider;    //위치정보
    double longitude;  //위도
    double latitude;   //경도
    double altitude;   //고도

    ListView questView;     //관람확인
    List<questViewItems> questItems;
    ImageButton but_gps;
    int questNum;
    QuestAdapter adapter;

    Bitmap gps_tracking;
    Bitmap[] in_exhibition_marker = new Bitmap[3];
    Bitmap[] out_exhibition_marker = new Bitmap[12];
    Bitmap[] facilities_market = new Bitmap[13];
    Bitmap[] store = new Bitmap[10];

    int zoomLevel = -2;

    /*** list test ***/
    ListView listView;
    List<String> qTitle = new ArrayList<String>();
    //    String qTitle[] = new String[10];
    String Title[] = {"제1전시관", "제2전시관", "제3전시관", "제4전시관", "제5전시관", "제6전시관"};
    //    String qTitle[];
    List<String> qDescription = new ArrayList<String>();
    //    String qDescription[] = new String[10];
    String qrDescription[] = {"제1전시관 설명", "제2전시관 설명", "제3전시관 설명", "제4전시관 설명", "제5전시관 설명", "제6전시관 설명"};
    String imgDescription[][] = {{"제1전시관 사진1", "제1전시관 사진2", "제1전시관 사진3", "제1전시관 사진4"},      //images배열과 매칭
                                {"제2전시관 사진1", "제2전시관 사진2", "제2전시관 사진3", "제2전시관 사진4"},
                                {"제3전시관 사진1", "제3전시관 사진2", "제3전시관 사진3", "제3전시관 사진4"},
                                {},
                                {"제5전시관 사진1", "제5전시관 사진2", "제5전시관 사진3", "제5전시관 사진4"},
                                {"제6전시관 사진1", "제6전시관 사진2", "제6전시관 사진3", "제6전시관 사진4"},};  //찍을 사진들 이름 적으면 될듯
    //    List<String> qDescription1;
    List<Integer> qImages = new ArrayList<Integer>();
    //int qImages[] = new int[6];
    int qrImages[] = {R.drawable.in_exhibition_img01, R.drawable.in_exhibition_img02, R.drawable.in_exhibition_img03, R.drawable.in_exhibition_img04, R.drawable.in_exhibition_img05, R.drawable.in_exhibition_img06};
    int images[][] = {{R.drawable.img1_1, R.drawable.img1_2, R.drawable.img1_3, R.drawable.img1_4}, //전시관마다 최대 사진 4개씩
                    {R.drawable.img2_1, R.drawable.img2_2, R.drawable.img2_3, R.drawable.img2_4},
                    {R.drawable.img3_1, R.drawable.img3_2, R.drawable.img3_3, R.drawable.img3_4},
                    {},         //4전시관은 사진X
                    {R.drawable.img5_1, R.drawable.img5_2, R.drawable.img5_3, R.drawable.img5_4},
                    {R.drawable.img6_1, R.drawable.img6_2, R.drawable.img6_3, R.drawable.img6_4}};   //찍을 이미지들
    List<Integer> qExhibitionNum = new ArrayList<Integer>();
    List<Integer> qType = new ArrayList<Integer>();     //0 - QR / 1 - Image
    List<Integer> qNumOfImg = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        listCountTv = (TextView)findViewById(R.id.list_count_tv);
        questView = (ListView) findViewById(R.id.questList);
        but_gps = (ImageButton) findViewById(R.id.but_gps);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        initBitmap();

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);

        totalImgNum = ImgNumByExhibition * 6;

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        Intent intent = getIntent();
        startDate = intent.getLongExtra("Time", 0);

        mapView = new MapView(this);
        mapView.setPOIItemEventListener(this);
        mapView.setMapViewEventListener(this);
        mapViewContainer.addView(mapView);

        getExhibitionData();  // DB 전시관 데이터 받아오기(오픈여부, 전시관 별 QR코드)
        if(!infoData.getBoolean("IS_RANDOM_IMG", false)) {      //한번만 실행
            chooseRandomImg(4, ImgNumByExhibition); //이미지를 랜덤으로 선택(Random돌릴 이미지 개수, 몇개뽑을건지)
        }
        loadInfo();           // 저장된 값 가져오기 - 각 전시관 QR코드 체크 여부, 튜토리얼 여부
        Mode = 0;
        setTogglebut(Mode);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.784116, 127.222147), 1, true);
        setIn_exhibition();

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (!isShowTutorial) {       //처음 지도를 보는 거면(튜토리얼을 본 적이 없으면) 튜토리얼을 보여줌
            intent = new Intent(NormalActivity.this, TutorialActivity.class);
            startActivityForResult(intent, 3000);
        }

        //list test
        listView = findViewById(R.id.questList);
        questItems = new ArrayList<questViewItems>();

        attributeSetting();
        QuestItemSet();

        listCountTv.setText(questItems.size() + "");
        listCountTv.setVisibility(View.VISIBLE);

        adapter = new QuestAdapter(this, questItems);
        listView.setAdapter(adapter); // 리스트뷰 생성됨

        QuestviewClick();

    }

    //보낸 Intent 정보 받음
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {   // NormalActivity에서 TutorialActivity로 요청할 때 보낸 요청 코드 (3000)
                case 3000:
                    break;//TutorialActivity에서 돌아왔을 때
                case 49374: {  //QrActivity에서 돌아왔을 때
                    String qrUrl = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent).getContents();
                    //URL 비교
                    boolean isCheck = false;
                    for(int i = 0 ; i < 6 ; i++) {
                        if(exhibitionQrCode[i].equals(qrUrl)) {
                            if(isCheckQrArr[i]) {
                                Toast.makeText(getApplicationContext(), "이미 찍은 QR코드 입니다.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), (i + 1) + "전시관의 QR코드와 일치합니다!", Toast.LENGTH_LONG).show();
                                SharedPreferences.Editor editor = infoData.edit();  //해당 전시관에 대한 정보를 공유변수에 저장
                                editor.putBoolean("IS_CHECK_QR_" + (i + 1), true);
                                editor.apply();
                                isCheckQrArr[i] = true;

                                for(int j = 0 ; j < questItems.size() ; j++) { //Listview에서 해당 Qr 제거해줌
                                    questViewItems q = questItems.get(j);
                                    //Log.d("qitemType", q.QTypeItem + "");
                                    //Log.d("qitemQExhibitionNum", q.QExhibitionNum + "");
                                    if(q.QTypeItem == 0 && q.QExhibitionNum == i) { //전시관 번호가 같고 QR이면
                                        boolean b = questItems.remove(q);                   //해당 item 제거
                                        //Log.d("qitemBool", b + "");
                                        adapter = new QuestAdapter(NormalActivity.this, questItems);
                                        listView.setAdapter(adapter);
                                        listCountTv.setText(questItems.size() + "");
                                        if(questItems.size() == 0) {
                                            listCountTv.setVisibility(View.GONE);
                                        }
                                        else {
                                            listCountTv.setVisibility(View.VISIBLE);
                                        }

                                    }
                                }
                            }
                            isCheck = true;
                            break;
                        }
                    }
                    if(!isCheck) {
                        Toast.makeText(getApplicationContext(), "일치하는 QR코드가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                case 1000: {
                    if(intent.getBooleanExtra("isSuccess", false)) {
                        questItems.remove(nowPosition);
                        adapter = new QuestAdapter(NormalActivity.this, questItems);
                        listView.setAdapter(adapter);
                        //Toast.makeText(NormalActivity.this, "찍은사진 삭제", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitude = location.getAltitude();
            Log.d("gps", "위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도  : " + altitude);
            curPosition = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            setGpsTracking();
            checkBoundary();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    // 저장된 값 가져오기 - 각 전시관 QR코드 체크 여부, 사진 체크여부, 튜토리얼 여부, 선택된 랜덤 사진들
    public void loadInfo() {
        for (int i = 0; i < 6; i++) {
            int num = i + 1;
            isCheckQrArr[i] = infoData.getBoolean("IS_CHECK_QR_" + num, false);     //전시관 QR코드 체크 여부
            Log.d(TAG, "isCheckQrArr" + num + ": " + isCheckQrArr[i]);
            for(int j = 0 ; j < ImgNumByExhibition ; j++) {     //전시관 이미지들 찍었는지 여부
                isCheckImgArr[i][j] = infoData.getBoolean("IS_CHECK_PIC_" + num + "-" + (j + 1), false);
                Log.d(TAG, "isCheckImgArr" + num + ": " + isCheckImgArr[i][j]);
            }
            for(int j = 0 ; j < ImgNumByExhibition ; j++) {     //선택된 랜덤 사진들 번호
                randomImgNumArr[i][j] = infoData.getInt("RANDOM_IMG_" + num + "_" + (j + 1), -1);
                Log.d(TAG, "randomImgNumArr" + num + ": " + randomImgNumArr[i][j]);
            }
        }
        isShowTutorial = infoData.getBoolean("IS_SHOW_TUTORIAL", false);        //튜토리얼 여부
    }
    public void chooseRandomImg(int maxImgNum, int randomImgNum) {
        SharedPreferences.Editor editor = infoData.edit();
        Random rand = new Random();     //현재 시간을 초기값으로 난수 발생
        boolean random[] = new boolean[maxImgNum];
        for(int i = 0 ; i < 6 ; i++) {
            if(i == 3) continue;    //4전시관은 패스
            for(int j = 0 ; j < maxImgNum ; j++) {
                random[j] = false;
            }
            for(int j = 0 ; j < randomImgNum ; j++) {       //중복아닌 randomImgNum개수의 숫자를 뽑음
                int a = rand.nextInt(maxImgNum);
                while(true) {
                    if(!random[a]) {
                        random[a] = true;
                        break;
                    } else {
                        a = (a + 1) % maxImgNum;
                    }
                    //Log.d("aaaaa", j + "_" + a + "_" + random[a]);
                }
            }
            int a = 1;  //몇번째 사진인지
            for(int j = 0 ; j < maxImgNum ; j++) {
                if(random[j]) {
                    editor.putInt("RANDOM_IMG_" + (i + 1) + "_" + a, j);
                    a++;
                }
            }
        }
        editor.putBoolean("IS_RANDOM_IMG", true);
        editor.apply();
    }

    public void getExhibitionData() {
        DdConnect dbConnect1 = new DdConnect(this);
        try {
            String result = dbConnect1.execute(dbConnect1.GET_EXHIBITION).get();
            Log.d("GET_EXHIBITION", result);
            if(!result.equals("-1")) {
                JSONObject jResult = new JSONObject(result);
                JSONArray jArray = jResult.getJSONArray("result");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    exhibitionState[i] = jObject.getString("isOpen");
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
        }

        DdConnect dbConnect2 = new DdConnect(this);
        try {
            String result = dbConnect2.execute(dbConnect2.GET_QR).get();
            Log.d("GET_QR", result);
            if(!result.equals("-1")) {
                JSONObject jResult = new JSONObject(result);
                JSONArray jArray = jResult.getJSONArray("result");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    exhibitionQrCode[i] = jObject.getString("address");
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkBoundary() {
        MapPoint leftB = MapPoint.mapPointWithGeoCoord(36.786625, 127.218318);
        MapPoint RightT = MapPoint.mapPointWithGeoCoord(36.776687, 127.233477);

        MapPointBounds boundary = new MapPointBounds(leftB, RightT);
        boolean isContain = boundary.contains(curPosition);

        if (isContain) {
            Toast.makeText(getApplicationContext(), "독립기념관 안입니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "위치를 이탈하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setGpsTracking() {
        if (ToggleGps) {
            mapView.removePOIItem(markerGps);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            markerGps.setItemName("");
            markerGps.setShowCalloutBalloonOnTouch(false);
            markerGps.setMapPoint(mapPoint);
            markerGps.setTag(0);
            markerGps.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            markerGps.setCustomImageBitmap(gps_tracking);
            mapView.addPOIItem(markerGps);
        }
    }

    public void setIn_exhibition() {
        mapView.removeAllPOIItems();

        List<MapPoint> mapPointArr = new ArrayList<MapPoint>();
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783353, 127.221629));  //제 1전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783802, 127.220985));  //제 2전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784352, 127.220894));  //제 3전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784868, 127.220980));  //제 4전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.785106, 127.221696));  //제 5전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784863, 127.222632));  //제 6전시관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783092, 127.223064));  //홍보관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784983, 127.223346));  //입체상영관
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784608, 127.223823));  //특별기획전시관

        MapPOIItem[] markerArr = new MapPOIItem[mapPointArr.size()];
        for (int i = 0; i < mapPointArr.size(); i++) {
            markerArr[i] = new MapPOIItem();
            markerArr[i].setMapPoint(mapPointArr.get(i));
            markerArr[i].setTag(i);
            markerArr[i].setItemName("");
            markerArr[i].setShowCalloutBalloonOnTouch(false);

            if (i < 6) {
                if (exhibitionState[i].equals("1") && !isCheckQrArr[i]) {

                    markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    markerArr[i].setCustomImageBitmap(in_exhibition_marker[0]);
                } else if (exhibitionState[i].equals("1") && isCheckQrArr[i]) {
                    markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    markerArr[i].setCustomImageBitmap(in_exhibition_marker[1]);
                } else if (exhibitionState[i].equals("0")) {
                    markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    markerArr[i].setCustomImageBitmap(in_exhibition_marker[2]);
                }
            } else {
                markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
                markerArr[i].setCustomImageBitmap(in_exhibition_marker[1]);
            }
            mapView.addPOIItem(markerArr[i]);
        }
        setGpsTracking();
    }

    public void setOut_exhibition() {
        mapView.removeAllPOIItems();

        List<MapPoint> mapPointArr = new ArrayList<MapPoint>();
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.780575, 127.227633));  //겨례의 탑
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.782636, 127.224867));  //태극기한마당
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783721, 127.223193));  //겨례의 집
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.785332, 127.219999));  //105인 층계
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.786663, 127.218276));  //추모의 자리
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.780123, 127.222143));  //조선총독부 철거부재 전시공원
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.782238, 127.229518));  //통일염원의 동산
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783914, 127.224861));  //C-47수송기 전시장
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.782167, 127.226047));  //광개토대왕릉비
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.778860, 127.222339));  //밀레니엄 숲
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.781928, 127.226287));  //백련못
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.785811, 127.217168));  //단풍나무 숲길

        MapPOIItem[] markerArr = new MapPOIItem[mapPointArr.size()];
        for (int i = 0; i < mapPointArr.size(); i++) {
            markerArr[i] = new MapPOIItem();

            markerArr[i].setMapPoint(mapPointArr.get(i));
            markerArr[i].setTag(i);

            markerArr[i].setItemName("open_marker");
            markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
            markerArr[i].setCustomImageBitmap(out_exhibition_marker[i]);

            markerArr[i].setShowCalloutBalloonOnTouch(false);
            mapView.addPOIItem(markerArr[i]);
        }
        setGpsTracking();
    }

    public void setFacilities() {
        mapView.removeAllPOIItems();

        List<MapPoint> mapPointArr = new ArrayList<MapPoint>();
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.779731, 127.228604));  //태극열차
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783088, 127.223259));  //겨례쉼터
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783011, 127.223018));  //의무실
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783906, 127.223203));  //음수시설1
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783313, 127.222573));  //음수시설2
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784530, 127.221057));  //음수시설3
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.785084, 127.222044));  //음수시설4
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783990, 127.222085));  //유아놀이방
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784198, 127.222242));  //수유실1
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.779536, 127.230317));  //수유실2
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.779347, 127.230344));  //종합안내센터
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783088, 127.222836));  //고객지원센터
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.778109, 127.231310));  //주차장

        MapPOIItem[] markerArr = new MapPOIItem[mapPointArr.size()];
        for (int i = 0; i < mapPointArr.size(); i++) {
            markerArr[i] = new MapPOIItem();
            markerArr[i].setMapPoint(mapPointArr.get(i));
            markerArr[i].setTag(i);
            markerArr[i].setItemName("");
            markerArr[i].setShowCalloutBalloonOnTouch(false);

            markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
            markerArr[i].setCustomImageBitmap(facilities_market[i]);

            mapView.addPOIItem(markerArr[i]);
        }
        setGpsTracking();
    }

    public void setStore() {
        mapView.removeAllPOIItems();

        List<MapPoint> mapPointArr = new ArrayList<MapPoint>();
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.779057, 127.229849));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.779917, 127.229098));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.781910, 127.225719));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.782821, 127.223723));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783345, 127.223025));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783912, 127.222242));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.783766, 127.221051));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784162, 127.220880));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784626, 127.221008));
        mapPointArr.add(MapPoint.mapPointWithGeoCoord(36.784918, 127.221341));

        MapPOIItem[] markerArr = new MapPOIItem[mapPointArr.size()];
        for (int i = 0; i < mapPointArr.size(); i++) {
            markerArr[i] = new MapPOIItem();
            markerArr[i].setItemName("");
            markerArr[i].setShowCalloutBalloonOnTouch(false);
            markerArr[i].setMapPoint(mapPointArr.get(i));
            markerArr[i].setTag(i);

            markerArr[i].setItemName("open_marker");
            markerArr[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);
            markerArr[i].setCustomImageBitmap(store[0]);

            markerArr[i].setShowCalloutBalloonOnTouch(false);
            mapView.addPOIItem(markerArr[i]);
        }
        setGpsTracking();
    }

    public void timeOn(View view) {
        Intent intent = new Intent(NormalActivity.this, PopupTimeActivity.class);
        intent.putExtra("Time", startDate);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_top);
    }

    public void onIn_exhibition(View view) {
        Mode = 0;
        setTogglebut(Mode);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.784116, 127.222147), 1, true);
        setIn_exhibition();
    }

    public void onOut_exhibition(View view) {
        Mode = 1;
        setTogglebut(Mode);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.780575, 127.227633), 1, true);
        setOut_exhibition();
    }

    public void onFacilities(View view) {
        Mode = 2;
        setTogglebut(Mode);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.778816, 127.230159), 1, true);
        setFacilities();
    }

    public void onStore(View view) {
        Mode = 3;
        setTogglebut(Mode);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.778816, 127.230159), 1, true);
        setStore();
    }

    public void onList(View view) {
        Mode = 4;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setTogglebut(Mode);
    }

    public void onCheck(View view) {
        Intent intent = new Intent(NormalActivity.this, CheckActivity.class);
        startActivity(intent);
    }

    public void onTutorial(View v) {
        Intent intent = new Intent(NormalActivity.this, TutorialActivity.class);
        startActivityForResult(intent, 3000);
    }

    public void onLocation(View view) {
        ToggleGps = !ToggleGps;
        if (ToggleGps) {
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), zoomLevel, true);
            setGpsTracking();
            findViewById(R.id.but_gps).setBackgroundResource(R.drawable.gps_on);
        } else {
            mapView.removePOIItem(markerGps);
            findViewById(R.id.but_gps).setBackgroundResource(R.drawable.gps_off);
        }
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void setTogglebut(int Mode) {
        mapViewContainer.setVisibility(View.GONE);
        questView.setVisibility(View.GONE);
        but_gps.setVisibility(View.GONE);

        ImageButton[] imgBut = new ImageButton[5];
        imgBut[0] = findViewById(R.id.in_exhibition);
        imgBut[1] = findViewById(R.id.out_exhibition);
        imgBut[2] = findViewById(R.id.facilities);
        imgBut[3] = findViewById(R.id.store);
        imgBut[4] = findViewById(R.id.list);
        TextView[] tv = new TextView[5];
        tv[0] = findViewById(R.id.tv_in_exhibition);
        tv[1] = findViewById(R.id.tv_out_exhibition);
        tv[2] = findViewById(R.id.tv_facilities);
        tv[3] = findViewById(R.id.tv_store);
        tv[4] = findViewById(R.id.tv_list);
        for (int i = 0; i < 5; i++) {
            if (i == Mode) {
                tv[i].setTextColor(Color.parseColor("#3F51B5"));
            } else {
                tv[i].setTextColor(Color.parseColor("#000000"));
            }
        }

        switch (Mode) {
            case 0: {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_on);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                mapViewContainer.setVisibility(View.VISIBLE);
                but_gps.setVisibility(View.VISIBLE);
                break;
            }
            case 1: {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_on);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                mapViewContainer.setVisibility(View.VISIBLE);
                but_gps.setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_on);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                mapViewContainer.setVisibility(View.VISIBLE);
                but_gps.setVisibility(View.VISIBLE);
                break;
            }
            case 3: {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_on);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                mapViewContainer.setVisibility(View.VISIBLE);
                but_gps.setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_on);
                questView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {  //마커 클릭 핸들링
        int tagNum = mapPOIItem.getTag();
        Intent intent = new Intent(NormalActivity.this, PopupMapActivity.class);

        intent.putExtra("Mode", Mode);
        intent.putExtra("TagNum", tagNum);
        startActivityForResult(intent, 2000);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoom) {
        zoomLevel = zoom;
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    public void initBitmap() {
        gps_tracking = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.gps_tracking);
        gps_tracking = Bitmap.createScaledBitmap(gps_tracking, 100, 100, true);

        in_exhibition_marker[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker00);
        in_exhibition_marker[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker01);
        in_exhibition_marker[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker02);
        for (int i = 0; i < in_exhibition_marker.length; i++) {
            in_exhibition_marker[i] = Bitmap.createScaledBitmap(in_exhibition_marker[i], 110, 110, true);
        }

        out_exhibition_marker[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker00);
        out_exhibition_marker[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker01);
        out_exhibition_marker[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker02);
        out_exhibition_marker[3] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker03);
        out_exhibition_marker[4] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker02);
        out_exhibition_marker[5] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker04);
        out_exhibition_marker[6] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker04);
        out_exhibition_marker[7] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker05);
        out_exhibition_marker[8] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker02);
        out_exhibition_marker[9] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker04);
        out_exhibition_marker[10] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker04);
        out_exhibition_marker[11] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_exhibition_marker04);
        for (int i = 0; i < out_exhibition_marker.length; i++) {
            out_exhibition_marker[i] = Bitmap.createScaledBitmap(out_exhibition_marker[i], 90, 90, true);
        }

        facilities_market[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker00);
        facilities_market[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker01);
        facilities_market[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker02);
        facilities_market[3] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker03);
        facilities_market[4] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker03);
        facilities_market[5] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker03);
        facilities_market[6] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker03);
        facilities_market[7] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker04);
        facilities_market[8] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker05);
        facilities_market[9] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker05);
        facilities_market[10] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker06);
        facilities_market[11] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker06);
        facilities_market[12] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.facilities_marker07);
        for (int i = 0; i < facilities_market.length; i++) {
            facilities_market[i] = Bitmap.createScaledBitmap(facilities_market[i], 90, 90, true);
        }

        store[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker03);
        store[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        store[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        store[3] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        store[4] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker01);
        store[5] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker03);
        store[6] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        store[7] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        store[8] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker03);
        store[9] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.store_marker00);
        for (int i = 0; i < store.length; i++) {
            store[i] = Bitmap.createScaledBitmap(store[i], 90, 90, true);
        }

    }


    public void QuestItemSet() {
        questNum = 0;
        for (; questNum < qTitle.size() ; questNum++) {
            questItems.add(new questViewItems() {{
                QImageItem = qImages.get(questNum);
                QTitleItem = qTitle.get(questNum);
                QSubTitleItem = qDescription.get(questNum);
                QExhibitionNum = qExhibitionNum.get(questNum);
                QTypeItem = qType.get(questNum);
                QNumOfImg = qNumOfImg.get(questNum);
            }});
        }
    }

    //QuestView의 리스트들 클릭됬을 때
    public void QuestviewClick() {
        //list test
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int type = questItems.get(position).QTypeItem;
                if(type == 0) { //QR코드인 경우
                    IntentIntegrator qrScan = new IntentIntegrator(NormalActivity.this);
                    qrScan.setCaptureActivity(QrActivity.class);
                    qrScan.setBeepEnabled(false);
                    qrScan.setPrompt("전시관의 QR코드를 스캔해주세요.");
                    qrScan.setCameraId(0);
                    qrScan.setOrientationLocked(false);
                    qrScan.initiateScan();
                }
                else if(type == 1) {    //사진인 경우
                    nowPosition = position;
                    Intent intent = new Intent(NormalActivity.this, CompareActivity.class);
                    intent.putExtra("imgAddress", questItems.get(position).QImageItem);
                    intent.putExtra("exhibitionNum", questItems.get(position).QExhibitionNum);      //전시관 번호(0~5)
                    intent.putExtra("imgNum", questItems.get(position).QNumOfImg);                  //몇번째 이미지인지(0~max-1)
                    startActivityForResult(intent, 1000);
                }
            }
        });
    }

    class QuestAdapter extends BaseAdapter {
        private final List<questViewItems> items;
        private final LayoutInflater qinflator;


        QuestAdapter(Activity context, List<questViewItems> items) {
            super();

            this.items = items;
            this.qinflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        //list test
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            questViewItems qItem = items.get(position);

            View qcontents = convertView;

            if (convertView == null)
                qcontents = qinflator.inflate(R.layout.list_quest, null);

            ImageView images = qcontents.findViewById(R.id.questImage);
            TextView myTItle = qcontents.findViewById(R.id.QtextMain);
            TextView myDescription = qcontents.findViewById(R.id.QtextSub);

            images.setImageResource(qItem.QImageItem);
            myTItle.setText(qItem.QTitleItem);
            myDescription.setText(qItem.QSubTitleItem);

            return qcontents;
        }
    }

    class questViewItems {
        public int QImageItem;
        public String QTitleItem;
        public String QSubTitleItem;
        public int QExhibitionNum;          //전시관 번호
        public int QTypeItem;               //Qr인지 사진인지
        public int QNumOfImg;               //몇번째 사진인지
    }

    public void attributeSetting() {
        for(int i = 0 ; i < 6 ; i++){
            if(exhibitionState[i].equals("1")) {
                if (isCheckQrArr[i] == false) {
                    qTitle.add(Title[i]);
                    qDescription.add(qrDescription[i]);
                    qImages.add(qrImages[i]);
                    qExhibitionNum.add(i);
                    qType.add(0);
                    qNumOfImg.add(-1);
                }
                if(i == 3) continue;    //4전시관은 사진 패스
                for(int j = 0 ; j < ImgNumByExhibition ; j++) {
                    if(isCheckImgArr[i][j] == false) {
                        qTitle.add(Title[i]);
                        qDescription.add(imgDescription[i][(randomImgNumArr[i][j])]);
                        qImages.add(images[i][(randomImgNumArr[i][j])]);     //찍을 이미지로 바꿔줘야함**
                        qExhibitionNum.add(i);
                        qType.add(1);
                        qNumOfImg.add(j);
                    }
                }
            }
        }
    }
}
