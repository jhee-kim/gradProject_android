package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/*
***** 도움말 *****
* (변수 접두어)
*   - i : integer
*   - s : string
*   - h : 서버 통신으로 주고받는 값
*   - bt : button
*
* (주요 변수)
*   - name : 사용자 이름
*   - number : 군번
*   - participation : 참여 구분(0:일반관람 / 1:전시 해설)
*   - division : 군종 구분(0:육군 / 1:해군 / 2:공군 / 3:해병대)
*   - temper : 부대명
*   - phone : 휴대폰 번호
*   - destination : 행선지
*
*/


public class MainActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    boolean is_login = false;
    boolean is_registered = false;
    boolean is_autoLogin;
    String s_id, s_name, s_number;
    DrawerLayout drawerLayout;
    View slideView;
    Button bt_openMap, bt_registration, bt_certificate;
    RelativeLayout bt_openMenu, bt_closeMenu;
    LinearLayout bt_viewtime, bt_myinfo, bt_certificate_2, bt_registration_2, bt_homepage, bt_information;
    private boolean is_start = false;
//    private boolean is_finish = false;    // 차후 구현 예정
    private String[][] exhibitionState = new String[6][2];      // 전시관 오픈 여부(1 : open, 0 : close)
    private String[][] exhibitionQrCode = new String[6][2];     // 전시관 QR코드 URL
//    String[][] exhibitionRssId = new String[6][2];    // 차후 구현 예정

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 1, 실패 0 반환)
//    public static final String GET_NARRATOR = BASE_PATH + "get_narrator.php";            //관람등록시 전시해설 on/oof 여부(해설자 스케줄 확인)  - 미구현
    public static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";          //각 전시관 별 개설 여부(JSON 형식) - ex) { "number": "1", "isOpen": "1" }
    public static final String GET_QR = BASE_PATH + "get_qr.php";                              //각 전시관 별 qr코드 파일 위치(JSON 형식) - ex) { "number": "1", "address": "http://35.221.108.183/QR/1.png" }
//    public static final String GET_RSSID = BASE_PATH + "get_mac.php";                        //각 전시관 별 RSSI(JSON 형식) - ex) { "number": "1", "mac": "00:70:69:47:2F:30" }
//    public static final String UPDATE_AUDIENCE = BASE_PATH + "update_audience.php";    //전시 종료 값 보내기(성공 1, 실패 0 반환)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        // 메인 화면 버튼
        bt_openMap = findViewById(R.id.bt_open_map);
        bt_registration = findViewById(R.id.bt_registration);
        bt_certificate = findViewById(R.id.bt_certificate);

        // 메뉴 관련 버튼
        drawerLayout = findViewById(R.id.layout_main);
        slideView = findViewById(R.id.layout_slide);
        bt_openMenu = findViewById(R.id.bt_open_menu);
        bt_closeMenu = findViewById(R.id.bt_close_menu);

        bt_openMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(slideView);
            }
        });

        bt_closeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(slideView);
            }
        });

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        is_autoLogin = infoData.getBoolean("IS_AUTOLOGIN", false);

        if (is_autoLogin) {
            loadInfo();
            is_login = true;
        } else {
            is_login = false;
        }

        // 메뉴 버튼 id 불러오기
        bt_viewtime = findViewById(R.id.bt_viewtime);
        bt_myinfo = findViewById(R.id.bt_myinfo);
        bt_certificate_2 = findViewById(R.id.bt_certificate_2);
        bt_registration_2 = findViewById(R.id.bt_registration_2);
        bt_homepage = findViewById(R.id.bt_homepage);
        bt_information = findViewById(R.id.bt_information);

        /* 메뉴 버튼 온클릭리스너 설정 */
        // 홈페이지 버튼
        bt_homepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.i815.or.kr/"));
                startActivity(intent);
            }
        });
        // 도움말 버튼
        bt_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "도움말 버튼 눌림", Toast.LENGTH_SHORT).show();
            }
        });

        getExhibitionData();
        getStartState();
        resumeActivity();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("RESTART", "onRestart()");
        resumeActivity();
    }

    /* DB-서버 통신 파트 */
    // DB에서 데이터 새로 받아오는 메소드
    public void getStartState() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            is_start = result.equals("1");
            Log.d("ISSTART", Boolean.toString(is_start));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // DB 전시관 및 전시해설 데이터 받아오기
    public void getExhibitionData() {
        // 전시관 오픈 여부
        GetExhibitionTask task = new GetExhibitionTask(this);
        try {
            String result = task.execute(GET_EXHIBITION).get();
            JSONObject jResult = new JSONObject(result);
            JSONArray jArray = jResult.getJSONArray("result");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                exhibitionState[i][0] = jObject.getString("number");
                exhibitionState[i][1] = jObject.getString("isOpen");

                Log.d("EXHIBITION", exhibitionState[i][0] + " : " + exhibitionState[i][1]);
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
                exhibitionQrCode[i][0] = jObject.getString("number");
                exhibitionQrCode[i][1] = jObject.getString("address");

                Log.d("EXHIBITION_QRCODE", exhibitionQrCode[i][0] + " : " + exhibitionQrCode[i][1]);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    // 액티비티 내용 새로고침 하는 메소드
    public void resumeActivity() {
        final ImageView iv_registration = findViewById(R.id.iv_registration);
        loadInfo();
        /* 액티비티 화면 내용 설정 */
        // 최초 등록 버튼 활성화 및 비활성화
        if (is_login) {
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(false);
            iv_registration.setEnabled(false);
            iv_registration.setColorFilter(Color.parseColor("#ffE0E0E0"), PorterDuff.Mode.SRC_IN);
        } else {
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(true);
            iv_registration.setEnabled(true);
            iv_registration.setColorFilter(null);
        }
        // 로그인 상태에 따른 화면 내용 표시
        if (is_login) {
            // 메뉴 맨 위에 표시되는 사용자 이름 설정
            Button bt_personal = findViewById(R.id.bt_personal);
            String msg_slide = s_name + " 님 >";
            bt_personal.setText(msg_slide);
            bt_personal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, PersonalActivity.class);
                    startActivity(intent);
                }
            });
            // 메인 맨 위에 표시되는 사용자 이름 설정
            TextView tv_name = findViewById(R.id.tv_name);
            String msg_main = s_name + " 님, 안녕하세요.";
            tv_name.setText(msg_main);
            // 로그아웃 버튼 생성
            RelativeLayout bt_logout = findViewById(R.id.bt_logout);
            bt_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLogout(view);
                }
            });
            bt_logout.setEnabled(true);
            bt_logout.setVisibility(View.VISIBLE);
        } else {
            // 메뉴 맨 위에 표시되는 사용자 이름 설정
            Button bt_personal = findViewById(R.id.bt_personal);
            bt_personal.setText("로그인해주세요 >");
            bt_personal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0);
                }
            });
            // 메인 맨 위에 표시되는 사용자 이름 설정
            TextView tv_name = findViewById(R.id.tv_name);
            tv_name.setText("로그인해주세요");
            RelativeLayout bt_logout = findViewById(R.id.bt_logout);
            bt_logout.setEnabled(false);
            bt_logout.setVisibility(View.INVISIBLE);
        }

        /* 메뉴 버튼 온클릭리스너 설정 */
        // 최초 등록 버튼
        bt_registration_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!is_login) {
                    Intent intent = new Intent(MainActivity.this, RegistActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });
        // 내 정보 버튼
        bt_myinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_login) {
                    Intent intent = new Intent(MainActivity.this, PersonalActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0);
                }
            }
        });
        // 확인증 버튼
        bt_certificate_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_login) {
                    Intent intent = new Intent(MainActivity.this, ConfirmActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0);
                }
            }
        });
        // 관람 현황 버튼
        bt_viewtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_login) {
                    Intent intent = new Intent(MainActivity.this, CheckActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 로그인 완료했을 경우
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                is_login = true;
            }
        }

        // 최초 등록 완료했을 경우(자동 로그인 됨)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                is_login = true;
            }
        }
    }

    // 메인 화면 버튼
    public void onClick(View v) {
        // 관람하기 버튼
        if (v == bt_openMap) {
            getStartState();
            if (is_login) {
                if (is_start) {
                    Intent intent = new Intent(MainActivity.this, NormalActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 0);
            }
        }
        // 최초 등록 버튼
        if (v == bt_registration) {
            if (!is_registered) {
                Intent intent = new Intent(MainActivity.this, RegistActivity.class);
                startActivityForResult(intent, 1);
            }
        }
        // 확인증 발급 버튼
        if (v == bt_certificate) {
            if (is_login) {
                Intent intent = new Intent(MainActivity.this, ConfirmActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 0);
            }
        }
    }

    // 메뉴 내부에 있는 로그아웃 버튼
    public void onLogout(View v) {
        if (v == findViewById(R.id.bt_logout)) {
            is_login = false;

            SharedPreferences.Editor editor = infoData.edit();
            editor.putBoolean("IS_AUTOLOGIN", false);
            editor.apply();

            final DrawerLayout drawerLayout = findViewById(R.id.layout_main);
            final View slideView = findViewById(R.id.layout_slide);
            drawerLayout.closeDrawer(slideView);

            Toast.makeText(getApplicationContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

//            resumeActivity();
            onRestart();
        }
    }

    // 저장된 값 가져오기
    private void loadInfo() {
        s_id = infoData.getString("ID", "");
        s_number = infoData.getString("NUMBER", "");
        s_name = infoData.getString("NAME", "");
    }

    /***** 서버 통신 *****/
    // 관람 시작 여부 받아오는 부분
    public static class GetIsStartTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> activityReference;
        ProgressDialog progressDialog;

        GetIsStartTask(MainActivity context) {
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
/*  차후 구현 예정  // 관람 종료 여부 받아오는 부분
    public static class PutFinishTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> activityReference;
        ProgressDialog progressDialog;

        PutFinishTask(MainActivity context) {
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
*/
    // 전시관 오픈 여부 받아오는 부분
    public static class GetExhibitionTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> activityReference;
        ProgressDialog progressDialog;

        GetExhibitionTask(MainActivity context) {
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
        private WeakReference<MainActivity> activityReference;
        ProgressDialog progressDialog;

        GetExhibitionQrTask(MainActivity context) {
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

