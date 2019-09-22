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

const int GOOD_PTS_MAX = 50;
const float GOOD_PORTION = 0.15f;
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

float resize(Mat img_src, Mat &img_resize, int resize_width){

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

    orb(img1.getMat(ACCESS_READ), Mat(), keypoints1, descriptors1);
    orb(img2.getMat(ACCESS_READ), Mat(), keypoints2, descriptors2);
    if(keypoints1.size() == 0 || keypoints2.size() == 0) {
        return -1;
    }
    matcher(descriptors1, descriptors2, matches, 2);

    if(matches.size() == 0) {
        return -1;
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

///////////////////////////////////////////////////////////////////////////////
void sortCorners(std::vector<cv::Point2f>& corners)         //Ecken ausrichten von Karten
{
    std::vector<cv::Point2f> top, bot;
    cv::Point2f center;
    // Get mass center
    for (int i = 0; i < corners.size(); i++)
        center += corners[i];
    center *= (1. / corners.size());

    for (int i = 0; i < corners.size(); i++)
    {
        if (corners[i].y < center.y)
            top.push_back(corners[i]);
        else
            bot.push_back(corners[i]);
    }
    corners.clear();

    if (top.size() == 2 && bot.size() == 2) {
        cv::Point2f tl = top[0].x > top[1].x ? top[1] : top[0];
        cv::Point2f tr = top[0].x > top[1].x ? top[0] : top[1];
        cv::Point2f bl = bot[0].x > bot[1].x ? bot[1] : bot[0];
        cv::Point2f br = bot[0].x > bot[1].x ? bot[0] : bot[1];

        corners.push_back(tl);
        corners.push_back(tr);
        corners.push_back(br);
        corners.push_back(bl);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_grad_1project_myapplication_OcrActivity_warp(JNIEnv *env, jobject thiz,
                                                  jlong input_mat) {
    ocl::setUseOpenCL(true);

    Mat &img = *(Mat *) input_mat;

    Mat quad;

    /*전처리*/
    //resize(img, img, 800);
    Mat reImg = img.clone();

    cvtColor( img, img, COLOR_BGR2GRAY);
    GaussianBlur(img, img, Size(5, 5), 1.5, 1.5);

    erode(img, img, Mat());// these lines may need to be optimized
    dilate(img, img, Mat());
    dilate(img, img, Mat());
    erode(img, img, Mat());

    Canny(img, img, 50, 150, 3); // canny parameters may need to be optimized

    vector<Point> selected_points;
    vector<vector<Point>> contours;
    Mat hierarchy;

    findContours(img, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
    sort(contours.begin(), contours.end(), [](const vector<Point>& c1, const vector<Point>& c2) {
        return contourArea(c1, false) > contourArea(c2, false);
    });

    vector<Point> approx;
    for (size_t i = 0; i < contours.size(); i++) {
        approxPolyDP(contours[i], approx, 0.02 * arcLength(contours[i], true), true);

        if (approx.size() == 4) {
            selected_points.insert(selected_points.end(), contours[i].begin(), contours[i].end());
            break;
        }
    }

    vector<Point2f> selected_points_f;
    vector<Point2f> corners;
    Mat(selected_points).convertTo(selected_points_f, CV_32F);
    Mat hull;
    convexHull(selected_points_f, hull, true, true);            //외각선 찾기(점 잇기 알고리즘)

    RotatedRect RRect = minAreaRect(hull);
    std::vector<cv::Point2f> RR_corners;
    Point2f four_points[4];
    RRect.points(four_points);
    RR_corners.push_back(four_points[0]);
    RR_corners.push_back(four_points[1]);
    RR_corners.push_back(four_points[2]);
    RR_corners.push_back(four_points[3]);

    for (int j = 0; j < 4; j++)
    {
        Point2f pt = RR_corners[j];
        Point2f nearest_pt = hull.at<Point2f>(j, 0);
        float dist = norm(pt - nearest_pt);
        for (int k = 1; k < hull.rows; k++)
        {
            Point2f hull_point = hull.at<Point2f>(k, 0);
            if (norm(pt - hull_point) < dist)
            {
                dist = norm(pt - hull_point);
                nearest_pt = hull_point;
            }
        }
        corners.push_back(nearest_pt);
    }
    sortCorners(corners);

    Mat(corners).convertTo(selected_points, CV_32S);

    Rect r = boundingRect(corners);
    quad = cv::Mat::zeros(norm(corners[1] - corners[2]), norm(corners[2] - corners[3]), CV_8UC3);

    std::vector<cv::Point2f> quad_pts;
    quad_pts.push_back(cv::Point2f(0, 0));
    quad_pts.push_back(cv::Point2f(quad.cols, 0));
    quad_pts.push_back(cv::Point2f(quad.cols, quad.rows));
    quad_pts.push_back(cv::Point2f(0, quad.rows));

    cv::Mat transmtx = cv::getPerspectiveTransform(corners, quad_pts);
    cv::warpPerspective(reImg, quad, transmtx, quad.size());

    img = quad;
}