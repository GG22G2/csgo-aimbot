package org.example.csgo;

import org.opencv.core.Mat;

import java.util.*;
import java.util.concurrent.atomic.AtomicLongArray;

import org.example.csgo.Locations.CaptureRecord;

/**
 * @author 胡帅博
 * @date 2022/9/10 21:27
 */
public class HistoryRecord {

    private List<CaptureRecord> hisImgs;
    private volatile int hisImgPosition;


    //shotTimes中是按照 鼠标左键按下抬起的顺序记录的
    private final AtomicLongArray shotTimes;

    private volatile int position;
    //保存的历史帧数量
    private final int limit = 800;


    private static class HistoryRecordInstance {
        public static HistoryRecord historyRecord = new HistoryRecord();
    }


    public static HistoryRecord getInstance() {
        return HistoryRecordInstance.historyRecord;
    }

    private HistoryRecord() {
        hisImgs = new ArrayList<CaptureRecord>(10240000);
        shotTimes = new AtomicLongArray(1024000);
    }


    public synchronized void addCaptureRecord(CaptureRecord record) {
        hisImgs.add(record);
    }


    public void addShotRecord(long shotTime) {
        shotTimes.set(position++, shotTime);
        // System.out.println(position);
    }


    /**
     * 开枪了，但是没有识别到敌人
     * 开枪后的100毫秒内没有识别到敌人
     */
    public List<CaptureRecord> shotNoDetect() {


        return null;
    }


    /**
     * 这个方法用来筛选可能是被误识别为敌人的情况，
     * <p>
     * 识别到敌人，但是没有开枪的
     * 识别到敌人之后的2秒内没有开枪操作
     */
    public List<CaptureRecord> detectNoShot() {
        List<CaptureRecord> result = new LinkedList<>();
        int size = hisImgs.size();
        int imgP = hisImgPosition;
        if ((size - imgP) < limit) {
            return result;
        }

        synchronized (this) {
            //System.out.println("现有数量："+hisImgs.size());
            int end = imgP + (int) (limit * 0.3);
            int t = imgP;

            int p = position;
            long[] times = new long[p];
            for (int i = 0; i < times.length; i++) {
                times[i] = shotTimes.get(i);
            }
            p--;

            end = Math.min(size, end);
            while (t < end) {
                CaptureRecord record = hisImgs.get(t++);


                if (record.detectTarget) {
                    //如果识别到目标则判断这个时间点后是否有射击操作
                    long nextShotTime = nextShotTime(p, record.time - 20, times);
                    // -20 识别到前20毫秒开枪也算有效

                    if (nextShotTime > 3000) {
                        //  System.out.println(nextShotTime);
                        //开启记录模式，把识别到的都存起来
                       // result.add(record);
                        record.mat.release();
                    } else {
                        record.mat.release();
                        //  iterator.remove();
                    }
                } else {
                    long nextShotTime = nextShotTime(p, record.time, times);
                    if (nextShotTime == 0) {
                        //result.add(record);
                        record.mat.release();
                     //   System.out.println("开枪期间，没有敌人");
                    }else {
                        record.mat.release();
                    }

                    //  iterator.remove();
                }
            }
            //  System.out.println("剩余数量："+hisImgs.size());
            System.out.println("错误样本数：" + result.size());
            hisImgPosition = t;
        }


        return result;
    }

    public long nextShotTime(int endIndex, long startTime, long[] times) {
        if (endIndex < 0) {
            return Long.MAX_VALUE;
        }


        //startTime -= 1000; //识别到前1000毫秒内有开枪也算
        // long[] times = shotTimes.get;
        long minTime = Long.MAX_VALUE;
        long shotStart = 0;
        long shotEnd = 0;
        if (endIndex % 2 == 0) {
            shotStart = times[endIndex];
            shotEnd = shotStart + 10000;
            endIndex--;
        } else {
            shotEnd = times[endIndex];
            shotStart = times[endIndex - 1];
            endIndex -= 2;
        }
        if (startTime > shotStart && startTime < shotEnd) {
            return 0;
        } else if (startTime < shotStart) {
            minTime = shotStart - startTime;
        }
        for (int i = endIndex; i >= 0; i -= 2) {
            shotStart = times[i - 1];
            shotEnd = times[i];
            if (startTime >= shotStart && startTime <= shotEnd) {
                return 0;
            } else if (startTime < shotStart) {
                long t = shotStart - startTime;
                if (t < minTime) {
                    minTime = t;
                }
            } else {
                break;
            }
        }
        // System.out.println(minTime);
//        if (minTime > 3000) {
//            System.out.println((startTime) + "," + (minTime) + "," + Arrays.toString(times));
//        }
        return minTime;
    }


}
