package grad_project.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
*   - participation : 참여 구분(0:전시 관람 / 1:전시 해설)
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
    String s_id, s_name;
    DrawerLayout drawerLayout;
    View slideView;
    Button bt_openMap, bt_registration, bt_certificate;
    RelativeLayout bt_openMenu, bt_closeMenu;
    LinearLayout bt_viewtime, bt_myinfo, bt_certificate_2, bt_registration_2, bt_homepage, bt_information;
    Long startDate;
    private boolean is_start = false;
//    private boolean is_finish = false;    // 차후 구현 예정

    /***** php 통신 *****/
    private static final String BASE_PATH = "http://35.221.108.183/android/";

    public static final String GET_ISSTART = BASE_PATH + "get_isStart.php";              //시작여부(성공 시작 시간, 실패 0 반환)
//    public static final String UPDATE_AUDIENCE = BASE_PATH + "update_audience.php";    //전시 종료 값 보내기(성공 1, 실패 0 반환)

    /***** 권한 *****/
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
    };
    static final int MULTIPLE_PERMISSION = 1;

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

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
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

        resumeActivity();
    }

    private void checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MULTIPLE_PERMISSION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(this.permissions[i])) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getApplicationContext(), "권한 요청에 동의해주셔야 이용이 가능합니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "권한 요청에 동의해주셔야 이용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("RESTART", "onRestart()");
        resumeActivity();
    }

    /* DB-서버 통신 파트 */
    // 관람 시작이 되었는지 여부 받아오는 메소드
    public void getStartState() {
        // 관람 시작 여부
        GetIsStartTask startTask = new GetIsStartTask(this);
        try {
            String result = startTask.execute(GET_ISSTART, s_id).get();
            is_start = !result.equals("0");
            if (is_start) {
                SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startDate = df.parse(result).getTime();
            }
            Log.d("ISSTART", String.valueOf(startDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 액티비티 내용 새로고침 하는 메소드
    public void resumeActivity() {
        final ImageView iv_registration = findViewById(R.id.iv_registration);
        loadInfo();

        if (is_autoLogin) {
            is_login = true;
        } else {
            is_login = false;
        }
        /* 액티비티 화면 내용 설정 */
        // 최초 등록 버튼 활성화 및 비활성화
        if (is_login) {
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(false);
            bt_registration.setTextColor(getResources().getColor(R.color.disableButton));
            iv_registration.setEnabled(false);
            iv_registration.setColorFilter(Color.parseColor("#ffE0E0E0"), PorterDuff.Mode.SRC_IN);

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
            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(true);
            bt_registration.setTextColor(getResources().getColor(R.color.unfocusText));
            iv_registration.setEnabled(true);
            iv_registration.setColorFilter(null);

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

        Log.d("STATE", Boolean.toString(is_start));

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
                Log.d("LOGIN COMPLETE", s_id);

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
                    intent.putExtra("Time", startDate);

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
            is_start = false;
            is_autoLogin = false;

            SharedPreferences.Editor editor = infoData.edit();
            editor.clear();
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
        is_autoLogin = infoData.getBoolean("IS_AUTOLOGIN", false);
        s_id = infoData.getString("ID", "");
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
}

