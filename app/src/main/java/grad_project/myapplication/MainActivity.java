package grad_project.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

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
*   - participation : 참여 구분(0:일반 관람 / 1:해설 관람)
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
    boolean is_menuOpen = false;
    String s_id, s_name;
    DrawerLayout drawerLayout;
    View slideView;
    Button bt_openMap, bt_registration, bt_certificate;
    RelativeLayout bt_openMenu, bt_closeMenu, bt_information, bt_notificationOn;
    LinearLayout bt_viewtime, bt_myinfo, bt_homepage, bt_route;
    Long startDate;
    private boolean is_start = false;
    private boolean is_end = false;

    boolean is_service = false;
    boolean is_notification = false;

    public static Activity A_MainActivity;

    /***** 권한 *****/
    private String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.READ_PHONE_STATE
    };
    ArrayList<String> permissionList;

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

        A_MainActivity = MainActivity.this;
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);

        is_service = isNotiServiceRunning();
        Log.d("ISSERVICE", Boolean.toString(is_service));

        // 메인 화면 버튼
        bt_openMap = findViewById(R.id.bt_open_map);
        bt_registration = findViewById(R.id.bt_registration);
        bt_certificate = findViewById(R.id.bt_certificate);

        // 메뉴 관련 버튼
        drawerLayout = findViewById(R.id.layout_main);
        slideView = findViewById(R.id.layout_slide);
        bt_openMenu = findViewById(R.id.bt_open_menu);
        bt_closeMenu = findViewById(R.id.bt_close_menu);

        bt_notificationOn = findViewById(R.id.bt_notification_on);
        bt_notificationOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("알림 켜기/끄기");
                if (is_login) {
                    String nowState = infoData.getBoolean("NOTIFICATION", false)? "ON":"OFF";
                    builder.setMessage("관람 정보를 알림으로 항상 띄웁니다.\n현재 상태 : " + nowState);
                    if (nowState.equals("OFF")) {
                        builder.setPositiveButton("켜기",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences.Editor editer = infoData.edit();
                                        editer.putBoolean("NOTIFICATION", true);
                                        editer.apply();
                                        refreshService();

                                        Toast.makeText(getApplicationContext(), "알림 ON", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                    } else {
                        builder.setPositiveButton("끄기",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences.Editor editer = infoData.edit();
                                        editer.putBoolean("NOTIFICATION", false);
                                        editer.apply();
                                        refreshService();
                                        Toast.makeText(getApplicationContext(), "알림 OFF", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                } else {
                    builder.setMessage("관람 정보를 알림으로 항상 띄웁니다.\n현재 상태 : 로그아웃됨");
                    builder.setPositiveButton("로그인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivityForResult(intent, 0);
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton("닫기",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                }
//                builder.setCancelable(false);
                builder.show();
            }
        });

        bt_openMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(slideView);
                is_menuOpen = true;
            }
        });

        bt_closeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(slideView);
                is_menuOpen = false;
            }
        });

        // 메뉴 버튼 id 불러오기
        bt_viewtime = findViewById(R.id.bt_viewtime);
        bt_myinfo = findViewById(R.id.bt_myinfo);
        bt_homepage = findViewById(R.id.bt_homepage);
        bt_route = findViewById(R.id.bt_route);
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.i815.or.kr/2018/tour/armyVisit.do"));
                startActivity(intent);
            }
        });
        // 오시는길 버튼
        bt_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://i815.or.kr/2018/tour/location.do"));
                startActivity(intent);
            }
        });


        resumeActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDestroyedReceiver, new IntentFilter("Service Destroyed"));
        LocalBroadcastManager.getInstance(this).registerReceiver(processFinishedReceiver, new IntentFilter("Finished!"));
        LocalBroadcastManager.getInstance(this).registerReceiver(processInitializedReceiver, new IntentFilter("Initialized!"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions();
                }
            }
        }, 500);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resumeActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceDestroyedReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (is_menuOpen) {
            drawerLayout.closeDrawer(slideView);
            is_menuOpen = false;
        } else {
            super.onBackPressed();
        }
    }

    private void checkPermissions() {
        int result;
        permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            Intent in_permission = new Intent(MainActivity.this, PopupPermissionActivity.class);
            in_permission.putStringArrayListExtra("permissionList", permissionList);
            startActivityForResult(in_permission, -1);
        }
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

    private boolean getGpsInfo() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean getStartState() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(dbConnect.GET_ISSTART, s_id).get();
            Log.d("GET_ISSTART", result);
            if (!result.equals("-1")) {
                is_start = !result.equals("0");
                if (is_start) {
                    SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    startDate = df.parse(result).getTime();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEnd() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(dbConnect.GET_ISEND, s_id).get();
            Log.d("GET_ISEND", result);
            if (!result.equals("-1")) {
                if (result.equals("0")) {
                    is_end = false;
                    return true;
                } else {
                    is_end = true;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String getParticipation() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(dbConnect.GET_PARTICIPATION, s_id).get();
            Log.d("GET_PARTICIPATION", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }
    // 액티비티 내용 새로고침 하는 메소드
    public void resumeActivity() {

        if (!getNetworkInfo()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("앱 사용을 위해서 네트워크 연결이 필요합니다.");
            builder.setPositiveButton("설정",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            builder.setNegativeButton("종료",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            builder.setCancelable(false);
            builder.show();
        }

        if (!getGpsInfo()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("앱 사용을 위해서 GPS 실행이 필요합니다.");
            builder.setPositiveButton("설정",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            builder.setNegativeButton("종료",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            builder.setCancelable(false);
            builder.show();
        }

        loadInfo();

        /* 액티비티 화면 내용 설정 */
        // 최초 등록 버튼 활성화 및 비활성화
        if (is_login) {
            if (isEnd()) {
                if (!is_end) {
                    if (!is_service) {
                        startNotiService();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            }

            Button bt_registration = findViewById(R.id.bt_registration);
            bt_registration.setEnabled(false);
            bt_registration.setTextColor(getResources().getColor(R.color.disableButton));

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

        /*관람하기 버튼*/
        Button bt_registration = findViewById(R.id.bt_open_map);
        bt_registration.setEnabled(true);
        bt_registration.setTextColor(getResources().getColor(R.color.unfocusText));
        if(isEnd()) {
            if(is_end) {
                bt_registration.setEnabled(false);
                bt_registration.setTextColor(getResources().getColor(R.color.disableButton));
            }
        }

        /* 메뉴 버튼 온클릭리스너 설정 */

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
                startNotiService();
                Log.d("LOGIN COMPLETE", s_id);
            }
        }
        // 최초 등록 완료했을 경우(자동 로그인 됨)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//                is_login = true;
            }
        }
        // 관람 완료 후 메인 액티비티로 자동 진입할 때
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                drawerLayout.closeDrawer(slideView);
                is_menuOpen = false;
            }
        }
    }

    private BroadcastReceiver serviceDestroyedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 서비스 꺼졌을 때
            Log.d("SERVICE", "SerserviceDestroyedReceiver!@#");
            resumeActivity();
        }
    };

    private BroadcastReceiver processFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 관람 종료됐을때
            Log.d("SERVICE", "processFinishedReceiver!@#");
        }
    };

    private BroadcastReceiver processInitializedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 관람 종료됐을때
            Log.d("SERVICE", "processInitializedReceiver!@#");
            ActivityCompat.finishAffinity(MainActivity.this);
        }
    };

    // 메인 화면 버튼
    public void onClick(View v) {
        // 관람하기 버튼
        if (v == bt_openMap) {
            if (is_login) {
                if (getStartState()) {
                    if (is_start) {
                        Intent intent = new Intent(MainActivity.this, NormalActivity.class);
                        intent.putExtra("Time", startDate);
                        startActivity(intent);
                    } else {
                        switch (getParticipation()) {
                            case "0": {
                                Intent intent = new Intent(MainActivity.this, HelpNorActivity.class);
                                startActivity(intent);
                                break;
                            }
                            case "1": {
                                Intent intent = new Intent(MainActivity.this, HelpComActivity.class);
                                startActivity(intent);
                                break;
                            }
                            default: {
                                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                } else {    // 네트워크 통신 오류 예외처리
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
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
                if (isEnd()) {
                    if (is_end) {
                        Intent intent = new Intent(MainActivity.this, ConfirmActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "아직 관람이 완료되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 0);
            }
        }
    }

    // 메뉴 내부에 있는 로그아웃 버튼
    public void onLogout(View v) {
        if (v == findViewById(R.id.bt_logout)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("로그아웃하면 관람 진행 정보가 모두 삭제됩니다.\n정말 로그아웃 하시겠습니까?");
            builder.setPositiveButton("네",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            is_login = false;
                            is_start = false;
//                            is_autoLogin = false;

                            stopNotiService();
                            SharedPreferences.Editor editor = infoData.edit();
                            editor.clear();
                            editor.putBoolean("IS_LOGIN", false);
                            editor.putBoolean("NOTIFICATION", false);
                            editor.apply();

                            final DrawerLayout drawerLayout = findViewById(R.id.layout_main);
                            final View slideView = findViewById(R.id.layout_slide);
                            drawerLayout.closeDrawer(slideView);

                            Toast.makeText(getApplicationContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                            onRestart();
                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setCancelable(false);
            builder.show();
        }
    }

    // 저장된 값 가져오기
    private void loadInfo() {
//        is_autoLogin = infoData.getBoolean("IS_AUTOLOGIN", false);
        is_login = infoData.getBoolean("IS_LOGIN", false);
        s_id = infoData.getString("ID", "");
        s_name = infoData.getString("NAME", "");
        is_notification = infoData.getBoolean("NOTIFICATION", false);
    }

    public void startNotiService() {
        if (!is_service) {
            Intent intent = new Intent(MainActivity.this, NotiService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            is_service = true;
        }
    }

    public void stopNotiService() {
        if (is_service) {
            Intent intent = new Intent(MainActivity.this, NotiService.class);

            stopService(intent);
            is_service = false;
        }
    }

    public void refreshService() {
        stopNotiService();
        startNotiService();
    }

    public boolean isNotiServiceRunning() {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotiService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

