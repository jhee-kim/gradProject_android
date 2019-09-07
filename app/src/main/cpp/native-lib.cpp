#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_grad_1project_myapplication_OcrActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                              jlong matAddrInput,
                                                              jlong matAddrResult) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}