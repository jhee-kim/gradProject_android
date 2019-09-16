#include <jni.h>
#include <opencv2/opencv.hpp>
#include "opencv2/core.hpp"
#include "opencv2/core/utility.hpp"
#include "opencv2/core/ocl.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/calib3d.hpp"
#include "opencv2/imgproc.hpp"
#include <iostream>
#include <android/log.h>

using namespace cv;
using namespace std;

const int LOOP_NUM = 10;
const float RATIO = 0.65f;

struct ORBDetector {
    Ptr<Feature2D> orb;
    ORBDetector(double hessian = 2000)
    {
        orb = ORB::create(hessian);
    }
    template<class T>
    void operator()(const T& in, const T& mask, std::vector<cv::KeyPoint>& pts, T& descriptors, bool useProvided = false)
    {
        orb->detectAndCompute(in, mask, pts, descriptors, useProvided);
    }
};
struct ORBMatcher {
    Ptr<BFMatcher> matcher;
    ORBMatcher() {
        matcher = BFMatcher::create(NORM_HAMMING);
    }
    template<class T>
    void operator()(const T &in1, const T &in2, std::vector<std::vector<cv::DMatch>>& pts, int k) {
        matcher->knnMatch(in1, in2, pts, k);
    }
};

float resize(UMat img_src, UMat &img_resize, int resize_width){

    float scale = resize_width / (float)img_src.cols ;

    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    }
    else {
        img_resize = img_src;
    }
    return scale;
}

extern "C"
JNIEXPORT void JNICALL
Java_grad_1project_myapplication_OcrActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                              jlong matAddrInput,
                                                              jlong matAddrResult) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

extern "C"
JNIEXPORT void JNICALL
Java_grad_1project_myapplication_CompareActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                  jlong matAddrInput,
                                                                  jlong matAddrResult) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

extern "C"
JNIEXPORT int JNICALL
Java_grad_1project_myapplication_CompareActivity_imageprocessing(JNIEnv *env, jobject thiz,
                                                                 jlong objectImage,
                                                                 jlong sceneImage) {
    ocl::setUseOpenCL(true);

    UMat img1, img2;

    Mat &img_object = *(Mat *) objectImage;
    Mat &img_scene = *(Mat *) sceneImage;

    img_object.copyTo(img1);
    img_scene.copyTo(img2);

    resize(img2, img2, 800);
    resize(img1, img1, 800);

    cvtColor( img1, img1, COLOR_RGBA2GRAY);
    cvtColor( img2, img2, COLOR_RGBA2GRAY);

    //declare input/output
    std::vector<KeyPoint> keypoints1, keypoints2;
    std::vector<std::vector<cv::DMatch>> matches;

    UMat _descriptors1, _descriptors2;
    Mat descriptors1 = _descriptors1.getMat(ACCESS_RW),
            descriptors2 = _descriptors2.getMat(ACCESS_RW);

    //instantiate detectors/matchers
    ORBDetector orb;
    ORBMatcher matcher;

    for (int i = 0; i <= LOOP_NUM; i++){
        orb(img1.getMat(ACCESS_READ), Mat(), keypoints1, descriptors1);
        orb(img2.getMat(ACCESS_READ), Mat(), keypoints2, descriptors2);
        matcher(descriptors1, descriptors2, matches, 2);
    }
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "%d keypoints on object image", keypoints1.size());
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "%d keypoints on scene image", keypoints2.size());
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "%d matches on scene image", matches.size());

    std::vector<DMatch> good_matches;
    for (int i = 0; i < matches.size(); i++) {
        if(matches[i].size() == 2 && matches[i][0].distance <= matches[i][1].distance * RATIO) {
            good_matches.push_back(matches[i][0]);
        }
    }

    return good_matches.size();
}