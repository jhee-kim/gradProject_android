package grad_project.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowImageActivity extends AppCompatActivity {
    private static final String TAG = CompareActivity.class.getSimpleName();
    /*intent로 받아오는 값들*/
    private int imgAddr;
    private int imgNum;
    private int exhibitionNum;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        ImageView imgView;
        TextView textView;
        String imgTitle;

        Intent intent = getIntent();
        imgAddr = intent.getIntExtra("ImgAddr", -1);
        exhibitionNum = intent.getIntExtra("exhibitionNum", -1);     //전시관 번호(0~5)
        imgNum = intent.getIntExtra("imgNum", -1);                   //몇번째 이미지인지(0~max-1)
        imgTitle = intent.getStringExtra("imgTitle");

        imgView = findViewById(R.id.correct_img);
        imgView.setImageResource(imgAddr);
        textView = findViewById(R.id.title_text);
        textView.setText(imgTitle);
    }
    public void onClick(View v) {
        Intent intent = new Intent(ShowImageActivity.this, CompareActivity.class);
        intent.putExtra("ImgAddr", imgAddr);
        intent.putExtra("exhibitionNum", exhibitionNum);      //전시관 번호(0~5)
        intent.putExtra("imgNum", imgNum);                  //몇번째 이미지인지(0~max-1)
        startActivityForResult(intent, 1000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch(requestCode) {
                case 1000:  //NormalActivity로 보냄
                    Intent sendIntent = new Intent(ShowImageActivity.this, NormalActivity.class);
                    sendIntent.putExtra("isSuccess", data.getBooleanExtra("isSuccess", false));
                    setResult(RESULT_OK, sendIntent);
                    finish();
                    break;
            }
        }
    }
}
