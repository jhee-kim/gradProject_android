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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    String s_name, s_number, s_phone, s_temper, s_destination;
    int i_participation, i_division;
    DrawerLayout drawerLayout;
    View slideView;
    Button bt_openMap, bt_registration, bt_certificate;
    RelativeLayout bt_openMenu, bt_closeMenu;
    LinearLayout bt_viewtime, bt_myinfo, bt_certificate_2, bt_registration_2, bt_homepage, bt_information;

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String ADD_AUDIENCE = BASE_PATH + "add_audience.php";            //관람등록(성공 1, 실패 0 반환)
    public static final String GET_AUDIENCE = BASE_PATH + "get_audience.php";             //로그인(성공 1, 실패 0 반환)
    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 1, 실패 0 반환)
    public static final String GET_NARRATOR = BASE_PATH + "get_narrator.php";            //관람등록시 전시해설 on/oof 여부(해설자 스케줄 확인)  - 미구현
    public static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";          //각 전시관 별 개설 여부(JSON 형식) - ex) { "number": "1", "isOpen": "1" }
    public static final String GET_QR = BASE_PATH + "get_qr.php";                              //각 전시관 별 qr코드 파일 위치(JSON 형식) - ex) { "number": "1", "address": "http://35.221.108.183/QR/1.png" }
    public static final String GET_RSSID = BASE_PATH + "get_mac.php";                        //각 전시관 별 RSSI(JSON 형식) - ex) { "number": "1", "mac": "00:70:69:47:2F:30" }
    public static final String UPDATE_AUDIENCE = BASE_PATH + "update_audience.php";    //전시 종료(성공 1, 실패 0 반환)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
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
        is_autoLogin = infoData.getBoolean("AUTOLOGIN", false);

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

        /***** 메뉴 버튼 온클릭리스너 설정 *****/
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

        resumeActivity();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("RESTART", "onRestart()");
        resumeActivity();
    }

    // 액티비티 내용 새로고침 하는 메소드
    public void resumeActivity() {
        final ImageView iv_registration = findViewById(R.id.iv_registration);
        final TextView tv_registreation = findViewById(R.id.tv_registration);

        is_registered = infoData.getBoolean("IS_REGISTERED", false);
        loadInfo();

        /***** 액티비티 화면 내용 설정 *****/
        // 최초 등록 버튼 활성화 및 비활성화
        if (is_registered) {
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(false);
            iv_registration.setEnabled(false);
            iv_registration.setColorFilter(Color.parseColor("#ffE0E0E0"), PorterDuff.Mode.SRC_IN);
            is_autoLogin = infoData.getBoolean("AUTOLOGIN", false);
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

        /***** 메뉴 버튼 온클릭리스너 설정 *****/
        // 최초 등록 버튼
        bt_registration_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!is_registered) {
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
                    Intent intent = new Intent(MainActivity.this, NormalActivity.class);
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
        // 최초 등록 완료했을 경우
        else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String h_name, h_number, h_participation, h_division, h_temper, h_phone, h_destination;
                h_name = data.getExtras().getString("H_NAME");
                h_number = data.getExtras().getString("H_NUMBER");
                h_participation = data.getExtras().getString("H_PARTICIPATION");
                h_division = data.getExtras().getString("H_DIVISION");
                h_temper = data.getExtras().getString("H_TEMPER");
                h_phone = data.getExtras().getString("H_PHONE");
                h_destination = data.getExtras().getString("H_DESTINATION");

                Log.i("NAME", h_name);
                Log.i("TEMPER", h_temper);
                Log.i("NUMBER", h_number);
                Log.i("PHONE", h_phone);

                InsertData task = new InsertData();
                task.execute(ADD_AUDIENCE, h_number, h_name, h_participation, h_division, h_temper, h_phone, h_destination);
            }
        }
        // 로그인 시도
        else if (requestCode == 2) {
            String h_name ,h_number;
            h_name = data.getExtras().getString("H_NAME");
            h_number = data.getExtras().getString("H_NUMBER");

            InsertData task = new InsertData();
        }
    }

    // 메인 화면 버튼
    public void onClick(View v) {
        Switch sw_permit = findViewById(R.id.sw_test);
        boolean is_permitted = sw_permit.isChecked();

        // 관람하기 버튼
        if (v == bt_openMap) {
            if (is_login) {
                if (is_permitted) {
                    Intent intent = new Intent(MainActivity.this, NormalActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(MainActivity.this, NormalActivity.class);
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
            editor.putBoolean("AUTOLOGIN", false);
            editor.apply();

            final DrawerLayout drawerLayout = findViewById(R.id.layout_main);
            final View slideView = findViewById(R.id.layout_slide);
            drawerLayout.closeDrawer(slideView);

            Toast.makeText(getApplicationContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

            resumeActivity();
            onRestart();
        }
    }

    // 저장된 값 가져오기
    private void loadInfo() {
        i_participation = infoData.getInt("PARTICIPATION", -1);
        i_division = infoData.getInt("DIVISION", -1);
        s_name = infoData.getString("NAME", "");
        s_number = infoData.getString("NUMBER", "");
        s_phone = infoData.getString("PHONE", "");
        s_temper = infoData.getString("TEMPER", "");
    }

    /***** 서버 통신 *****/
    public class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
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
            String serverURL = (String)params[0];
            String number = (String)params[1];
            String name = (String)params[2];
            String participation = (String)params[3];
            String division = (String)params[4];
            String temper = (String)params[5];
            String phone = (String)params[6];
            String destination = (String)params[7];
            String postParameters = "&number=" + number + "&name=" + name + "&participation=" + participation + "&division=" + division + "&temper=" + temper + "&phone=" + phone + "&destination=" + destination;
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
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                return new String("Error: " + e.getMessage());
            }
        }
    }
}

