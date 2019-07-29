package grad_project.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.view.ViewGroup;
import android.widget.Toast;

import net.daum.android.map.MapViewEventListener;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;


public class NormalActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    private Chronometer mChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        mChronometer = (Chronometer)findViewById(R.id.chronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        MapView mapView = new MapView(this);

        setMuseMarkers(mapView);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setMuseMarkers(MapView mapView){

        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.783564, 127.223225), 1, true);
        mapViewContainer.addView(mapView);
        mapView.setPOIItemEventListener(this);

        MapPOIItem marker1 = new MapPOIItem();
        marker1.setItemName("제1전시관");        //눌렀을때 말풍선
        marker1.setTag(1);

        MapPoint mapPoint1 = MapPoint.mapPointWithGeoCoord(36.783323, 127.221605);
        marker1.setMapPoint(mapPoint1);
        // 기본으로 제공하는 BluePin 마커 모양.
        marker1.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        //말풍선 출력여부
        //marker1.setShowCalloutBalloonOnTouch(false);

        MapPOIItem marker2 = new MapPOIItem();
        marker2.setItemName("제2전시관");
        marker2.setTag(2);

        MapPoint mapPoint2 = MapPoint.mapPointWithGeoCoord(36.783710, 127.221090);
        marker2.setMapPoint(mapPoint2);
        marker2.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);


        MapPOIItem marker3 = new MapPOIItem();
        marker3.setItemName("제3전시관");
        marker3.setTag(3);

        MapPoint mapPoint3 = MapPoint.mapPointWithGeoCoord(36.784381, 127.220875);
        marker3.setMapPoint(mapPoint3);
        marker3.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        MapPOIItem marker4 = new MapPOIItem();
        marker4.setItemName("제4전시관");        //눌렀을때 말풍선
        marker4.setTag(4);

        MapPoint mapPoint4 = MapPoint.mapPointWithGeoCoord(36.784888, 127.220961);
        marker4.setMapPoint(mapPoint4);
        marker4.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker4.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        MapPOIItem marker5 = new MapPOIItem();
        marker5.setItemName("제5전시관");
        marker5.setTag(5);

        MapPoint mapPoint5 = MapPoint.mapPointWithGeoCoord(36.785034, 127.221573);
        marker5.setMapPoint(mapPoint5);
        marker5.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker5.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        MapPOIItem marker6 = new MapPOIItem();
        marker6.setItemName("제6전시관");        //눌렀을때 말풍선
        marker6.setTag(6);

        MapPoint mapPoint6 = MapPoint.mapPointWithGeoCoord(36.784982, 127.222463);
        marker6.setMapPoint(mapPoint6);
        marker6.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker6.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

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

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {  //마커 클릭 이벤트
        Intent intent = new Intent(NormalActivity.this, PopupMapActivity.class);
        intent.putExtra("AUDIENCE", mapPOIItem.getItemName());
        startActivityForResult(intent, 0);
    }
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {  //말풍선 클릭 이벤트1
    }
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {  //말풍선 이벤트2
    }
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {  //마커 이동 이벤트
    }
}
