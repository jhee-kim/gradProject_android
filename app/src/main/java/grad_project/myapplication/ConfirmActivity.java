package grad_project.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;

public class ConfirmActivity extends AppCompatActivity {
    String s_id, s_name, s_number, s_phone, s_temper, s_destination, s_participation, s_division, s_startDate;
    TextView tv_participation, tv_name, tv_start, tv_date1, tv_date2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        RelativeLayout bt_back_layout = findViewById(R.id.bt_back_layout);
        bt_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SharedPreferences infoData;
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        s_id = infoData.getString("ID", "");
        if (getStartState() && getEndDate()) {
            if(getUserData()) {
                tv_participation = findViewById(R.id.tv_participation);
                tv_name = findViewById(R.id.tv_name);
                tv_start = findViewById(R.id.tv_start);
                tv_date1 = findViewById(R.id.tv_date1);
                tv_date2 = findViewById(R.id.tv_date2);
                if (s_number.length() > 3) {
                    s_number = s_number.substring(0, 2) + "-" + s_number.substring(2);
                }
                String temp_name = s_name + "\n(" + s_number + ")";
                tv_name.setText(temp_name);
                switch (s_participation) {
                    case "0":
                        tv_participation.setText("일반 관람");
                        break;
                    case "1":
                        tv_participation.setText("해설 관람");
                        break;
                    default:
                        tv_participation.setText("E");
                }
                tv_start.setText(s_startDate);
                tv_date1.setText(DateFormat.format("yyyy년 MM월 dd일", Calendar.getInstance(Locale.KOREA)));
                tv_date2.setText(DateFormat.format("yyyy. MM. dd", Calendar.getInstance(Locale.KOREA)));
            } else {    // 네트워크 통신 오류 예외처리
                Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {    // 네트워크 통신 오류 예외처리
            Toast.makeText(getApplicationContext(), "네트워크 통신 오류", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void onDownload(View v) {
        findViewById(R.id.content_confirm).setDrawingCacheEnabled(true);
        findViewById(R.id.content_confirm).buildDrawingCache();
        Bitmap saveBitmap =  findViewById(R.id.content_confirm).getDrawingCache();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss");

        String namePostfix = format.format(new Date());
        String basePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).toString() + File.separator + "Screenshots";//sdPath + File.separator;
        File dir = new File(basePath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File saveFile = new File(basePath + File.separator + "확인증_" + namePostfix + ".jpg");
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(saveFile);
            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);
            Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_SHORT).show();
        } finally {
            if(output!=null) { try{output.close();}catch(Exception e){e.printStackTrace();}}
        }
    }

    public void onBack(View v) {
        if (v == findViewById(R.id.bt_back)) {
            finish();
        }
    }

    public boolean getUserData() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(DdConnect.GET_AUDIENCE, s_id).get();
            Log.d("GET_AUDIENCE", result);
            if (!result.equals("-1")) {
                JSONObject jResult = new JSONObject(result);
                JSONArray jArray = jResult.getJSONArray("result");
                JSONObject jObject = jArray.getJSONObject(0);
                s_number = jObject.getString("number");
                s_name = jObject.getString("name");
                s_phone = jObject.getString("phone");
                s_participation = jObject.getString("participation");
                s_division = jObject.getString("division");
                s_temper = jObject.getString("temper");
                s_destination = jObject.getString("destination");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean getStartState() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(DdConnect.GET_ISSTART, s_id).get();
            Log.d("GET_ISSTART", result);
            if (!result.equals("-1") && !result.equals("0")) {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(result);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\n(HH:mm)", Locale.KOREA);
                s_startDate = sdf.format(date);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean getEndDate() {
        DdConnect dbConnect = new DdConnect(this);
        try {
            String result = dbConnect.execute(DdConnect.GET_ISEND, s_id).get();
            Log.d("GET_ISEND", result);
            if (!result.equals("-1") && !result.equals("0")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}