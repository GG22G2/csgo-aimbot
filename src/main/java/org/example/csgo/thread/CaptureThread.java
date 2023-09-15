package org.example.csgo.thread;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;
import org.example.csgo.Config;
import org.example.csgo.Locations;
import org.example.csgo.wrapper.WinCaptureWrapper;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.csgo.wrapper.WinCaptureWrapper.init_csgo_capture;

/**
 * @author
 * @date 2022/2/13 18:28
 */
public class CaptureThread implements Runnable {

    Locations locations;
    ReentrantLock lock;

    Condition condition;

    public CaptureThread(Locations locations, ReentrantLock lock, Condition condition) {
        this.locations = locations;
        this.lock = lock;
        this.condition = condition;
    }

    AtomicInteger captureCount = new AtomicInteger(0);

    @Override
    public void run() {
        lock.lock();

        MemoryAddress dateAddress = null;
        try {
            while (true) {
                dateAddress =  init_csgo_capture();

                if (dateAddress.toRawLongValue() == 0) {
                    Thread.sleep(20);
                } else {

                    while (true) {
                        MemoryAddress captureRes = (MemoryAddress) WinCaptureWrapper.game_capture_tick_cpu.invokeExact(dateAddress, 4.0f);
                        if (captureRes.toRawLongValue() != 0) {
                            break;
                        }
                    }
                    System.out.println("截图成功");
                    MemoryAddress finalDateAddress = dateAddress;
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                Config.detecting = false;
                                WinCaptureWrapper.stop_game_capture.invoke(finalDateAddress);
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        lock.unlock();
        MemoryAddress captureRes;
        long totalTime = 0;

        int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
        int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);

        ResourceScope scope = ResourceScope.newConfinedScope();
        Mat bgrDetectImg = new Mat(Config.DETECT_WIDTH, Config.DETECT_HEIGHT, CvType.CV_8UC3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int lastCount = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                        int t = captureCount.get();
                        System.out.println("detect img/s " + (t - lastCount));
                        lastCount = t;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            try {

                long captureStartTime = System.currentTimeMillis();

                //使用obs的 游戏截图方式
                if (!Config.detecting) {
                    Thread.sleep(1000);
                    continue;
                }

                long captureEndTime = System.currentTimeMillis();
                captureRes = (MemoryAddress) WinCaptureWrapper.game_capture_tick_cpu.invokeExact(dateAddress, 4.0f);
                if (captureRes.toRawLongValue() == 0) {
                    continue;
                }


                captureCount.incrementAndGet();

                ByteBuffer imageBuffer = captureRes.asSegment(1920 * 1080 * 4, scope).asByteBuffer();

                //截屏返回的是四通道BGRA格式,这里转成BGR,并截取识别区域
                Mat bgraImg = new Mat(1080, 1920, CvType.CV_8UC4, imageBuffer);
                Rect roi = new Rect(screenX, screenY, Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
                Mat bgraImgRoi = new Mat(bgraImg, roi);
                Imgproc.cvtColor(bgraImgRoi, bgrDetectImg, Imgproc.COLOR_BGRA2BGR);  //CSGO
                // Imgproc.cvtColor(bgraImgRoi, bgrDetectImg, Imgproc.COLOR_RGBA2BGR);  // 守望先锋

                bgraImg.release();
                bgraImgRoi.release();

                //使用阻塞队列 录屏速度根据识别速度变换
                locations.addCapture(bgrDetectImg, captureEndTime);


                waitNotify();

//                long useTime = System.currentTimeMillis() - captureStartTime;
//
//                //控制识别速率
//                if (useTime <= Config.CAPTURE_AND_DETECT_TIME) {
//                    try {
//                        Thread.sleep((int) (Config.CAPTURE_AND_DETECT_TIME - useTime));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    private void waitNotify() {
        try {
            lock.lock();
            condition.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }


}
