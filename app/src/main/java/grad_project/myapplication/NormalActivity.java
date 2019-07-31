package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class NormalActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    private SharedPreferences infoData;
    private boolean[] isCheckQrArr = new boolean[6];
    Long startDate;
    private String[] exhibitionState = new String[6];      // 전시관 오픈 여부(1 : open, 0 : close)
    private String[] exhibitionQrCode = new String[6];     // 전시관 QR코드 URL
//    String[] exhibitionRssId = new String[6];    // 차후 구현 예정

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";          //각 전시관 별 개설 여부(JSON 형식) - ex) { "number": "1", "isOpen": "1" }
    public static final String GET_QR = BASE_PATH + "get_qr.php";                              //각 전시관 별 qr코드 파일 위치(JSON 형식) - ex) { "number": "1", "address": "http://35.221.108.183/QR/1.png" }
//    public static final String GET_NARRATOR = BASE_PATH + "get_narrator.php";            //관람등록시 전시해설 on/oof 여부(해설자 스케줄 확인)  - 미구현
//    public static final String GET_RSSID = BASE_PATH + "get_mac.php";                        //각 전시관 별 RSSI(JSON 형식) - ex) { "number": "1", "mac": "00:70:69:47:2F:30" }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        Intent intent = getIntent();
//        exhibitState = intent.getStringArrayExtra("MuseumState");
        startDate = intent.getLongExtra("Time", 0);

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);

        getExhibitionData();
        setMuseMarkers(mapView);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        int result = intent.getIntExtra("finish_exhibition_num", -1);
        if(result >= 1 && result <= 6) {        //전시관의 QR코드를 찍었으면
            SharedPreferences.Editor editor = infoData.edit();  //해당 전시관에 대한 정보를 공유변수에 저장
            editor.putBoolean("IS_CHECK_" + result, true);
            editor.apply();
        }
        loadInfo();
    }

    // DB 전시관 및 전시해설 데이터 받아오기
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 저장된 값 가져오기
    private void loadInfo() {
        for(int i = 0 ; i < 6 ; i++) {
            int num = i + 1;
            isCheckQrArr[i] = infoData.getBoolean("IS_CHECK_" + num, false);    //true 없으면 false로
            Log.d(num + "전시관 QR체크여부 : ", isCheckQrArr[i] + "");
        }
    }

    public void setMuseMarkers(MapView mapView){

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.783564, 127.223225), 1, true);
        mapViewContainer.addView(mapView);
        mapView.setPOIItemEventListener(this);

        MapPOIItem marker1 = new MapPOIItem();
        marker1.setItemName("제1전시관");        //눌렀을때 말풍선
        marker1.setTag(1);

        MapPoint mapPoint1 = MapPoint.mapPointWithGeoCoord(36.783323, 127.221605);
        marker1.setMapPoint(mapPoint1);
        // 기본으로 제공하는 BluePin 마커 모양.
        if(exhibitionState[0].equals("1"))
            marker1.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[0].equals("0"))
            marker1.setMarkerType((MapPOIItem.MarkerType.YellowPin));
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker1.setShowCalloutBalloonOnTouch(false);
        //mapView.setImage
        //marker1.getMapPoint();


        MapPOIItem marker2 = new MapPOIItem();
        marker2.setItemName("제2전시관");
        marker2.setTag(2);

        MapPoint mapPoint2 = MapPoint.mapPointWithGeoCoord(36.783710, 127.221090);
        marker2.setMapPoint(mapPoint2);
        // 기본으로 제공하는 BluePin 마커 모양.
        //marker2.setMarkerType(MapPOIItem.MarkerType.BluePin);
        if(exhibitionState[1].equals("1"))
            marker2.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[1].equals("0"))
            marker2.setMarkerType((MapPOIItem.MarkerType.YellowPin));
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker2.setShowCalloutBalloonOnTouch(false);


        MapPOIItem marker3 = new MapPOIItem();
        marker3.setItemName("제3전시관");        //눌렀을때 말풍선
        marker3.setTag(3);

        MapPoint mapPoint3 = MapPoint.mapPointWithGeoCoord(36.784381, 127.220875);
        marker3.setMapPoint(mapPoint3);
        // 기본으로 제공하는 BluePin 마커 모양.
        //marker3.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker3.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker3.setShowCalloutBalloonOnTouch(false);

        if(exhibitionState[2].equals("1"))
            marker3.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[2].equals("0"))
            marker3.setMarkerType((MapPOIItem.MarkerType.YellowPin));



        MapPOIItem marker4 = new MapPOIItem();
        marker4.setItemName("제4전시관");        //눌렀을때 말풍선
        marker4.setTag(4);

        MapPoint mapPoint4 = MapPoint.mapPointWithGeoCoord(36.784888, 127.220961);
        marker4.setMapPoint(mapPoint4);
        // 기본으로 제공하는 BluePin 마커 모양.
        if(exhibitionState[3].equals("1"))
            marker4.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[3].equals("0"))
            marker4.setMarkerType((MapPOIItem.MarkerType.YellowPin));
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker4.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker4.setShowCalloutBalloonOnTouch(false);

        MapPOIItem marker5 = new MapPOIItem();
        marker5.setItemName("제5전시관");        //눌렀을때 말풍선
        marker5.setTag(5);

        MapPoint mapPoint5 = MapPoint.mapPointWithGeoCoord(36.785034, 127.221573);
        marker5.setMapPoint(mapPoint5);
        // 기본으로 제공하는 BluePin 마커 모양.
        if(exhibitionState[4].equals("1"))
            marker5.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[4].equals("0"))
            marker5.setMarkerType((MapPOIItem.MarkerType.YellowPin));
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker5.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker5.setShowCalloutBalloonOnTouch(false);

        MapPOIItem marker6 = new MapPOIItem();
        marker6.setItemName("제6전시관");        //눌렀을때 말풍선
        marker6.setTag(6);

        MapPoint mapPoint6 = MapPoint.mapPointWithGeoCoord(36.784982, 127.222463);
        marker6.setMapPoint(mapPoint6);
        // 기본으로 제공하는 BluePin 마커 모양.
        if(exhibitionState[5].equals("1"))
            marker6.setMarkerType(MapPOIItem.MarkerType.BluePin);
        else if(exhibitionState[5].equals("0"))
            marker6.setMarkerType((MapPOIItem.MarkerType.YellowPin));
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker6.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        marker6.setShowCalloutBalloonOnTouch(false);


        mapView.addPOIItem(marker1);
        mapView.addPOIItem(marker2);
        mapView.addPOIItem(marker3);
        mapView.addPOIItem(marker4);
        mapView.addPOIItem(marker5);
        mapView.addPOIItem(marker6);
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }
    //마커 선택시의 액션
    //팝업 띄우기
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        int TagNum = mapPOIItem.getTag();
        Intent intent = new Intent(NormalActivity.this, PopupMapActivity.class);
        intent.putExtra("TagNum", TagNum);
        startActivity(intent);
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

    public void timeOn(View view) {
        Intent intent = new Intent(NormalActivity.this, PopupTimeActivity.class);
        intent.putExtra("Time", startDate);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_top);
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
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
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
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
}
