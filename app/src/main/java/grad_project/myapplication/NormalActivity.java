package grad_project.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NormalActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener {
    private SharedPreferences infoData;
    private boolean[] isCheckQrArr = new boolean[6];
    Long startDate;
    private String[] exhibitionState = new String[6];      // 전시관 오픈 여부(1 : open, 0 : close)
    private String[] exhibitionQrCode = new String[6];     // 전시관 QR코드 URL
    private boolean isShowTutorial;
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


    Bitmap gps_tracking;
    Bitmap[] in_exhibition_marker = new Bitmap[3];
    Bitmap[] out_exhibition_marker = new Bitmap[12];
    Bitmap[] facilities_market = new Bitmap[13];
    Bitmap[] store = new Bitmap[10];

    int zoomLevel = -2;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";          //각 전시관 별 개설 여부(JSON 형식) - ex) { "number": "1", "isOpen": "1" }
    public static final String GET_QR = BASE_PATH + "get_qr.php";                              //각 전시관 별 qr코드 파일 위치(JSON 형식) - ex) { "number": "1", "address": "http://35.221.108.183/QR/1.png" }
//    public static final String GET_RSSID = BASE_PATH + "get_mac.php";                        //각 전시관 별 RSSI(JSON 형식) - ex) { "number": "1", "mac": "00:70:69:47:2F:30" }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

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

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        Intent intent = getIntent();
        startDate = intent.getLongExtra("Time", 0);

        mapView = new MapView(this);
        mapView.setPOIItemEventListener(this);
        mapView.setMapViewEventListener(this);
        mapViewContainer.addView(mapView);

        getExhibitionData();  // DB 전시관 데이터 받아오기(오픈여부, 전시관 별 QR코드)
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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {     //보낸 Intent 정보 받음
        if (resultCode == RESULT_OK) {
            switch (requestCode) {   // NormalActivity에서 TutorialActivity로 요청할 때 보낸 요청 코드 (3000)
                case 3000: break;//TutorialActivity에서 돌아왔을 때
                case 2000: {  //PopupMapActivity에서 돌아왔을 때
                    int result = intent.getIntExtra("finish_exhibition_num", -1);
                    boolean isAlreadyCheckQr = false;
                    if (result >= 1 && result <= 6) {        //전시관의 QR코드를 찍었으면
                        for (int i = 0; i < 6; i++) {
                            if (isCheckQrArr[result - 1]) {
                                Toast.makeText(getApplicationContext(), "이미 찍은 QR코드 입니다.", Toast.LENGTH_LONG).show();
                                isAlreadyCheckQr = true;
                            }
                        }
                        if (!isAlreadyCheckQr) {
                            Toast.makeText(getApplicationContext(), result + "전시관의 QR코드와 일치합니다!", Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = infoData.edit();  //해당 전시관에 대한 정보를 공유변수에 저장
                            editor.putBoolean("IS_CHECK_" + result, true);
                            editor.apply();
                            isCheckQrArr[result - 1] = true;

                            mapViewContainer.removeView(mapView);       //원래 설정해뒀떤 mapView를 삭제해고
                            mapView = new MapView(this);        //새로 띄워줌(찍은 QR 적용해 맵을 띄우기 위함)
                            setIn_exhibition();
                        }
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
            Log.d("gps","위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도  : " + altitude);
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

    // 저장된 값 가져오기 - 각 전시관 QR코드 체크 여부, 튜토리얼 여부
    public void loadInfo() {
        for (int i = 0; i < 6; i++) {
            int num = i + 1;
            isCheckQrArr[i] = infoData.getBoolean("IS_CHECK_" + num, false);
            Log.d("isCheckQrArr" + num, String.valueOf(isCheckQrArr[i]));
        }
        isShowTutorial = infoData.getBoolean("IS_SHOW_TUTORIAL", false);
    }

    // DB 전시관 데이터 받아오기
    public void getExhibitionData() {
        // 전시관 오픈 여부
        GetExhibitionTask task = new GetExhibitionTask(this);
        try {
            String result = task.execute(GET_EXHIBITION).get();
            Log.d("Exhibition", result);
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            Log.d("ARRAY LENGTH", Integer.toString(jArray.length()));
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                exhibitionState[i] = jObject.getString("isOpen");
                Log.d("EXHIBITION", i + " : " + exhibitionState[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 전시관 QR코드
        GetExhibitionQrTask qrTask = new GetExhibitionQrTask(this);
        try {
            String result = qrTask.execute(GET_QR).get();
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                exhibitionQrCode[i] = jObject.getString("address");
                Log.d("EXHIBITION_QRCODE", exhibitionQrCode[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkBoundary() {
        MapPoint leftB = MapPoint.mapPointWithGeoCoord(36.786625, 127.218318);
        MapPoint RightT = MapPoint.mapPointWithGeoCoord(36.776687, 127.233477);

        MapPointBounds boundary = new MapPointBounds(leftB, RightT);
        boolean isContain = boundary.contains(curPosition);

        if(isContain) {
            Toast.makeText(getApplicationContext(), "독립기념관 안입니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "위치를 이탈하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setGpsTracking() {
        if(ToggleGps) {
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
            }
            else {
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

        MapPOIItem[] markerArr = new  MapPOIItem[mapPointArr.size()];
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

        MapPOIItem[] markerArr = new  MapPOIItem[mapPointArr.size()];
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

        MapPOIItem[] markerArr = new  MapPOIItem[mapPointArr.size()];
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
            findViewById(R.id.location).setBackgroundResource(R.drawable.gps_on);
        } else {
            mapView.removePOIItem(markerGps);
            findViewById(R.id.location).setBackgroundResource(R.drawable.gps_off);
        }
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void setTogglebut(int Mode) {
        ImageButton[] imgBut = new ImageButton[5];
        imgBut[0] = findViewById(R.id.in_exhibition);
        imgBut[1] =  findViewById(R.id.out_exhibition);
        imgBut[2] = findViewById(R.id.facilities);
        imgBut[3] = findViewById(R.id.store);
        imgBut[4] = findViewById(R.id.list);
        TextView[] tv = new TextView[5];
        tv[0] = findViewById(R.id.tv_in_exhibition);
        tv[1] = findViewById(R.id.tv_out_exhibition);
        tv[2] = findViewById(R.id.tv_facilities);
        tv[3] = findViewById(R.id.tv_store);
        tv[4] = findViewById(R.id.tv_list);
        for(int i = 0; i < 5; i++) {
            if(i == Mode) {
                tv[i].setTextColor(Color.parseColor("#3F51B5"));
            }
            else {
                tv[i].setTextColor(Color.parseColor("#000000"));
            }
        }

        switch (Mode) {
            case 0 : {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_on);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                break;
            }
            case 1 : {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_on);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                break;
            }
            case 2 : {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_on);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                break;
            }
            case 3 : {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_on);
                imgBut[4].setBackgroundResource(R.drawable.list_off);
                break;
            }
            case 4 : {
                imgBut[0].setBackgroundResource(R.drawable.in_exhibition_off);
                imgBut[1].setBackgroundResource(R.drawable.out_exhibition_off);
                imgBut[2].setBackgroundResource(R.drawable.facilities_off);
                imgBut[3].setBackgroundResource(R.drawable.store_off);
                imgBut[4].setBackgroundResource(R.drawable.list_on);
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

    // 전시관 오픈 여부 받아오는 부분
    public static class GetExhibitionTask extends AsyncTask<String, Void, String> {
        private WeakReference<NormalActivity> activityReference;
        ProgressDialog progressDialog;

        GetExhibitionTask(NormalActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            /*출력값*/
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                int responseStatusCode = httpURLConnection.getResponseCode();
                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }

    // 전시관 QR코드 정보 받아오는 부분
    public static class GetExhibitionQrTask extends AsyncTask<String, Void, String> {
        private WeakReference<NormalActivity> activityReference;
        ProgressDialog progressDialog;

        GetExhibitionQrTask(NormalActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            /*출력값*/
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                int responseStatusCode = httpURLConnection.getResponseCode();
                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
    public void initBitmap() {
        gps_tracking = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.gps_tracking);
        gps_tracking = Bitmap.createScaledBitmap(gps_tracking, 100, 100, true);

        in_exhibition_marker[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker00);
        in_exhibition_marker[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker01);
        in_exhibition_marker[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.in_exhibition_marker02);
        for(int i = 0; i < in_exhibition_marker.length; i++) {
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
        for(int i = 0; i < out_exhibition_marker.length; i++) {
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
        for(int i = 0; i < facilities_market.length; i++) {
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
        for(int i = 0; i < store.length; i++) {
            store[i] = Bitmap.createScaledBitmap(store[i], 90, 90, true);
        }

    }
}
