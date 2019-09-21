package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.lang.ref.WeakReference;

public class CompareActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//    private static final String TAG = CompareActivity.class.getSimpleName();
    private SharedPreferences infoData;
    private int imgAddr;
    private int imgNum;
    private int exhibitionNum;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private long mLastClickTime = 0;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native int imageprocessing(long objectImage, long sceneImage);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new NullPointerException("Null ActionBar");
        } else {
            actionBar.hide();
        }

        mOpenCvCameraView = findViewById(R.id.surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        infoData = getSharedPreferences("infoData", MODE_PRIVATE);
        Intent intent = getIntent();
        //해당 이미지의 주소(R.drawable...), 사진번호, 해당 이미지의 전시관 가져옴
        imgAddr = intent.getIntExtra("ImgAddr", -1);
        exhibitionNum = intent.getIntExtra("exhibitionNum", -1);     //전시관 번호(0~5)
        imgNum = intent.getIntExtra("imgNum", -1);                   //몇번째 이미지인지(0~max-1)

        ImageButton shutter = findViewById(R.id.button_capture1);
        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 10000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                /*90도 회전*/
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap = Bitmap.createBitmap(matResult.width(), matResult.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matResult, bitmap);
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                AsyncTask<Bitmap, Void, Integer> task = new FeatureComparingTask();
                task.execute(rotated);
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        matInput = inputFrame.rgba();
        //if ( matResult != null ) matResult.release(); fix 2018. 8. 18
        if ( matResult == null )
            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

        return matResult;
    }

    private class FeatureComparingTask extends AsyncTask<Bitmap, Void, Integer> {
        private final static int MIN_CORRECT_NUM = 100;
        private WeakReference<CompareActivity> mActivityWeakReference;
        private ProgressDialog asyncDialog = new ProgressDialog(CompareActivity.this);

        @Override
        protected void onPreExecute() {
            mActivityWeakReference = new WeakReference<>(CompareActivity.this);
            asyncDialog.setCancelable(false);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("처리중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Bitmap... bitmaps) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), imgAddr);
            Mat correctImg =  new Mat(0,0, CvType. CV_32FC2);
            Utils.bitmapToMat(bmp, correctImg);
            Mat compareImg =  new Mat(0,0, CvType. CV_32FC2);
            Utils.bitmapToMat(bitmaps[0], compareImg);
//////////////////////////////////////////////////////
            return imageprocessing(correctImg.getNativeObjAddr(), compareImg.getNativeObjAddr());
        }

        @Override
        protected void onPostExecute(Integer correctNum) {
            String resultMessage;
            boolean isSuccess = false;
            if(correctNum < MIN_CORRECT_NUM) {
                resultMessage = "사진이 일치하지 않습니다.";
            } else {    //사진이 일치하는 경우
                SharedPreferences.Editor editor = infoData.edit();  //Share preference 설정
                editor.putBoolean("IS_CHECK_PIC_" + (exhibitionNum  + 1) + "-" + (imgNum + 1), true);
                editor.apply();
                resultMessage = "사진이 일치합니다.";   //띄울 메시지
                isSuccess = true;
            }
            asyncDialog.dismiss();
            Toast.makeText(getApplicationContext(), resultMessage, Toast.LENGTH_LONG).show();//resultMessage 띄워줌
            CompareActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                Intent intent = new Intent(CompareActivity.this, ShowImageActivity.class);
                if(isSuccess) {
                    intent.putExtra("isSuccess", true);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}