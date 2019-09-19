package grad_project.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotiService extends Service {
    public static final int MSG_SEND_TO_ACTIVITY = 0;
    SharedPreferences infoData;
    IBinder mBinder = new NotiBinder();
    boolean is_notify = false;  // 커스텀 노티 기능 ON/OFF 여부(SharedPreferences 사용)
    boolean is_start = false;   // 관람 시작 여부
    boolean is_end = false;     // 관람 종료 여부
    boolean is_initialize = false;  // 초기화 진행 여부
    boolean is_setIsStart = false;
    String s_id = "";             // 로그인된 사용자 아이디
    String s_stateNoti = "";     // 커스텀 노티에 표시될 관람 상태 내용(관람 시작 전/관람 진행 중/관람 종료)
    long l_startTime = 0;       // 시작시간 서버에서 받아옴
    long l_nowTime = 0;         // 현재 시간 값 저장
    long l_elapseTime = -1;     // 진행시간 계산(현재시간-시작시간)
    String s_elapseTime = "";   // 진행시간 커스텀 노티에 표시하기 위함
    int i_state = -1;           // 현재 진행 상태(0:시작 안함, 1:진행 중, 2:종료)
    boolean is_notification;    // 커스텀 노티가 띄워져 있는지 여부
    boolean is_defaultNoti;     // 기본 노티가 띄워져 있는지 여부
    boolean is_warned;          // 위치 벗어났을 때 1회 경고가 되었는지 여부
    boolean is_inLocation;      // 검사한 위치 상태 임시 저장용 변수
    int location_count;         // 위치 검사 결과가 연속 false일 때 초기화하기 위해 카운트
    boolean is_timeendNoti;

    /* 노티 채널 */
    Notification notification;
    NotificationManager notiManager, notiManager_default, notiManager_warning;
    final String channelId = "notiChannel";
    final String channelName = "관람 상태 알림";
    final String channelId_default = "notiChannel_2";
    final String channelName_default = "위치 검사";
    final String channelId_warning = "notiChannel_warning";
    final String channelName_warning = "중요 알림";
    final int NOTIFICATION_ID = 1;
    final int NOTIFICATION_ID_default = 2;
    final int NOTIFICATION_ID_warning = 3;
    final int NOTIFICATION_ID_network = 4;
    final int NOTIFICATION_ID_timeend = 5;
    int requestId, requestId_default, requestId_warning;
    NotificationCompat.Builder builder;
    NotificationCompat.Builder builder_default;
    NotificationCompat.Builder builder_warning;
    final int nFLAG_INITIALIZE = 0;
    final int nFLAG_START = 1;
    final int nFLAG_FINISH = 2;
    final int nFLAG_TIME = 10;

    Intent notificationMainIntent;
    Intent notificationCheckIntent;

    boolean network_state;

    /* 타이머 */
    final int NETWORK_DELAY = 60; // 네트워크 통신 시간 간격 : 값 변경할 때는 초 단위로 변경
    StateTimerHandler stateTimerhandler;
    final int START_TIMER_START = 100;

    final int END_TIMER_START = 200;
    TimeTimerHandler timeTimerHandler;
    final int NOWTIME_TIMER_START = 101;

    final int LOCATION_DELAY = 100; // 위치검사 시간 간격 : 값 변경할 때는 초 단위로 변경, 3회 연속 검사함(100이면 5분마다 검사)
    LocationTimerHandler locationTimerHandler;
    final int LOCATION_TIMER_START = 300;

    /*GPS*/
    String provider;    //위치정보
    double longitude;  //위도
    double latitude;   //경도
    double altitude;   //고도
    private MapPoint gpsPosition;  //현재 GPS 위치

    class NotiBinder extends Binder {
        NotiService getService() {
            return NotiService.this;
        }
    }

    public double getLongitude(){
        return longitude;
    }

    public double gatLatitude(){
        return latitude;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("NotiService", "서비스 onBind 시작");

        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // 사용자 아이디와 커스텀 노티 ON/OFF 여부 가져옴
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");
        is_notify = infoData.getBoolean("NOTIFICATION", false);
        if (!infoData.getBoolean("IS_LOGIN", false)) {
            Log.d("서비스", "로그인 안되어있음");
            stopSelf();
        }

        // 초기화
        is_notification = false;
        stateTimerhandler = new StateTimerHandler();
        timeTimerHandler = new TimeTimerHandler();
        locationTimerHandler = new LocationTimerHandler();

        notificationMainIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationCheckIntent = new Intent(getApplicationContext(), CheckActivity.class);

        location_count = 0;
        is_inLocation = false;
        is_warned = false;
        is_timeendNoti = true;

        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager_default = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager_warning = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        cm.registerNetworkCallback(builder.build(),
                new ConnectivityManager.NetworkCallback()
                {
                    @Override
                    public void onAvailable( Network network )
                    {
                        //네트워크 연결됨
                        Log.d("NETWORK", "연결됨");
                        notiManager_warning.cancel(NOTIFICATION_ID_network);
                        network_state = true;
                    }

                    @Override
                    public void onLost( Network network )
                    {
                        //네트워크 끊어짐
                        Log.d("NETWORK", "연결 끊어짐");
                        network_state = getNetworkInfo();
                        networkDisconnected();
                    }
                } );

        // 오레오 이상 노티 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 커스텀 노티 생성할 알림 채널(중요도 LOW:무음)
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.setShowBadge(false);
            notiManager.createNotificationChannel(mChannel);
            // 커스텀 노티 기능 OFF일 때 기본 생성될 알림 채널(중요도 NONE:무음 및 최소화인데 최소화는 자동으로 안되는듯)
            int importance_default = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel mChannel_default = new NotificationChannel(channelId_default, channelName_default, importance_default);
            mChannel_default.setShowBadge(false);
            mChannel_default.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notiManager_default.createNotificationChannel(mChannel_default);
            // 위치 이탈했을 때 경고 알림용 채널(중요도 HIGH:소리 및 팝업)
            int importance_warning = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel_warning = new NotificationChannel(channelId_warning, channelName_warning, importance_warning);
            notiManager_warning.createNotificationChannel(mChannel_warning);
        }

        /* GPS 초기화*/
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NotiService", "서비스 시작");

        // 서비스 시작하면 시작 여부와 종료 여부(시작했을 때) 받아옴
        if (!getStartState()) { // 네트워크 통신 오류 예외처리
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
//            disconnectedNetwork();
        }
        if (is_start) {
            if (!isEnd()) { // 네트워크 통신 오류 예외처리
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
//                disconnectedNetwork();
            }
        }

        // 노티피케이션 생성
        if (is_notify) {    // 사용자가 알림 기능을 ON 했으면
            createNotification();   // 커스텀 노티 만들고
            startForeground(NOTIFICATION_ID, notification); // Foreground로 실행(이래야 앱이 꺼져도 서비스가 안죽음)
            is_notification = true;
            if (is_defaultNoti) {   // 기본 노티가 띄워져 있으면 죽임
                notiManager_default.cancel(NOTIFICATION_ID_default);
                is_defaultNoti = false;
            }
        } else {    // 사용자가 알림 기능을 OFF 했으면
            // 오레오 이상 버전은 죽지 않는 서비스를 실행하기 위해 노티를 꼭 띄워야 함
            // 오레오 미만은 노티 띄울 필요 없어서 따로 동작하는 코드 없음
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 기본적인 노티 생성 과정
                builder_default = new NotificationCompat.Builder(getApplicationContext(), channelId_default);

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                requestId_default = (int) System.currentTimeMillis();

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        requestId_default, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder_default.setContentTitle("독립기념관 관람")
                        .setContentText("앱 열기")
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setAutoCancel(false)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setVisibility(Notification.VISIBILITY_SECRET)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent);

                Notification temp_notification;
                temp_notification = builder_default.build();

                // 노티 빌드 후 foreground로 실행
                startForeground(NOTIFICATION_ID_default,temp_notification);
                is_defaultNoti = true;
                if (is_notification) {
                    notiManager.cancel(NOTIFICATION_ID);
                    is_notification = false;
                }
            }
        }
        // 시작여부 일정 시간마다 받아오는 핸들러 실행
        stateTimerhandler.sendEmptyMessage(START_TIMER_START);
        timeTimerHandler.sendEmptyMessage(NOWTIME_TIMER_START);
        locationTimerHandler.sendEmptyMessage(LOCATION_TIMER_START);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("NotiService", "서비스 종료");

        notiManager.cancel(NOTIFICATION_ID);
        notiManager_default.cancel(NOTIFICATION_ID_default);

        is_notification = false;
        is_defaultNoti = false;

        stateTimerhandler.removeMessages(START_TIMER_START);
        stateTimerhandler.removeMessages(END_TIMER_START);
        timeTimerHandler.removeMessages(NOWTIME_TIMER_START);
        locationTimerHandler.removeMessages(LOCATION_TIMER_START);

        Intent intent = new Intent("Service Destroyed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitude = location.getAltitude();
            Log.d("gps", "위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도  : " + altitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public boolean checkInLocation() {
        /*독립기념관*/
        MapPoint leftB = MapPoint.mapPointWithGeoCoord(36.782945, 127.213537);
        MapPoint RightT = MapPoint.mapPointWithGeoCoord(36.779085, 127.237411);

        /*한기대*/
        //MapPoint leftB = MapPoint.mapPointWithGeoCoord(36.770386, 127.276913);
        //MapPoint RightT = MapPoint.mapPointWithGeoCoord(36.760433, 127.286772);

        MapPointBounds boundary = new MapPointBounds(leftB, RightT);
        gpsPosition = MapPoint.mapPointWithGeoCoord(latitude, longitude);

        return boundary.contains(gpsPosition);
    }

    public void setOutLocation() {
        Log.d("위치 초기화", "위치 초기화 시작!!");

        Log.d("SERVICE", "위치 초기화 진행 0단계");
        Thread init_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (is_initialize) {
                    Log.d("SERVICE", "위치 초기화 진행 1단계");
                    if (setInitialize()) {
                        if (is_setIsStart) {
                            Log.d("SERVICE", "위치 초기화 진행 2단계");
                            stateTimerhandler.removeMessages(END_TIMER_START);
                            stateTimerhandler.sendEmptyMessage(START_TIMER_START);
                            i_state = 0;
                            s_stateNoti = "관람 시작 전";
                            updateNotification(nFLAG_INITIALIZE);
                            is_start = false;
                            is_initialize = false;
                            is_setIsStart = false;

                            SharedPreferences.Editor editor = infoData.edit();
                            NormalActivity.NormalClass normalClass = new NormalActivity.NormalClass();
                            int imgNum = normalClass.getImgNumByExhibition();
                            for (int i = 0; i < 6; i++) {
                                int num = i+1;
                                editor.remove("IS_CHECK_QR_" + num);
                                editor.remove("IS_CHECK_PIC_" + num);
                                for (int j = 0; j < imgNum; j++) {
                                    editor.remove("IS_CHECK_PIC_" + num + "-" + (j+1));
                                    editor.remove("RANDOM_IMG_" + num + "_" + (j+1));
                                }
                            }
                            editor.remove("IS_RANDOM_IMG");
                            editor.apply();

                            // 초기화되었다는 정보를 broadcast 한다
                            Intent intent = new Intent("Initialized!");
                            LocalBroadcastManager.getInstance(NotiService.this).sendBroadcast(intent);
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent in_main = new Intent(NotiService.this, MainActivity.class);
                            startActivity(in_main);
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        init_thread.run();

        Log.d("위치 초기화", "위치 초기화 끝!!");
    }

    // 지정한 시간 단위로 시작/종료 여부 서버에서 받아옴
    private class StateTimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_TIMER_START) {
                if (is_inLocation) {
                    // 관람 시작 전 : 관람 시작 여부 검사
                    if (getStartState()) {
                        Log.d("ISSTART_SERVICE", Boolean.toString(is_start));
                        // 관람 시작 했으면 관람 진행 상태로 넘어감
                        if (is_start) {
                            stateTimerhandler.removeMessages(START_TIMER_START);
                            if (i_state == 0) {
                                i_state = 1;
                                s_stateNoti = "관람 진행 중";
                                updateNotification(nFLAG_START);
                            }
                            this.sendEmptyMessage(END_TIMER_START);
                            Log.d("START TIMER", "RUNNING");
                        } else {
                            this.sendEmptyMessageDelayed(START_TIMER_START, NETWORK_DELAY * 1000);
                        }
                    }
                } else {
                    Log.d("ISINLOCATION_SERVICE", "밖에 나가있음");
                    this.sendEmptyMessageDelayed(START_TIMER_START, NETWORK_DELAY * 1000);
                }
            }
            // 관람 진행 중 : 관람 종료 여부 및 초기화 여부 검사
            else if (msg.what == END_TIMER_START) {
                if (is_inLocation) {    // 독립기념관 내부에 있을 때만 검사
                    if (getStartState()) {
                        // 서버에서 관람기록 초기화했을 경우 앱에서도 초기화
                        if (!is_start) {
                            if (i_state == 1) {
                                stateTimerhandler.removeMessages(END_TIMER_START);
                                this.sendEmptyMessage(START_TIMER_START);
                                i_state = 0;
                                s_stateNoti = "관람 시작 전";
                                updateNotification(nFLAG_INITIALIZE);
                                is_start = false;
                            }
                        }
                    }
                    if (l_elapseTime > 7140000) {
//                    if (l_elapseTime > 0) {
                        Log.d("NOTISERVICE", "두시간 넘었음!");
                        if (isEnd()) {
                            // 관람 종료되었으면 관람 종료했다고 띄워줌
                            if (is_end) {
                                stateTimerhandler.removeMessages(END_TIMER_START);
                                timeTimerHandler.removeMessages(NOWTIME_TIMER_START);
                                s_stateNoti = "관람 종료";
                                i_state = 2;

                                makeAlarmNotification("알림", "관람 종료 확인되었습니다.",
                                        NOTIFICATION_ID_timeend, notificationCheckIntent);

                                Intent intent = new Intent("Finished!");
                                LocalBroadcastManager.getInstance(NotiService.this).sendBroadcast(intent);

                                Log.d("END TIMER", "RUNNING");
                                stopSelf();
                            } else {
                                this.sendEmptyMessageDelayed(END_TIMER_START, NETWORK_DELAY * 1000);
                            }
                        }
                    } else {
                        Log.d("NOTISERVICE", "두시간 안 넘었음!");
                    }
                }
            } else {
                Log.d("ISINLOCATION_SERVICE", "밖에 나가있음");
                this.sendEmptyMessageDelayed(END_TIMER_START, NETWORK_DELAY * 1000);
            }
        }
    }

    // 경과시간 새로고침
    private class TimeTimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == NOWTIME_TIMER_START) {
                Log.d("TimeTimerHandler", "Running");
                if (is_start) {
                    l_nowTime = System.currentTimeMillis();
                    l_elapseTime = l_nowTime - l_startTime;
                    if (is_notification) {
                        long temp_time = l_elapseTime / 1000;
                        if (temp_time % 60 >= 0 && temp_time % 60 < 30) {
                            Date elapseDate = new Date(l_elapseTime);
                            SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            sdfElapse.setTimeZone(TimeZone.getTimeZone("UTC"));
                            s_elapseTime = sdfElapse.format(elapseDate);
                            updateNotification(nFLAG_TIME);
                            Log.d("TIME TIMER", "NOTI UPDATE");
                        }
                    }
                    Log.d("TIME TIMER", Long.toString(l_elapseTime));
                    if (l_elapseTime > 7200000) {
                        if (!is_timeendNoti) {
                            makeAlarmNotification("알림", "관람 시간이 2시간을 경과했습니다.",
                                    NOTIFICATION_ID_timeend, notificationCheckIntent);
                            is_timeendNoti = true;
                        }
                    } else {
                        is_timeendNoti = false;
                    }
                    this.sendEmptyMessageDelayed(NOWTIME_TIMER_START, 30000);
                } else {
                    this.sendEmptyMessageDelayed(NOWTIME_TIMER_START, 30000);
                }
            }
        }
    }

    // 시간단위 위치추적
    private class LocationTimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOCATION_TIMER_START) {
                if (checkInLocation()) {
                    location_count = 0;
                    is_inLocation = true;
                    is_warned = false;
                    Log.d("LOCATIONTIMER", "영역 안에 있음");
                    this.sendEmptyMessageDelayed(LOCATION_TIMER_START, LOCATION_DELAY * 1000);
                } else {
                    location_count++;
                    if (location_count > 2) {
                        is_inLocation = false;
                        Log.d("LOCATIONTIMER", "영역 밖에 있음");
                        if (is_start) {
                            if (is_warned) {
                                location_count = 0;
                                is_warned = false;
                                is_initialize = true;
                                Log.d("I_STATE", Integer.toString(i_state));
                                setOutLocation();
                                builder_warning.setContentText("관람 기록이 초기화되었습니다.\n안내센터에 문의하세요.");
                                notiManager_warning.notify(NOTIFICATION_ID_warning, builder_warning.build());
                                makeAlarmNotification("경고", "관람 기록이 초기화되었습니다. 안내 센터에 문의하세요.",
                                        NOTIFICATION_ID_warning, notificationMainIntent);
                            } else {
                                makeAlarmNotification("경고", "외부로 나가면 관람 기록이 초기화됩니다!",
                                        NOTIFICATION_ID_warning, notificationMainIntent);
                                is_warned = true;
                                location_count = 0;
                            }
                        }
                    }

                    this.sendEmptyMessageDelayed(LOCATION_TIMER_START, LOCATION_DELAY * 1000);
                }
            }
        }
    }

    private void createNotification() {

        if (getStartState()) {
            if (is_start) {
                s_stateNoti = "관람 진행 중";
                i_state = 1;
                if (isEnd()) {
                    if (is_end) {
                        s_stateNoti = "관람 종료";
                        i_state = 2;
                        stopSelf();
                    }
                } else {    // 네트워크 통신 오류 예외처리
//                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
//                    disconnectedNetwork();
                    s_stateNoti = "Error";
                    i_state = -1;
                }
            } else {
                s_stateNoti = "관람 시작 전";
                i_state = 0;
            }
        } else {    // 네트워크 통신 오류 예외처리
//                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
//            disconnectedNetwork();
            s_stateNoti = "Error";
            i_state = -1;
        }

        if (is_start) {
            l_nowTime = System.currentTimeMillis();
            l_elapseTime = l_nowTime - l_startTime;
        }

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        requestId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                requestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent in_openMain = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi_openMain = PendingIntent.getActivity(getApplicationContext(), requestId, in_openMain, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent in_openCheck = new Intent(getApplicationContext(), CheckActivity.class);
        PendingIntent pi_openCheck = PendingIntent.getActivity(getApplicationContext(), requestId, in_openCheck, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        if (is_start) {
            Date elapseDate = new Date(l_elapseTime);
            SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdfElapse.setTimeZone(TimeZone.getTimeZone("UTC"));
            s_elapseTime = sdfElapse.format(elapseDate);
            remoteViews.setTextViewText(R.id.tv_time, s_elapseTime);
        } else {
            remoteViews.setTextViewText(R.id.tv_time, "-");
        }
        remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
        remoteViews.setOnClickPendingIntent(R.id.bt_openMain, pi_openMain);
        remoteViews.setOnClickPendingIntent(R.id.bt_openCheck, pi_openCheck);
        builder.setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(false)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        notification = builder.build();

    }

    private void updateNotification(int flag) {
        if (is_notification) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);

            switch (flag) {
                case nFLAG_INITIALIZE:
                    remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
                    remoteViews.setTextViewText(R.id.tv_time, "-");
                    break;
                case nFLAG_START:
                    remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
                    remoteViews.setTextViewText(R.id.tv_time, s_elapseTime);
                    break;
                case nFLAG_FINISH:
                    remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
                    remoteViews.setTextViewText(R.id.tv_time, "-");
                    break;
                case nFLAG_TIME:
                    remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
                    remoteViews.setTextViewText(R.id.tv_time, s_elapseTime);
                    break;
            }

            Intent in_openMain = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pi_openMain = PendingIntent.getActivity(getApplicationContext(), requestId, in_openMain, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent in_openCheck = new Intent(getApplicationContext(), CheckActivity.class);
            PendingIntent pi_openCheck = PendingIntent.getActivity(getApplicationContext(), requestId, in_openCheck, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.bt_openMain, pi_openMain);
            remoteViews.setOnClickPendingIntent(R.id.bt_openCheck, pi_openCheck);
            builder.setContent(remoteViews);

            notiManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void makeAlarmNotification(String title, String text, int id, Intent intent) {
        builder_warning = new NotificationCompat.Builder(getApplicationContext(), channelId_warning);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        requestId_warning = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                requestId_warning, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder_warning.setContentTitle(title)
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);

        notiManager_warning.notify(id, builder_warning.build());
    }

    private boolean getStartState() {
        DdConnect dbConnect = new DdConnect();
        try {
            String result = dbConnect.execute(dbConnect.GET_ISSTART, s_id).get();
            Log.d("GET_ISSTART", result);
            if (!result.equals("-1")) {
                is_start = !result.equals("0");
                if (is_start) {
                    SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    l_startTime = df.parse(result).getTime();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isEnd() {
        DdConnect dbConnect = new DdConnect();
        try {
            String result = dbConnect.execute(dbConnect.GET_ISEND, s_id).get();
            Log.d("GET_ISEND", result);
            if (!result.equals("-1")) {
                is_end = !result.equals("0");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean setInitialize() {
        DdConnect dbConnect = new DdConnect();
        try {
            String result = dbConnect.execute(dbConnect.SET_ISSTART, s_id).get();
            Log.d("SET_ISSTART", result);
            if (!result.equals("-1")) {
                is_setIsStart = !result.equals("0");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            return false;
        }
    }

    private void networkDisconnected() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("NETWORK", Boolean.toString(network_state));
                if (!network_state) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                        makeAlarmNotification("경고", "네트워크 연결이 해제되었습니다. 앱 이용을 위해 네트워크를 연결해주세요!",
                                NOTIFICATION_ID_network, intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                        makeAlarmNotification("경고", "네트워크 연결이 해제되었습니다. 앱 이용을 위해 네트워크를 연결해주세요!",
                                NOTIFICATION_ID_network, intent);
                    }
                }
            }
        }).run();
    }
}
