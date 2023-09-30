package hsb.aimbot.csgo;

import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import hsb.aimbot.csgo.utils.Time;
import hsb.aimbot.csgo.wrapper.DectectWrapper;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;
import hsb.aimbot.csgo.wrapper.WinCaptureWrapper;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static hsb.aimbot.csgo.Config.*;

/**
 * @author 胡帅博
 * @date 2023/9/20 16:06
 */
public class CSGODetect {

    Locations locations;
    MemorySegment dataAddress;


    public static void main(String[] args) {
        double fef = 2.8;
        System.out.println((int) fef);
        long startNanos_41_29 = System.nanoTime();
        try {
            Thread.sleep(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endNanos_41_31 = System.nanoTime();
        System.out.println((endNanos_41_31 - startNanos_41_29) / 1000000.0);
    }

    public CSGODetect(Locations locations) {
        this.locations = locations;
    }

    public void init() {
        try {
            dataAddress = WinCaptureWrapper.initCapture();
            DectectWrapper.initDetectEngine();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public double[] positionCenter(int positionCount) {
        int xSumMove = 0;
        int ySumMove = 0;
        AtomicBoolean canContinue = new AtomicBoolean(true);
        AtomicLong xSpeed = new AtomicLong(0);
        AtomicLong ySpeed = new AtomicLong(0);


        try {

            new Thread(() -> {
                // 可能一毫米不能够移动距离，这时候可以依靠这个累计值，等到1后移动
                double xAccumulate = 0;
                double yAccumulate = 0;


                while (canContinue.get()) {

                    long startNanos = System.nanoTime();
                    Time.sleep(1);


                    long endNanos = System.nanoTime();
                    //获取实际睡眠的毫秒数
                    double realSleepMillis = (endNanos - startNanos) / 1000000.0;


                    double xv = xSpeed.get() * realSleepMillis / 1000000.0;
                    double yv = ySpeed.get() * realSleepMillis / 1000000.0;

                    int curXMove = (int) (xv + xAccumulate);
                    int curYMove = (int) (yv + yAccumulate);
                    xAccumulate = xv + xAccumulate - curXMove;
                    yAccumulate = yv + yAccumulate - curYMove;
                    if (Math.abs(curXMove) != 0||Math.abs(curYMove) != 0) {
                        try {
                            System.out.println(STR. "休眠时长:\{ realSleepMillis },本次移动：\{ curXMove },累计量:\{ xAccumulate }" );
                            MouseHelpWrapper.mouseMove.invokeExact((int) (curXMove), (int) (curYMove));
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
            //Thread.startVirtualThread();

            double lastXDirection = 0;
            double lastYDirection = 0;
            double xSlowFrameCount = 0;
            double ySlowFrameCount = 0;
            boolean needXSlow = false;
            boolean needYSlow = false;

            double lastWidth = 0;
            double lastHeight = 0;
            //假设4帧可以完全定位，那么剩下时间是一个跟踪和微调的过程，这个过程中如果突然出现一次需要大幅度拉位置的情况，可以认为是识别出错，或者敌人已经死亡了，立即结束

            //todo 找到第一个人之后，进行跟踪模式，如果丢失则尝试查找，但是如果发现偏移过多，则取消
            for (int i = 0; i < positionCount; i++) {
                detectOnce();

                Location nearestPoint = findNearestPoint();
                if (nearestPoint != null) {
                    double xPixel = nearestPoint.centerX - cx;                                  //x轴方向
                    double yPixel = nearestPoint.centerY - (nearestPoint.height / 10.0) - cy;       //y轴方向
                    int curXDirection = xPixel >= 0 ? 1 : -1;
                    int curYDirection = yPixel >= 0 ? 1 : -1;
                    xSumMove += xPixel;
                    ySumMove += yPixel;


                    if (i == 0) {
                        lastWidth = nearestPoint.width;
                        lastHeight = nearestPoint.height;
                        lastXDirection = curXDirection;
                        lastYDirection = curYDirection;
                    }
                    double diff = Math.abs(((lastWidth / nearestPoint.width + lastHeight / nearestPoint.height) / 2) - 1);

                    double diff2 = Math.abs(((lastWidth / lastHeight) - ((double) nearestPoint.width / nearestPoint.height)));


                    //System.out.println(STR. "定位\{ i + 1 }次,x:\{ xPixel },y:\{ yPixel },和上一次宽高比值的差异度:\{ diff },diff2:\{ diff2 }" );
                    /**
                     * diff小于0.06
                     * 或者少量位移情况下在0.15
                     * 或者考虑转身情况 极少量位移
                     * */

                    if (diff < 0.07 || (diff < 0.18 && Math.abs(xPixel) < nearestPoint.width)) {
                        if (i < 4) {
                            MouseHelpWrapper.mouseMove.invokeExact((int) xPixel, (int) yPixel);
                        } else {

                            //todo 如果开枪的话，如何处理枪械抬升导致的抖动
                            // MouseHelpWrapper.mouseMove.invokeExact(0, (int) yPixel);
                            if (lastXDirection != curXDirection || xSlowFrameCount > 0) {
                                //如果改变方向，为了防止抖动，则跳过一帧，但是把速度减小
                                long l = xSpeed.get();
                                xSpeed.set((long) (l / 10.0));
                                System.out.println(STR."x方向改变，减小速度");
                                if (xSlowFrameCount == 0) {
                                    xSlowFrameCount = 4;
                                }
                            } else {
                                if (Math.abs(xPixel) <= 4) {
                                    xSpeed.set(0); //配速
                                } else {
                                    double t = needXSlow ? 20.0 : 10.0;
                                    if (needXSlow) {
                                        needXSlow = false;
                                    }
                                    xSpeed.set((long) ((xPixel / t) * 1000000.0)); //配速
                                    System.out.println(STR. "x设置配速\{ xPixel / t }" );

                                }
                            }


                            if (lastYDirection != curYDirection || ySlowFrameCount > 0) {
                                //如果改变方向，为了防止抖动，则跳过一帧，但是把速度减小
                                long l = xSpeed.get();
                                ySpeed.set((long) (l / 10.0));
                                System.out.println(STR."y方向改变，减小速度");
                                if (ySlowFrameCount == 0) {
                                    ySlowFrameCount = 4;
                                }
                            } else {
                                if (Math.abs(yPixel) <= 6) {
                                    ySpeed.set(0);
                                } else {
                                    double t = needYSlow ? 40.0 : 20.0;

                                    if (needYSlow) {
                                        needYSlow = false;
                                    }
                                    ySpeed.set((long) ((yPixel / t) * 1000000.0)); //配速
                                    System.out.println(STR. "y设置配速\{ xPixel / t }" );
                                }
                            }


                        }
                        lastXDirection = curXDirection;
                        lastYDirection = curYDirection;
                        lastWidth = nearestPoint.width;
                        lastHeight = nearestPoint.height;
                    } else {
                        //差别过大，不重新定位了,
                        break;
                    }
                } else {
                    break;
                    //如果没识别到的时候，怎么移动
                    // MouseHelpWrapper.mouseMove.invokeExact(xMove, 0);
                }

                //记录本次是被人物的


                try {
                    if (i < 4) {
                        Thread.sleep(16);
                    } else {
                        if (ySlowFrameCount == 1) {
                            needYSlow = true;
                        }
                        if (xSlowFrameCount == 1) {
                            needXSlow = true;
                        }

                        if (xSlowFrameCount == 1) {
                            captureGameFrame();
                            captureGameFrame();
                        } else {
                            captureGameFrame();
                        }


                        if (xSlowFrameCount > 0) {
                            xSlowFrameCount--;
                        }
                        if (ySlowFrameCount > 0) {
                            ySlowFrameCount--;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            canContinue.set(false);
        }


        return new double[]{Math.abs(xSumMove), Math.abs(ySumMove)};
    }


    //todo 实现给定的平滑位移，

    /**
     * @param count  移动次数
     * @param xPixel 总的x移动距离
     * @param yPixel 总的y轴移动距离
     */
    public int[] xSmoothingMove(int xPixel, int yPixel, int count) {


        return null;
    }


    public Location findNearestPoint() {
        Location location = locations.minDistance(cx, cy);
        if (location != null) {
            return location.clone();
        }
        return null;
    }

    public double[] getNearestPointPixDistance() {
        Location location = locations.minDistance(cx, cy);
        double[] result = null;
        if (location != null) {
            result = new double[2];
            result[0] = location.centerX - cx;                                  //x轴方向
            result[1] = location.centerY - (location.height / 10.0) - cy;       //y轴方向
        }
        return result;
    }

    public void detectFrame(MemorySegment gpuFramePointer) throws Throwable {
        MemorySegment res = (MemorySegment) DectectWrapper.detect_inferenceGpuData.invokeExact(gpuFramePointer, Config.DETECT_WIDTH, Config.DETECT_HEIGHT, RGBA);

        //返回6*20个float  6个为一组  x,y,width,height,conf,classid
        MemorySegment segment = MemoryAccessHelper.asSegment(res, 6 * 20 * 4);
        float[] rects = segment.toArray(ValueLayout.JAVA_FLOAT);
        System.out.println(rects[0]);
        locations.update(screenX, screenY, rects, Time.getTime());
        //todo 这个segment是否需要释放？
    }


    /**
     * 获取游戏帧，返回gpu指针，这个方法会获取下一帧数据
     */
    public MemorySegment captureGameFrame() throws Throwable {
        MemorySegment captureRes;
        while (true) {
            captureRes = (MemorySegment) WinCaptureWrapper.game_capture_tick_gpu.invokeExact(dataAddress, 4.0f, screenX, screenY
                    , Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
            if (captureRes.address() == 0) {
                Time.sleep(1);
                continue;
            }
            break;
        }
        return captureRes;
    }

    /**
     * 识别一次
     */
    public Locations.CaptureRecord detectOnce() throws Throwable {
        try {
            int time = Time.getTime();
            MemorySegment gameFramePointer = captureGameFrame();
            detectFrame(gameFramePointer);
            return new Locations.CaptureRecord(gameFramePointer, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void release() {
        try {
            DectectWrapper.detecte_release.invoke();
            WinCaptureWrapper.stop_game_capture.invoke(dataAddress);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
