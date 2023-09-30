package hsb.aimbot.csgo.thread;


import hsb.aimbot.csgo.CSGODetect;
import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.Locations;
import hsb.aimbot.csgo.utils.Time;
import hsb.aimbot.csgo.wrapper.WinCaptureWrapper;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static hsb.aimbot.csgo.Config.screenX;
import static hsb.aimbot.csgo.Config.screenY;


/**
 * @author
 * @date 2022/2/13 18:28
 */
public class CaptureThread implements Runnable {

    Locations locations;
    ReentrantLock lock;

    Condition condition;
    CSGODetect detect;
    public CaptureThread(CSGODetect detect , Locations locations, ReentrantLock lock, Condition condition) {
        this.locations = locations;
        this.lock = lock;
        this.condition = condition;
        this.detect = detect;
    }

    AtomicInteger captureCount = new AtomicInteger(0);

    @Override
    public void run() {

        MemorySegment captureRes;
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
                //使用obs的 游戏截图方式
                if (!Config.detecting) {
                    Thread.sleep(1000);
                    continue;
                }

                int captureEndTime = Time.getTime();

                captureRes =detect.captureGameFrame();

                captureCount.incrementAndGet();

                //使用阻塞队列 录屏速度根据识别速度变换
                locations.addCapture(captureRes, captureEndTime);

                waitNotify();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    private void waitNotify() {
        lock.lock();
        try {
            condition.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }


}
