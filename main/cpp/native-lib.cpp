#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;

//중심점으로 부터 거리를 가지고 각도가 비슷한지 체크
bool findSameAngle(std::vector<Point2f> &approx, float errorRate, long minR);

//다각형 전체를 포함하는 큰 사각형을 만듬.
void findBigRect(std::vector<Point2f> &approx, Point2f *output);


// 전역 변수
auto gray = Mat();
auto tmp_blur = Mat();
Size size = Size(3,3);
auto tmp1 = Mat();

extern "C" JNIEXPORT jstring JNICALL
Java_com_heesu_dudogy_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_heesu_dudogy_CvCameraViewListener_findRectangle(JNIEnv *env, jobject thiz,
                                                         jlong input_address, jlong box_address) {
    Mat &input = *(Mat*)input_address;
    Mat2f &box = *(Mat2f*)box_address;
    std::vector<Point2f> approx;
    std::vector<std::vector<cv::Point>> contours;

    int index = 0;
    int maxIndex = box.rows;

    auto boxPoint = box.ptr<Point2f>();

    line(input , Point(0,0), Point(100,0), Scalar(0,255,0,255), 3);
    line(input , Point(0,0), Point(0,20), Scalar(0,255,0,255), 3);

    cvtColor(input, gray, COLOR_RGBA2GRAY);
    GaussianBlur(input, tmp_blur, size, 0);
    adaptiveThreshold(gray, tmp1, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 31, -20);
    findContours(tmp1, contours,RETR_LIST, CHAIN_APPROX_SIMPLE);
    for (const auto &contour : contours){
        if(index < maxIndex) {
            approxPolyDP(Mat(contour), approx, arcLength(Mat(contour), true) * 0.02F, true);
            if (approx.size() == 4) {
                if (findSameAngle(approx, 0.15F, 33)) {
                    // 주의!!! 사각형은 2 * 2 가 필요하기 때문에 기존 Point2f에서 * 2 만큼 이동하게 해야 함 아마도
                    circle(input, *(boxPoint + (index * 2)), 3, Scalar(0,0,255,255));
                    circle(input, *(boxPoint + (index * 2 + 1)), 3, Scalar(255,0,0,255));
                    findBigRect(approx, boxPoint + (index++ * 2));
                    line(input, approx[0], approx[1], Scalar(0,0,255,255), 3);
                    line(input, approx[1], approx[2], Scalar(0,255,0,255), 3);
                    line(input, approx[2], approx[3], Scalar(0,255,0,255), 3);
                    line(input, approx[3], approx[0], Scalar(0,255,0,255), 3);
                }
            }
        }
        else
            break;
    }
    // 나머지는 숨겨지는 효과를 주기 위해
    for (; index < maxIndex; index++) {
        boxPoint[index * 2].x = -10;
        boxPoint[index * 2].y = -10;
        boxPoint[index * 2 + 1].x = -5;
        boxPoint[index * 2 + 1].y = -5;
    }

}

// 생각해 보니 rvalue로 관리하는게 좋을거 같아 수정
bool findSameAngle(std::vector<Point2f> &approx, float errorRate, long minR){
    float meanX = (approx[0].x + approx[1].x + approx[2].x + approx[3].x) / 4;
    float meanY = (approx[0].y + approx[1].y + approx[2].y + approx[3].y) / 4;
    float r1 = sqrt((approx[0].x - meanX) * (approx[0].x - meanX) + (approx[0].y - meanY) * (approx[0].y - meanY));
    float r2 = sqrt((approx[1].x - meanX) * (approx[1].x - meanX) + (approx[1].y - meanY) * (approx[1].y - meanY));
    float r3 = sqrt((approx[2].x - meanX) * (approx[2].x - meanX) + (approx[2].y - meanY) * (approx[2].y - meanY));
    float r4 = sqrt((approx[3].x - meanX) * (approx[3].x - meanX) + (approx[3].y - meanY) * (approx[3].y - meanY));
    float meanR = (r1 + r2 + r3 + r4) / 4;

    if( meanR > minR){
        if((1 + errorRate) < (r1 / meanR) or (r1 / meanR) < (1 - errorRate))
            return false;
        else if((1 + errorRate) < (r2 / meanR) or (r2 / meanR) < (1 - errorRate))
            return false;
        else if((1 + errorRate) < (r3 / meanR) or (r3 / meanR) < (1 - errorRate))
            return false;
        else
            return !((1 + errorRate) < (r4 / meanR) or (r4 / meanR) < (1 - errorRate));
    }
    return false;
}


void findBigRect(std::vector<Point2f> &approx, Point2f *output){
    float minX = approx[0].x;
    float minY = approx[0].y;
    float maxX = approx[0].x;
    float maxY = approx[0].y;
    if (minX > approx[1].x)
        minX = approx[1].x;
    else if (maxX < approx[1].x)
        maxX = approx[1].x;
    if (minX > approx[2].x)
        minX = approx[2].x;
    else if (maxX < approx[2].x)
        maxX = approx[2].x;
    if (minX > approx[3].x)
        minX = approx[3].x;
    else if (maxX < approx[3].x)
        maxX = approx[3].x;
    if (minY > approx[1].y)
        minY = approx[1].y;
    else if (maxY < approx[1].y)
        maxY = approx[1].y;
    if (minY > approx[2].y)
        minY = approx[2].y;
    else if (maxY < approx[2].y)
        maxY = approx[2].y;
    if (minY > approx[3].y)
        minY = approx[3].y;
    else if (maxY < approx[3].y)
        maxY = approx[3].y;
    output[0].x = minX;
    output[0].y = minY;
    output[1].x = maxX;
    output[1].y = maxY;
}

