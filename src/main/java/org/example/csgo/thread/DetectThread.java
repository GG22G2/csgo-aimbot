package org.example.csgo.thread;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.foreign.MemoryAddressImpl;
import org.example.csgo.Config;
import org.example.csgo.HistoryRecord;
import org.example.csgo.Location;
import org.example.csgo.Locations;
import org.example.csgo.utils.RecordImageDetect;
import org.example.csgo.wrapper.DectectWrapper;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @date 2022/2/12 17:06
 * <p>
 * 定位csgo中警和匪位置的线程
 */
public class DetectThread implements Runnable {
    Locations locations;

    ReentrantLock lock;
    Condition condition;
    HistoryRecord historyRecord = HistoryRecord.getInstance();

    final List<Long> useTimeRecord = new ArrayList<>(1000);
    public MouseListener mouseListener = null;

    public DetectThread(Locations locations, ReentrantLock lock, Condition condition) {
        this.locations = locations;
        this.lock = lock;
        this.condition = condition;
    }


    final int cx = (int) (Config.SOURCE_WIDTH * Config.scale / 2);
    final int cy = (int) (Config.SOURCE_HEIGHT * Config.scale / 2);

    @Override
    public void run() {
        try {
            lock.lock();
            ResourceScope scope = ResourceScope.newConfinedScope();

            //初始化模型
            MemorySegment engineMemory = CLinker.toCString(Config.YOLOV5_MODEL_PATH, scope);
            DectectWrapper.detecte_init.invoke(engineMemory.address());
            System.out.println("yolov5检查初始化成功");
            lock.unlock();
            Mat captureRes;

            int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
            int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);

//            new Thread(()->{
//                File file = new File("useTimeRecord.txt");
//                try {
//                    FileOutputStream fileOutputStream = new FileOutputStream(file);
//                    OutputStreamWriter outputStream = new OutputStreamWriter(fileOutputStream);
//                    while (!Thread.currentThread().isInterrupted()){
//                        synchronized (useTimeRecord){
//                            if (useTimeRecord.size()>0) {
//                                for (Long time : useTimeRecord) {
//                                    outputStream.write(""+time.toString()+",");
//                                }
//                                outputStream.flush();
//                                useTimeRecord.clear();
//                            }
//                        }
//                    }
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//
//            }).start();
            // ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(6, 6, 0, TimeUnit.DAYS, new LinkedBlockingDeque<>());
            //这个线程用来判断是否存在 识别错误的情况
//            new Thread(() -> {
//                while (true){
//                    long startNanos_7_74 = System.nanoTime();
//                    List<Locations.CaptureRecord> captureRecords = historyRecord.detectNoShot();
//                    long endNanos_7_76 = System.nanoTime();
//                    System.out.println((endNanos_7_76 - startNanos_7_74) / 1000000.0);
//                    poolExecutor.execute(() -> {
//                        for (Locations.CaptureRecord captureRecord : captureRecords) {
//                            try {
//                                Imgcodecs.imwrite("G:\\dataset\\csgo\\unBioaji3\\" + captureRecord.time + ".png", captureRecord.mat);
//                                captureRecord.mat.release();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ignored) {
//                    }
//                }
//
//            },"detectDefect").start();

            //RecordImageDetect recordImageDetect = new RecordImageDetect();
            //recordImageDetect.start();
            while (true) {
                try {
                    Locations.CaptureRecord captureRecord = locations.nextCapture();

                    captureRes = captureRecord.mat;
                    Mat showImg = captureRes;
                    MemoryAddressImpl bgrImgAddress = new MemoryAddressImpl(null, showImg.dataAddr());


                    MemoryAddress res = (MemoryAddress) DectectWrapper.detecte_inference.invokeExact(bgrImgAddress.address(), Config.DETECT_WIDTH, Config.DETECT_HEIGHT);

                   // useTimeRecord.add(endNanos_23_104 - startNanos_23_102);
                    //todo 这个方法有时候耗时达到30毫秒以上，此时gpu占用率达到100%了
//                    if (((endNanos_23_104 - startNanos_23_102) / 1000000.0)>30){
//                        System.out.println("高耗时"+   ((endNanos_23_104 - startNanos_23_102) / 1000000.0)+",休眠2秒");
//                        Thread.sleep(2000);
//                    }

                    //返回6*20个float  6个为一组  x,y,width,height,conf,classid
                    float[] rects = res.asSegment(6 * 20 * 4, scope).toFloatArray();
                    locations.update(screenX, screenY, rects, captureRecord.time);

                    //把识别结果记录到视频里
                    //recordImageToVideo(rects, showImg, recordImageDetect);

                   // showImg.release();

                    //todo 如果调用鼠标移动,鼠标移动的反馈到图片被捕捉到可能要几帧之后,这时候应该设置一个延迟忽略后续几帧
                    boolean track = mouseListener.track();
                    if (track){
                        Thread.sleep(5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lock.lock();
                        condition.signalAll();
                        lock.unlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void recordImageToVideo(float[] rects, Mat showImg, RecordImageDetect recordImageDetect) {
        for (int i = 0, j = 0; j < 120; i++, j += 6) {
            if (rects[j + 2] <= 0) {
                break;
            }
            float conf = rects[j + 4];
            if (conf < 0.7) {
                continue;
            }
            int x = (int) ((rects[j]) * Config.scale);
            int y = (int) ((rects[j + 1]) * Config.scale);
            int width = (int) (rects[j + 2] * Config.scale);
            int height = (int) (rects[j + 3] * Config.scale);
            Rect rect = new Rect(x, y, width, height);
            Imgproc.rectangle(showImg, rect, new Scalar(0, 255, 0), 1);
        }

        Location location = locations.minDistance(cx, cy);

        if (location != null) {
            double xPixel = location.centerX - cx;
            double yPixel = location.centerY - (location.height / 10.0) - cy;

            Imgproc.line(showImg, new Point(Config.DETECT_WIDTH / 2, Config.DETECT_HEIGHT / 2)
                    , new Point(Config.DETECT_WIDTH / 2 + xPixel, Config.DETECT_HEIGHT / 2 + yPixel)
                    , new Scalar(0, 255, 0), 1);
        }

        byte[] pixels = new byte[showImg.width() * showImg.height() * showImg.channels()];
        showImg.get(0, 0, pixels);
        recordImageDetect.addMat(pixels);
    }


}
