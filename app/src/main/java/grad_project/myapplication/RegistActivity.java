package grad_project.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistActivity extends AppCompatActivity {
    private SharedPreferences infoData;
    private String s_id;
    private String s_name = "";
    private int i_division = -1;
    private int i_participation = -1;
    private int i_expTime = -1;
    private String s_temper = "";
    private String s_number = "";
    private String s_number_0 = "";
    private String s_number_1 = "";
    private String s_phone = "";
    private String s_phone_0 = "";
    private String s_phone_1 = "";
    private String s_phone_2 = "";
    private String s_destination = "";
    EditText et_name, et_temper, et_number_1, et_phone_1, et_phone_2, et_destination;
    Spinner sp_number_0, sp_phone_0, sp_division, sp_participation, sp_expTime;
    CheckBox cb_check;
    boolean check_temp;
    RadioGroup rg_participation, rg_division;
    LinearLayout ll_agreement, bt_partHelp;

    ArrayAdapter<String> timeAdapter;
    List<String> timeList;
    boolean[] b_expTime = {true, false, false};
    boolean is_dialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        et_name = findViewById(R.id.et_name);
        et_temper = findViewById(R.id.et_temper);
        sp_number_0 = findViewById(R.id.sp_number_0);
        et_number_1 = findViewById(R.id.et_number_1);
        sp_phone_0 = findViewById(R.id.sp_phone_0);
        et_phone_1 = findViewById(R.id.et_phone_1);
        et_phone_2 = findViewById(R.id.et_phone_2);
        et_destination = findViewById(R.id.et_destination);
        cb_check = findViewById(R.id.cb_check);
        sp_division = findViewById(R.id.sp_division);
        ll_agreement = findViewById(R.id.ll_agreement);
        bt_partHelp = findViewById(R.id.bt_partHelp);
        sp_participation = findViewById(R.id.sp_participation);
        sp_expTime = findViewById(R.id.sp_expTime);

        check_temp = false;
        cb_check.setChecked(false);

        sp_number_0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_number_0 = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        // 군번 8자리 입력하면 자동으로 폰번호 입력으로 커서 넘어가는 코드인데
//        // 굳이 필요할까 싶음
//        et_number_1.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (et_number_1.length() >= 8) {
//                    et_phone_1.requestFocus();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });

        sp_phone_0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_phone_0 = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        et_phone_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_phone_1.length() >= 4) {
                    et_phone_2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sp_division.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                i_division = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sp_participation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    i_participation = 0;
                    i_expTime = -100;
                    sp_expTime.setEnabled(false);
                    sp_expTime.setVisibility(View.INVISIBLE);
                }
                else if (position == 1) {
                    i_participation = 1;
                    sp_expTime.setEnabled(true);
                    sp_expTime.setVisibility(View.VISIBLE);
                    sp_expTime.setSelection(0);
                    if (sp_expTime.getItemAtPosition(0).toString().equals("없음")) {
                        Toast.makeText(getApplicationContext(), "오늘 이용 가능한 시간이 없습니다.", Toast.LENGTH_SHORT).show();
                        i_expTime = -101;
                    }
                } else {
                    i_participation = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(dbConnect.GET_NARRATOR).get();
            Log.d("GET_NARRATOR", result);
            if (result.equals("-1")) {
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            } else {
                JSONObject jResult = new JSONObject(result);
                JSONArray jArray = jResult.getJSONArray("result");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    if(jObject.getString("first_time").equals("1")) {
                        b_expTime[1] = true;
                    }
                    if(jObject.getString("second_time").equals("1")) {
                        b_expTime[2] = true;
                    }
                    if(b_expTime[1] || b_expTime[2]) {
                        b_expTime[0] = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
        }

        timeList = new ArrayList<String>();
        if (b_expTime[0]) {
            timeList.add("없음");
        }
        else {
            timeList.add("시간");
            if (b_expTime[1]) {
                timeList.add("11:00");
            }
            if (b_expTime[2]) {
                timeList.add("13:00");
            }
        }
        timeAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_expTime.setAdapter(timeAdapter);

        sp_expTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    i_expTime = -100;
                } else {
                    String temp = sp_expTime.getItemAtPosition(position).toString();
                    if (temp.equals("없음")) {
                        Toast.makeText(getApplicationContext(), "오늘 이용 가능한 시간이 없습니다.", Toast.LENGTH_SHORT).show();
                        i_expTime = -100;
                    } else {
                        if (!is_dialog) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegistActivity.this);
                            builder.setTitle("주의");
                            builder.setMessage("해설 관람은 선착순입니다.\n\n앱에서 시간을 선택하셨더라도 반드시 안내센터에서 등록하셔야 하며,\n관람 인원이 초과되면 해설 관람 이용이 불가할 수 있습니다.");
                            builder.setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.setCancelable(false);
                            builder.show();
                            is_dialog = true;
                        }
                        if (temp.equals("11:00")) {
                            i_expTime = 0;
                        } else if (temp.equals("13:00")) {
                            i_expTime = 1;
                        } else {
                            i_expTime = -100;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bt_partHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "//관람 방법에 대한 설명 띄우기//", Toast.LENGTH_SHORT).show();
            }
        });

        ll_agreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistActivity.this, PopupAgreementActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        cb_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check_temp) {
                    cb_check.setChecked(false);
                    Intent intent = new Intent(RegistActivity.this, PopupAgreementActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(getApplicationContext(), "개인정보 수집 및 이용에 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                    check_temp = false;
                }
            }
        });

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent in_ocr = new Intent(RegistActivity.this, PopupOcrActivity.class);
        startActivityForResult(in_ocr, 2);
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public void onClick(View v) {
//        int participation_selected = rg_participation.getCheckedRadioButtonId();

        s_name = et_name.getText().toString().trim();
        s_temper = et_temper.getText().toString().trim();
        s_number_1 = et_number_1.getText().toString().trim();
        s_number = s_number_0 + s_number_1;
        s_phone_1 = et_phone_1.getText().toString().trim();
        s_phone_2 = et_phone_2.getText().toString().trim();
        s_phone = s_phone_0 + s_phone_1 + s_phone_2;
        s_destination = et_destination.getText().toString().trim();
        boolean b_check = cb_check.isChecked();
        String temp_phone = s_phone_0 + "-" + s_phone_1 + "-" + s_phone_2;
        String temp_number = s_number_0 + "-" + s_number_1;

//        switch (participation_selected) {
//            case R.id.rb_normal:
//                i_participation = 0;
//                break;
//            case R.id.rb_narr:
//                i_participation = 1;
//                break;
//            default:
//                i_participation = -1;
//                break;
//        }

        Log.i("NAME", s_name);
        Log.i("DIVISION", Integer.toString(i_division));
        Log.i("TEMPER", s_temper);
        Log.i("NUMBER", s_number);
        Log.i("PHONE", s_phone);

        if (s_name.equals("")) {
            Toast.makeText(RegistActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_name.requestFocus();
        } else if (s_number.equals("")) {
            Toast.makeText(RegistActivity.this, "군번을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_number_1.requestFocus();
        } else if (s_phone.equals("")) {
            Toast.makeText(RegistActivity.this, "휴대폰 번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_phone_1.requestFocus();
        } else if (i_participation == -1) {
            Toast.makeText(RegistActivity.this, "관람 방법을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if (i_division == -1) {
            Toast.makeText(RegistActivity.this, "구분을 선택해주세요.", Toast.LENGTH_SHORT).show();
        } else if (s_temper.equals("")) {
            Toast.makeText(RegistActivity.this, "소속 부대를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
        } else if (s_destination.equals("")) {
            Toast.makeText(RegistActivity.this, "행선지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_temper.requestFocus();
        } else if (!b_check) {
            Toast.makeText(RegistActivity.this, "개인정보 수집 및 이용에 동의해주세요.", Toast.LENGTH_SHORT).show();
        } else if (i_expTime == -101) {
            Toast.makeText(RegistActivity.this, "오늘 이용 가능한 해설 관람이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RegistActivity.this, PopupRegistActivity.class);
            intent.putExtra("NAME", s_name);
            intent.putExtra("NUMBER", temp_number);
            intent.putExtra("PHONE", temp_phone);
            intent.putExtra("DIVISION", i_division);
            intent.putExtra("EXPTIME", i_expTime);
            intent.putExtra("TEMPER", s_temper);
            intent.putExtra("DESTINATION", s_destination);
            intent.putExtra("PARTICIPATION", i_participation);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 입력된 정보 최종 확인이 되었을 경우
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = infoData.edit();
                DdConnect dbConnect = new DdConnect(this);
                try {
                    String result = dbConnect.execute(dbConnect.ADD_AUDIENCE, "", s_number, s_name, Integer.toString(i_participation), Integer.toString(i_division), s_temper, s_phone, s_destination).get();
                    Log.d("ADD_AUDIENCE", result);
                    if (result.equals("0")) {
                        Toast.makeText(RegistActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    } else if (result.equals("-1")) {
                        Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                    } else {
                        s_id = result;
                        Toast.makeText(RegistActivity.this, "등록 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                        editor.putBoolean("IS_AUTOLOGIN", true);
                        editor.putString("ID", s_id);
                        editor.putString("NAME", s_name);
                        editor.apply();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 개인정보 수집 활용 동의/미동의시
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                cb_check.setChecked(true);
                check_temp = true;
            } else if (resultCode == RESULT_CANCELED) {
                cb_check.setChecked(false);
                check_temp = false;
            }
        }

        // OCR 이벤트 처리
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data.getStringExtra("result") != null) {
                    Hangul hangul = new Hangul();
                    String[] st1 = data.getStringExtra("result").split("\n");
                    ArrayList<String> results = new ArrayList<String>();
                    for (String str2 : st1) {
                        String[] st3 = str2.split("\n");
                        for (String str4 : st3) {
                            String[] st5 = str4.split(" : ");
                            for (String str6 : st5) {
                                String[] st7 = str6.split(" ");
                                for (String str8 : st7) {
                                    results.add(str8);
                                }
                            }
                        }
                    }

                    for (int i = 0; i < results.size(); i++) {
                        if(hangul.jasoEqual(hangul.hangulToJaso(results.get(i)), hangul.hangulToJaso("성명"))) {
                            et_name.setText(results.get(i+1));
                            break;
                        }
                    }
                    for (int i = 0; i < results.size(); i++) {
                        if(hangul.jasoEqual(hangul.hangulToJaso(results.get(i)), hangul.hangulToJaso("군번"))) {
                            String temp_num[] = results.get(i+1).split("-");
                            boolean is_success_sp = false;
                            for (int j = 0; j < sp_number_0.getCount(); j++) {
                                if (sp_number_0.getItemAtPosition(j).toString().equals(temp_num[0])) {
                                    sp_number_0.setSelection(j);
                                    is_success_sp = true;
                                }
                            }
                            if (!is_success_sp) {
                                Toast.makeText(getApplicationContext(), "텍스트 읽어오기 오류!", Toast.LENGTH_SHORT).show();
                            }
                            et_number_1.setText(temp_num[1]);
                            break;
                        }
                    }
                    for (int i = 0; i < results.size(); i++) {
                        if(hangul.jasoEqual(hangul.hangulToJaso(results.get(i)), hangul.hangulToJaso("행선지"))) {
                            et_destination.setText(results.get(i+1));
                            break;
                        }
                    }
                    for (int i = 0; i < results.size(); i++) {
                        if(hangul.jasoEqual(hangul.hangulToJaso(results.get(i)), hangul.hangulToJaso("소속"))) {
                            et_temper.setText(results.get(i+1));
                            break;
                        }
                    }
                    /*핸드폰 번호 가져오기*/
                    TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String PhoneNum = telManager.getLine1Number();
                    if(PhoneNum.startsWith("+82")){
                        PhoneNum = PhoneNum.replace("+82", "0");
                        String[] temp_phone = new String[3];
                        if (PhoneNum.length() == 10) {
                            temp_phone[0] = PhoneNum.substring(0, 3);
                            temp_phone[1] = PhoneNum.substring(3, 6);
                            temp_phone[2] = PhoneNum.substring(6);
                        }
                        else {
                            temp_phone[0] = PhoneNum.substring(0, 3);
                            temp_phone[1] = PhoneNum.substring(3, 7);
                            temp_phone[2] = PhoneNum.substring(7);
                        }
                        boolean is_success_sp = false;
                        for (int j = 0; j < sp_phone_0.getCount(); j++) {
                            if (sp_phone_0.getItemAtPosition(j).toString().equals(temp_phone[0])) {
                                sp_phone_0.setSelection(j);
                                is_success_sp = true;
                            }
                        }
                        if (!is_success_sp) {
                            Toast.makeText(getApplicationContext(), "텍스트 읽어오기 오류!", Toast.LENGTH_SHORT).show();
                        }
                        et_phone_1.setText(temp_phone[1]);
                        et_phone_2.setText(temp_phone[2]);
                    }
                }
            }
        }
    }

    public class Hangul {
        final char[] ChoSung   = { 0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139,
                0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147,
                0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d,
                0x314e };
        final char[] JwungSung = { 0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154,
                0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a,
                0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160,
                0x3161, 0x3162, 0x3163 };
        final char[] JongSung  = { 0,      0x3131, 0x3132, 0x3133, 0x3134, 0x3135,
                0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c,
                0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142,
                0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a,
                0x314b, 0x314c, 0x314d, 0x314e };

        public String hangulToJaso(String s) {
            int a, b, c; // 자소 버퍼: 초성/중성/종성 순
            String result = "";
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch >= 0xAC00 && ch <= 0xD7A3) {
                    c = ch - 0xAC00;
                    a = c / (21 * 28);
                    c = c % (21 * 28);
                    b = c / 28;
                    c = c % 28;
                    result = result + ChoSung[a] + JwungSung[b];
                    if (c != 0) result = result + JongSung[c] ;
                } else {
                    result = result + ch;
                }
            }
            return result;
        }
        public boolean jasoEqual(String s1, String s2) {
            int count = 0;
            int length = s1.length() > s2.length() ? s2.length() : s1.length();
            for(int i = 0; i < length; i++) {
                if(s1.charAt(i) != s2.charAt(i)) {
                    count++;
                }
            }
            if(count < length/4) {
                return true;
            }
            return false;
        }
    }
}
