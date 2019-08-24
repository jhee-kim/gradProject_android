package grad_project.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OcrActivity  extends Activity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyC5ilTz6H2zJCkC4joiZnW2T7IDouhmZwY";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final String TAG = OcrActivity.class.getSimpleName();

    Preview preview;
    Camera camera;
    Activity act;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ocr);

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        ImageButton shutter = (ImageButton) findViewById(R.id.button_capture);
        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });
    }


    public void uploadImage(Bitmap bitmap) {
        if (bitmap != null) {

            OpenCVLoader.initDebug();
            Mat grayImage =  new Mat(0,0, CvType.CV_32FC2);
            Utils.bitmapToMat(bitmap, grayImage);
            Imgproc.cvtColor(grayImage.clone(), grayImage, Imgproc.COLOR_RGB2GRAY);
            Imgproc.adaptiveThreshold(grayImage.clone(), grayImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 10);
            Utils.matToBitmap(grayImage, bitmap);

            /*
            Imgproc.GaussianBlur(grayImage, grayImage, new Size(3, 3), 0);
            Imgproc.Canny(grayImage, grayImage, 75, 200);

            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();

            Imgproc.findContours(grayImage, contours, hierarchy,  Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double maxArr = 0;
            MatOfPoint2f temp = new MatOfPoint2f();
            for(MatOfPoint contour : contours) {
                MatOfPoint2f approx = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                Imgproc.approxPolyDP(contour2f, approx, 0.02*Imgproc.arcLength(contour2f, true), true);

                if(approx.toArray().length == 4 && Imgproc.contourArea(contour) > maxArr) {
                    maxArr = Imgproc.contourArea(contour);
                    temp = approx;
                }
            }
            if(temp != null) {
                Rect rect1 = Imgproc.boundingRect(temp);
                Bitmap result1 = Bitmap.createBitmap(bitmap, (int) rect1.tl().x, (int) rect1.tl().y, rect1.width, rect1.height);
                Mat scaleImg = new Mat(0, 0, CvType.CV_32FC2);
                Utils.bitmapToMat(result1, scaleImg);
                Imgproc.cvtColor(scaleImg.clone(), scaleImg, Imgproc.COLOR_RGB2GRAY);
                Imgproc.adaptiveThreshold(scaleImg.clone(), scaleImg, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 10);
                //Mat kernel =  new Mat(7,7,CvType.CV_8S);
                //Imgproc.dilate(scaleImg.clone(), scaleImg, kernel);
                //Imgproc.erode(scaleImg.clone(), scaleImg, kernel);
                Utils.matToBitmap(scaleImg, result1);
                ImageView mMainImage1 = findViewById(R.id.main_image1);
                mMainImage1.setImageBitmap(result1);


                Mat rect = new Mat(4, 1, CvType.CV_32FC2);
                Mat dst = new Mat(4, 1, CvType.CV_32FC2);

                Point topLeft = temp.toArray()[1];
                Point topRight = temp.toArray()[0];
                Point bottomRight = temp.toArray()[3];
                Point bottomLeft = temp.toArray()[2];

                double w1 = abs(bottomRight.x - bottomLeft.x);
                double w2 = abs(topRight.x - topLeft.x);
                double h1 = abs(bottomRight.y - topRight.y);
                double h2 = abs(bottomLeft.y - topLeft.y);

                double maxWidth = max(w1, w2);
                double maxHeight = max(h1, h2);

                rect.put(4, 1, topLeft.x, topLeft.y, topRight.x, topRight.y, bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y);
                dst.put(4, 1, 0, 0, maxWidth, 0, maxWidth, maxHeight, 0, maxHeight);


                Mat N = Imgproc.getPerspectiveTransform(rect, dst);

                Imgproc.warpPerspective(imageCny, imageCny, N, new Size(maxWidth, maxHeight));
                Log.d("test-img", String.valueOf(imageCny));
                //Imgproc.cvtColor(imageCny, imageCny,  Imgproc.COLOR_RGB2GRAY);
                //Imgproc.adaptiveThreshold(imageCny, imageCny, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 10);

                Bitmap result2 = Bitmap.createBitmap(imageCny.cols(), imageCny.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(imageCny, result2);

                ImageView mMainImage2 = findViewById(R.id.main_image2);
                mMainImage2.setImageBitmap(result2);

                bitmap = result1;
            }
            */
            callCloudVision(bitmap);
        }
        else {
            Log.d(TAG, "Image picker gave us a null image.");
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<OcrActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(OcrActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            OcrActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                //TextView imageDetail = activity.findViewById(R.id.image_details);
                //imageDetail.setText(result);
            }
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");
        Log.d("text", String.valueOf(response));
        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.KOREA, "%s", label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }


    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open(0);
                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                uploadImage(bitmap);
            }
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/camtest");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
    }
}
