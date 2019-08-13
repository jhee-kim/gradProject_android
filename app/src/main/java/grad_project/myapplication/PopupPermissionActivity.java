package grad_project.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class PopupPermissionActivity extends Activity {
    Button bt_apply, bt_cancel;

    MainActivity mainActivity = (MainActivity)MainActivity.A_MainActivity;

    /***** 권한 *****/
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
    };
    static final int MULTIPLE_PERMISSION = 1;
    ArrayList<String> permissionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_permission);

        Intent intent = getIntent();

        permissionList = intent.getStringArrayListExtra("permissionList");

        bt_apply = findViewById(R.id.bt_apply);
        bt_cancel = findViewById(R.id.bt_cancel);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !(event.getAction() == MotionEvent.ACTION_OUTSIDE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mainActivity.finish();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_apply :
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSION);
                finish();
                break;
            case R.id.bt_cancel :
                finish();
                mainActivity.finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                Intent intent = new Intent(PopupPermissionActivity.this, MainActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
