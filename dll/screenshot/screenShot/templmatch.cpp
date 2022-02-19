#include "pch.h"

#include <intrin.h>

const int lineLength = 16;
const int gap = 0;

float precision = 0.98f;

struct Image {
    int width;
    int height;
    unsigned char* image;
};

struct Image2 {
    int width;
    int height;
    unsigned long long* image;
};

bool similarityCal(Image templateImage, Image targetImage, int templateStartIndex, int* oneRowIndex, int matchPointLength);
bool similarityCal2(Image templateImage, Image2 targetImage, int templateStartIndex, int* oneRowIndex, int matchPointLength);
bool similarityCal3(Image templateImage, Image targetImage, int templateStartIndex, int* oneRowIndex, int matchPointLength);


 void match(unsigned char* templateImages, int templateWidth, int templateheight, unsigned char* targetImages, int targetWidth, int targetheigh , int* result) {
     result[0] = -1;
     result[1] = -1;
     if (templateWidth >= targetWidth && templateheight >= targetheigh) {
         Image templateImage = { templateWidth,templateheight,templateImages };
         Image targetImage = { targetWidth,targetheigh,targetImages };

         int span = lineLength + gap;
         //每一行的匹配点数量
         int rowNum = targetWidth / span;
         if (targetWidth - (span * rowNum) >= lineLength) {
             rowNum++;
         }
         //第一行的匹配点索引
         int* oneRowIndex = new int[rowNum];
         for (int j = 0, index = 0; j < rowNum; j++) {
             oneRowIndex[j] = index;
             index += span;
         }

         int matchPointNum = rowNum * targetheigh;

         int col = templateWidth - targetWidth;
         int row = templateheight - targetheigh;


         for (int i = 0, index = 0; i < row; i++, index += templateWidth) {
             for (int j = 0, position = index; j < col; j++) {
                 bool b = similarityCal3(templateImage, targetImage, position++, oneRowIndex, rowNum);
                 if (b) {
                     result[0] = j;
                     result[1] = i;
                 }
             }
         }
     }
}


 bool similarityCal(Image templateImage,Image targetImage, int templateStartIndex, int* oneRowIndex , int matchPointLength ) {

     unsigned char* template1  = templateImage.image;
     unsigned char* target = targetImage.image;

     int targetStartIndex = 0;
     int templateIndex;
     int targetIndex;

     int matchCount = 0;
     int missCount = 0;

     for (int i = 0; i < targetImage.height; i++) {
         int result;
         for (int j = 0; j < matchPointLength; j++) {
             templateIndex = oneRowIndex[j] + templateStartIndex;
             targetIndex = oneRowIndex[j] + targetStartIndex;

             result = 0;
             for (int k = 0; k < lineLength; k++) {
                 result += template1[templateIndex + k] - target[targetIndex + k];
             }
             if (result != 0) {
                 missCount++;
                 if (missCount > 4) {
                     return false;
                 }
             }
         }
         templateStartIndex += templateImage.width;
         targetStartIndex += targetImage.width;
     }

     //统计多少个区域匹配上了
    // if (1 - (missCount / matchPointNum) >= precision) {
    //     return true;
   //  }
    
     return true;
 }


 bool similarityCal2(Image templateImage, Image2 targetImage, int templateStartIndex, int* oneRowIndex, int matchPointLength) {

     unsigned char* template1 = templateImage.image;
     unsigned long long* target = targetImage.image;

     int templateIndex;
     int targetIndex;

     int matchCount = 0;
     int missCount = 0;

     for (int i = 0, targetIndex=0; i < targetImage.height; i++) {
         int result;
         for (int j = 0; j < matchPointLength; j++) {
             templateIndex = oneRowIndex[j] + templateStartIndex;
    
             //8字节长度计算
             unsigned long long r = *(long long*)(template1 + templateIndex);
             unsigned long long r2 = *(target + targetIndex++);
             result = r - r2;
             
             missCount += result != 0 ? 1 : 0;
             if (missCount > 6) {
                 return false;
             }

         }
         templateStartIndex += templateImage.width;
     }
     return true;
 }


 //这是一个失败的测试，使用simd后并没有起到加速效果，可能和内存没有对齐，用法有误有关吧。
 bool similarityCal3(Image templateImage, Image targetImage, int templateStartIndex, int* oneRowIndex, int matchPointLength) {

     unsigned char* template1 = templateImage.image;
     unsigned char* target = targetImage.image;
     int targetStartIndex = 0;
     int templateIndex;
     int targetIndex;
     int missCount = 0;
     int result = 0;
     unsigned char* r = new unsigned char[4];
     for (int i = 0; i < targetImage.height; i++) {
         for (int j = 0 ; j < matchPointLength; j++) {
             templateIndex = oneRowIndex[j] + templateStartIndex ;
             targetIndex = oneRowIndex[j] + targetStartIndex ;
             
             __m128i m =  _mm_loadu_si128((__m128i*)(template1 + templateIndex));
             __m128i m2 = _mm_loadu_si128((__m128i*)(target+ targetIndex));
             __m128i m3 = _mm_sub_epi8(m, m2);
             _mm_store_si128((__m128i*)r, m3);
             result = result + r[0];
             //for (int k = 0; k < 4; k++) {
             //    result += r[k];
             //}
             //if (result != 0) {
             //    missCount++;
             //    if (missCount > 4) {
             //        return false;
             //    }
             //}
         }
         templateStartIndex += templateImage.width;
         targetStartIndex += targetImage.width;
     }

     //统计多少个区域匹配上了
    // if (1 - (missCount / matchPointNum) >= precision) {
    //     return true;
   //  }
 	
     return false;
 }