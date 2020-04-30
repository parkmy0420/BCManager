#include <jni.h>
#include "com_example_bcmanager_MainActivity.h"

#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;


void filter(Mat img, Mat &dst, Mat mask);


extern "C" {

JNIEXPORT void JNICALL Java_com_example_bcmanager_MainActivity_ConvertRGBtoGray(
        JNIEnv *env, jobject instance, jlong matAddrInput, jlong matAddrResult) {

    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);

}

JNIEXPORT void JNICALL Java_com_example_bcmanager_MainActivity_BlurImage(JNIEnv *env, jobject thiz,
                                                                         jlong input_image,
                                                                         jlong output_image) {
    // TODO: implement BlurImage()

    Mat &input = *(Mat *) input_image;
    CV_Assert(input.data);
    Mat &output = *(Mat *) output_image;
    vector<int>::iterator it;

    output = Mat::zeros(input.rows, input.cols, CV_8UC3);
    Mat tmp = Mat::zeros(input.rows, input.cols, CV_8UC3);

//    cvtColor( input, output, COLOR_RGB2GRAY);


//    filter(input, output, mask);

    Canny(input, output, 100, 200, 3, false);

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;

    findContours(output, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0));

    vector<Point> cnt = contours[0];
    drawContours(input, contours, -1, CV_RGB(0, 255, 0), 3);

    double epsilon = 0.1 * arcLength(cnt, true);

    vector<Point> approx;
    vector<vector<Point>> result_approx;

    approxPolyDP(cnt, approx, epsilon, true);

//    result_approx.insert(result_approx.begin(),approx.begin(), approx.end());
    result_approx.push_back(approx);
    drawContours(tmp, result_approx, -1, CV_RGB(0, 255, 0), 5);
    output = tmp;

}

}

void filter(Mat img, Mat &dst, Mat mask) {
    dst = Mat(img.size(), CV_32F, Scalar(0));
    Point h_m = mask.size() / 2;

    for (int i = h_m.y; i < img.rows - h_m.y; i++) {
        for (int j = h_m.x; j < img.cols - h_m.y; j++) {

            float sum = 0;
            for (int u = 0; u < mask.rows; u++) {
                for (int v = 0; v < mask.cols; v++) {
                    int y = i + u - h_m.y;
                    int x = j + v - h_m.x;
                    sum += mask.at<float>(u, v) * img.at<uchar>(y, x); //회선수식
                }
            }

            dst.at<float>(i, j) = sum;
        }
    }

    dst.convertTo(dst, CV_8U);
}