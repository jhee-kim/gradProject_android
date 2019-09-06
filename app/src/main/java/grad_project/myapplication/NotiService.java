package grad_project.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotiService extends Service {
    SharedPreferences infoData;
    IBinder mBinder = new NotiBinder();
    boolean is_start = false;
    boolean is_end = false;
    String s_id = "";
    String s_stateNoti = "";
    long l_startTime = 0;
    long l_nowTime = 0;
    long l_elapseTime = -1;
    String s_elapseTime = "";
    int i_state = -1;
    boolean is_timeUpdated = false;

    NotificationManager notiManager;
    String channelId = "notiChannel";
    String channelName = "관람 상태 알림";
    int NOTIFICATION_ID = 1;
    int requestId;
    NotificationCompat.Builder builder;

    StateTimerHandler stateTimerhandler;
    int START_TIMER_START = 100;
    int END_TIMER_START = 200;

    TimeTimerHandler timeTimerHandler;
    int NOWTIME_TIMER_START = 101;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 1, 실패 0 반환)
    public static final String GET_ISEND = BASE_PATH + "get_isEnd.php";    //전시 종료 여부 받기(종료됨 : 시간, 종료안됨 : 0)

    class NotiBinder extends Binder {
        NotiService getService() {
            return NotiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("NotiService", "서비스 onBind 시작");
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");

        stateTimerhandler = new StateTimerHandler();
        timeTimerHandler = new TimeTimerHandler();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NotiService", "서비스 시작");
        createNotification();

        stateTimerhandler.sendEmptyMessage(START_TIMER_START);
        timeTimerHandler.sendEmptyMessage(NOWTIME_TIMER_START);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        Log.d("NotiService", "서비스 종료");
        notiManager.cancel(NOTIFICATION_ID);
        stateTimerhandler.removeMessages(START_TIMER_START);
        stateTimerhandler.removeMessages(END_TIMER_START);
        timeTimerHandler.removeMessages(NOWTIME_TIMER_START);

        super.onDestroy();
    }

    // 3초 단위로 시작 여부 서버에서 받아옴
    private class StateTimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_TIMER_START) {
                if (getStartState()) {
                    if (is_start) {
                        stateTimerhandler.removeMessages(START_TIMER_START);
                        if (i_state == 0) {
                            s_stateNoti = "관람 진행 중";
                            i_state = 1;
                            updateNotification();
                        }
                        this.sendEmptyMessage(END_TIMER_START);
                        Log.d("START TIMER", "RUNNING");
                    } else {
                        this.sendEmptyMessageDelayed(START_TIMER_START, 5000);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
            }
            else if (msg.what == END_TIMER_START) {
                if (getStartState()) {
                    if (!is_start) {
                        if (i_state == 1) {
                            stateTimerhandler.removeMessages(END_TIMER_START);
                            this.sendEmptyMessage(START_TIMER_START);
                            s_stateNoti = "관람 시작 전";
                            updateNotification();
                            i_state = 0;
                        }
                    }
                }
                if (isEnd()) {
                    if (is_end) {
                        stateTimerhandler.removeMessages(END_TIMER_START);
                        timeTimerHandler.removeMessages(NOWTIME_TIMER_START);
                        s_stateNoti = "관람 종료";
                        i_state = 2;
                        updateNotification();
                        Log.d("END TIMER", "RUNNING");
                    } else {
                        this.sendEmptyMessageDelayed(END_TIMER_START, 5000);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 3초 단위로 시작 여부 서버에서 받아옴
    private class TimeTimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == NOWTIME_TIMER_START) {
                if (is_start) {
                    l_nowTime = System.currentTimeMillis();
                    l_elapseTime = l_nowTime - l_startTime;
                    long temp_time = l_elapseTime / 1000;
                    if (temp_time % 60 == 0) {
                        Date elapseDate = new Date(l_elapseTime);
                        SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        sdfElapse.setTimeZone(TimeZone.getTimeZone("UTC"));
                        s_elapseTime = sdfElapse.format(elapseDate);
                        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
                        remoteViews.setTextViewText(R.id.tv_time, s_elapseTime);
                        builder.setContent(remoteViews);

                        notiManager.notify(NOTIFICATION_ID, builder.build());
                        Log.d("TIME TIMER", "NOTI UPDATE");
                    }
                    Log.d("TIME TIMER", Long.toString(l_elapseTime));
                    this.sendEmptyMessageDelayed(NOWTIME_TIMER_START, 1000);
                } else {
                    this.sendEmptyMessageDelayed(NOWTIME_TIMER_START, 1000);
                }
            }
        }
    }

    public void createNotification() {
        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (getStartState()) {
            if (is_start) {
                s_stateNoti = "관람 진행 중";
                i_state = 1;
                if (isEnd()) {
                    if (is_end) {
                        s_stateNoti = "관람 종료";
                        i_state = 2;
                    }
                } else {    // 네트워크 통신 오류 예외처리
//                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                    s_stateNoti = "Error";
                    i_state = -1;
                }
            } else {
                s_stateNoti = "관람 시작 전";
                i_state = 0;
            }
        } else {    // 네트워크 통신 오류 예외처리
//                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            s_stateNoti = "Error";
            i_state = -1;
        }

        if (is_start) {
            l_nowTime = System.currentTimeMillis();
            l_elapseTime = l_nowTime - l_startTime;
        }

        // 오레오 이상 노티 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notiManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        requestId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                requestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
        if (is_start) {
            Date elapseDate = new Date(l_elapseTime);
            SimpleDateFormat sdfElapse = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdfElapse.setTimeZone(TimeZone.getTimeZone("UTC"));
            s_elapseTime = sdfElapse.format(elapseDate);
            remoteViews.setTextViewText(R.id.tv_time, s_elapseTime);
        } else {
            remoteViews.setTextViewText(R.id.tv_time, "-");
        }
//        remoteViews.setOnClickPendingIntent();
        builder.setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(android.R.drawable.btn_star)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        notiManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void updateNotification() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.tv_state, s_stateNoti);
        builder.setContent(remoteViews);
        notiManager.notify(NOTIFICATION_ID, builder.build());
    }

    /* DB-서버 통신 파트 */
    // 관람 시작이 되었는지 여부 받아오는 메소드(연결 상태 return)
    public boolean getStartState() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            if (result.equals("ERROR")) {
                return false;
            } else {
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

    public boolean isEnd() {
        FinishTask finishTask = new FinishTask(this);
        try {
            String result = finishTask.execute(GET_ISEND, s_id).get();
            Log.d("ISEND RESULT", result);
            if (result.equals("ERROR")) {
                return false;
            } else {
                is_end = !result.equals("0");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /***** 서버 통신 *****/
    // 관람 시작 여부 받아오는 부분
    public static class GetIsStartTask extends AsyncTask<String, Void, String> {

        GetIsStartTask(NotiService context) {
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            /*출력값*/
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String id = params[1];
            String postParameters = "&id=" + id;
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
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

    // 관람 종료 여부 정보 받아오기
    public static class FinishTask extends AsyncTask<String, Void, String> {

        FinishTask(NotiService context) {

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String id = params[1];
            String postParameters = "&id=" + id;
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
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
                return "ERROR";
            }
        }
    }
}
