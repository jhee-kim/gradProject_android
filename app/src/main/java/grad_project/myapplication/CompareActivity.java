package grad_project.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.autofill.FieldClassification;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.NORM_HAMMING;

public class CompareActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = CompareActivity.class.getSimpleName();
    private SharedPreferences infoData;
    private ImageView checkImg;
    private int imgAddr;
    private int imgNum;
    private int exhibitionNum;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private long mLastClickTime = 0;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

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

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        checkImg = (ImageView)findViewById(R.id.correct_img);
        infoData = getSharedPreferences("infoData", MODE_PRIVATE);

        Intent intent = getIntent();
        //해당 이미지의 주소(R.drawable...), 사진번호, 해당 이미지의 전시관 가져옴
        imgAddr = intent.getIntExtra("imgAddress", -1);
        exhibitionNum = intent.getIntExtra("exhibitionNum", -1);     //전시관 번호(0~5)
        imgNum = intent.getIntExtra("imgNum", -1);                   //몇번째 이미지인지(0~max-1)
        Log.d(TAG, "imgAddr: " + imgAddr);
        Log.d(TAG, "exhibitionNum: " + exhibitionNum);
        Log.d(TAG, "imgNum: " + imgNum);
        checkImg.setImageResource(imgAddr);  //이미지 왼쪽 위에 띄움

        ImageButton shutter = (ImageButton) findViewById(R.id.button_capture1);
        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 10000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                uploadImage(matResult);
            }
        });
    }

    public void uploadImage(Mat mat) {
        if (mat != null) {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matResult, bitmap);
            checkImg.setImageBitmap(bitmap);
            Log.d(TAG, "bitmap matResult: " + bitmap.getWidth() + " " + bitmap.getHeight() + " " + bitmap.getConfig());
        }
        else {
            Log.d(TAG, "Image picker gave us a null image.");
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
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

/*
    private class FeatureComparingTask extends AsyncTask<Mat, Void, Integer> {
        private final static int MIN_CORRECT_NUM = 20;
        private WeakReference<CompareActivity> mActivityWeakReference;
        private ProgressDialog asyncDialog;

        @Override
        protected void onPreExecute() {
            mActivityWeakReference = new WeakReference<>(CompareActivity.this);
            asyncDialog = new ProgressDialog(CompareActivity.this);
            asyncDialog.setCancelable(false);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("처리중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Mat... mat) {
            Bitmap bit = Bitmap.createBitmap(mat[0].cols(), mat[0].rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat[0], bit);
            return 0;

            int correctNum = 0;
            try {
                final int k = 2;
                final float nndrRatio = 0.65f;
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), imgAddr);
                Mat correctImg =  new Mat(0,0, CvType. CV_32FC2);
                Utils.bitmapToMat(bmp, correctImg);
                Mat compareImg =  mat[0];
                Log.d(TAG, "sssscor: " + correctImg.size().area());
                Log.d(TAG, "sssscom: " + compareImg.size().area());
                MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
                MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
                Mat descriptors1 = new Mat();
                Mat descriptors2 = new Mat();

                ORB orb = ORB.create(2000);
                orb.detectAndCompute(correctImg, new Mat(), keypoints1, descriptors1);
                Log.d(TAG, "11111: ");
                orb.detectAndCompute(compareImg, new Mat(), keypoints2, descriptors2);
                if(keypoints2.size().area() == 0) {
                    Log.d(TAG, "keypoints_size: " + descriptors2.size().area());
                    return -1;
                }
                if(descriptors2.size().area() == 0) {
                    Log.d(TAG, "descriptors2_size: " + descriptors2.size().area());
                    return -1;
                }
                Log.d(TAG, "22222: ");
                Log.d(TAG, "ssss1: " + keypoints1.size().area());
                Log.d(TAG, "ssss2: " + descriptors1.size().area());
                Log.d(TAG, "ssss3: " + keypoints2.size().area());
                Log.d(TAG, "ssss4: " + descriptors2.size().area());
                List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
                BFMatcher bfMatcher = new BFMatcher(NORM_HAMMING);
                Log.d(TAG, "33333: ");
                bfMatcher.knnMatch(descriptors1, descriptors2, matches,k);
                Log.d(TAG, "matches size: " + matches.size());

                List<DMatch> goodMatches = new ArrayList<DMatch>();
                for(int i = 0 ; i < matches.size() ; i++) {
                    if(matches.get(i).size().area() == 2 &&
                            matches.get(i).toArray()[0].distance <=  nndrRatio * matches.get(i).toArray()[1].distance) {
                        goodMatches.add(matches.get(i).toArray()[0]);
                    }
                }
                Log.d(TAG, "goodMatches size: " + goodMatches.size());
                correctNum = goodMatches.size();
            } catch (NullPointerException e) {
                Log.d(TAG, "algorithm NullPointerException" + e.getMessage());
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.d(TAG, "algorithm ArrayIndexOutOfBoundsException " +
                        e.getMessage());
                e.printStackTrace();
            }
            return correctNum;
        }

        protected void onPostExecute(int correctNum) {
            String resultMessage;
            boolean isSuccess = false;
            if(correctNum < MIN_CORRECT_NUM) {
                resultMessage = "사진이 일치하지 않습니다.";
            } else {    //사진이 일치하는 경우
                SharedPreferences.Editor editor = infoData.edit();  //Share preference 설정해주고
                editor.putBoolean("IS_CHECK_PIC_" + (exhibitionNum  + 1) + "-" + (imgNum + 1), true);
                editor.apply();
                resultMessage = "사진이 일치합니다.";   //띄울 메시지
                isSuccess = true;
            }
            asyncDialog.dismiss();
            Toast.makeText(getApplicationContext(), resultMessage + correctNum + "/" + MIN_CORRECT_NUM, Toast.LENGTH_LONG).show();
//resultMessage 띄워줌
            CompareActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                Intent intent = new Intent(CompareActivity.this, NormalActivity.class);
                if(isSuccess) {
                    intent.putExtra("isSuccess", true);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }*/
}