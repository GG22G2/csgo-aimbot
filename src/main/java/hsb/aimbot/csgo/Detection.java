package hsb.aimbot.csgo;


import hsb.aimbot.OpencvUtil;
import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import hsb.aimbot.csgo.utils.Time;
import hsb.aimbot.csgo.wrapper.DectectWrapper;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;
import hsb.aimbot.csgo.wrapper.WinCaptureWrapper;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

import static hsb.aimbot.csgo.Config.*;
import static hsb.aimbot.csgo.wrapper.MouseHelpWrapper.moveDegree;

/**
 * @author 胡帅博
 * @date 2022/7/16 18:38
 * 对csgo的一些参数做测量
 *
 */
public class Detection {

    /**
     * 分析识别到的图片中任务距离屏幕中心点的像素值p 和 调用mousemove需要设置的移动量x之间的对应系数t的比值关系
     * <p>
     * p 越小 t越小
     * p总体上在1.2-1.6网上
     */
    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();
        //     moveDegree(20, xMoveDegree);

        Locations locations = new Locations();
        Detection detection = new Detection(locations);

        for(int i = 71; i <= 75; i++) {
            detection.pixelWidthDistanceEstimate(i);
        }

        //   detection.detectOnce();
        //  int width = detection.findNearestPoint().width;
        //   System.out.println(width);

        //todo 我感觉根据fov 识别到物体的宽高，
        //  detection.distanceEstimate();
        //     detection.moveConvertRadio();
//        try {
//            detection.detectOnce();
//
//            // detection.positionCenter();
////            Thread.sleep(1000);
////            for (int i = 0; i < 100; i++) {
////                Thread.sleep(100);
////                detection.measureMoveDelay();
////            }
//
//            double[] nearestPointPixDistance = detection.getNearestPointPixDistance();
//            double xt = nearestPointPixDistance[0];
//            double yt = nearestPointPixDistance[1];
//
//            int xSumMove = 0;
//            int ySumMove = 0;
//                try {
//                    Thread.sleep(64);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            System.out.println("累计移动x:" + xSumMove + ",y:" + ySumMove);
//            double xp = (Math.abs(xSumMove) / Math.abs(xt));
//            double yp = (Math.abs(ySumMove) / Math.abs(yt));
//            System.out.println("像素距离和移动x的比例x:" + xp + ",y:" + yp);
//
//
//            MouseHelpWrapper.mouseMove.invokeExact((int) (-xt * xp), (int) (-yt * yp));
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        detection.release();
    }


    Locations locations;
    MemorySegment dataAddress;

    public Detection(Locations locations) {
        this.locations = locations;
        init();
    }


    public void init() {
        try {
            dataAddress = WinCaptureWrapper.initCapture();
            DectectWrapper.initDetectEngine();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            DectectWrapper.detecte_release.invoke();
            WinCaptureWrapper.stop_game_capture.invoke(dataAddress);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void pixelWidthDistanceEstimate(int width) throws Throwable {
        Robot robot = new Robot();
        positionCenter();
        moveAdjustWidth(robot, width);
        int xPixel = 0;
        //864/2 = 432
        int segmentLength = 10;
        int end = segmentLength;
        int index = 0;
        int count = 0;
        double segmentTotal = 0;
        double[] result = new double[21];
        for (int i = 1; i < 800 && xPixel < 200 ; i += 1) {
            double[] moveXY = positionCenter();
            Thread.sleep(50);
            MouseHelpWrapper.mouseMove.invoke(i, 0);
            Thread.sleep(50);
            detectOnce();
            double[] nearestPointPixDistance = getNearestPointPixDistance();
            if (nearestPointPixDistance == null) {
                continue;
            }
            xPixel = (int) Math.abs(nearestPointPixDistance[0]);
           // System.out.println(i + "位移对应像素:" + xPixel);
            if (xPixel==0){
                continue;
            }
            if ( xPixel < end) {
                segmentTotal += ((double) i / xPixel);
                count++;
            } else {
                result[index++] = segmentTotal / count;
                end = end + segmentLength;
                segmentTotal = ((double) i / xPixel);
                count = 1;
            }
        }
        result[index] = segmentTotal / count;

//40:[1.5118834903609337, 1.459710145186327, 1.4496563419915314, 1.4487747083690334, 1.4472370970367376, 1.4417152279397718, 1.4360804882866236, 1.4307704514959658, 1.422238684722989
//40:[1.5505494505494504, 1.4698090716905199, 1.4561446129092102, 1.4512035518768607, 1.454685120241009, 1.4475791805876657, 1.4423685365559473, 1.4400868593442149, 1.4338451466385336, 1.4307565377364317, 1.4221676862289958, 1.415421117544071, 1.4112079249175522, 1.4017166034590103, 1.3912707516007472, 1.3837324423818382, 1.3728545637713607, 1.3615454938384919, 1.3530740451929946, 1.341373802514312, 0.0]
//30:[1.403988504233602, 1.44245671696754, 1.4350461537491772, 1.4430119268366484, 1.4429368478267084, 1.4363854987890106, 1.4325103765430915, 1.4255813767060121, 1.4189711790351391, 1.4141863179055478, 1.4091409315107617, 1.4020252331281857, 1.393257890104136, 1.3829930248824247, 1.37656134167025, 1.3667383371619801, 1.3569914025748457, 1.3477832075559906, 1.337580768069241, 1.3280365710258792, 0.0]

        System.out.println(width+":" + Arrays.toString(result));

//        for(int i = 0; i < ; i++) {
//
//        }


    }

    private void moveAdjustWidth(Robot robot, int width) throws Throwable {
        while (true) {
            detectOnce();
            Location nearestPoint = findNearestPoint();
            if (nearestPoint == null) {
                moveDegree(1, scale);
                Thread.sleep(16);
                continue;
            }
            double nearestPointPixDistance = nearestPoint.width;
            //System.out.println(nearestPointPixDistance);
            if (nearestPointPixDistance >= width) {
                if (nearestPointPixDistance - width > 0) {
                    robot.keyPress(KeyEvent.VK_S);
                    Thread.sleep(1);
                    robot.keyRelease(KeyEvent.VK_S);
                } else {
                    break;
                }
            }

            if (nearestPointPixDistance < width) {
                if (width - nearestPointPixDistance > 0) {
                    robot.keyPress(KeyEvent.VK_W);
                    Thread.sleep(1);
                    robot.keyRelease(KeyEvent.VK_W);
                } else {
                    break;
                }
            }
            Thread.sleep(7);
        }
    }


    //把准心定位到人身上
    public double[] positionCenter() {
        int xSumMove = 0;
        int ySumMove = 0;
        try {

            for (int i = 0; i < 6; i++) {
                detectOnce();
                double[] nearestPointPixDistance = getNearestPointPixDistance();

                if (nearestPointPixDistance != null) {
                    double xPixel = nearestPointPixDistance[0];
                    double yPixel = nearestPointPixDistance[1];
                    xSumMove += xPixel;
                    ySumMove += yPixel;
                    MouseHelpWrapper.mouseMove.invokeExact((int) xPixel, (int) yPixel);
                } else {
                    return null;
                }
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new double[]{Math.abs(xSumMove), Math.abs(ySumMove)};
    }

    /**
     * 认为框的对角线和人到目标框的距离 ，这两个参数成线性比例
     */
    public void distanceEstimate() throws Throwable {
        Thread.sleep(1000);
        positionCenter();
        Thread.sleep(100);

        double degree = 20;
        //移动10度
        int x = (int) (degree / xMoveDegree);
        MouseHelpWrapper.mouseMove.invoke(x, 0);
        Thread.sleep(50);

        detectOnce();

        Location nearestPoint = findNearestPoint();
        int width = nearestPoint.width;
        int height = nearestPoint.height;
        double distanceX = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)); //适用框的对角线做视角到敌人的距离系数

        double v = Math.abs(getNearestPointPixDistance()[0]);

        double theta = Math.toRadians(degree);

        double distance = v / Math.sin(theta);

        System.out.println(width + "," + height + "," + (width * 1.0 / height) + "," + distanceX);
        System.out.println("像素距离：" + v);
        System.out.println("测算的距离：" + distance);
        System.out.println("比例系数(对角线)：" + (distance / distanceX) + ",比例系数(高)：" + (distance / height));


    }


    /**
     * 计算鼠标原始输入对应的游戏中的转动角度，
     * 通过原始输入方式，水平方向移动一距离，对应的游戏中视角转动的角度 360.0/6543.0 = 0.0550206
     */
    public void moveConvertRadio() throws Throwable {
        Thread.sleep(1000);
        //6543是我现在csgo的默认配置   csgo的fov是90
        positionCenter();
        Thread.sleep(100);
        //第一次测试的时候可以把这个
        int sumMove = 6000;

        MouseHelpWrapper.mouseMove.invoke(6000, 0);
        Thread.sleep(20);
        while (true) {
            MouseHelpWrapper.mouseMove.invoke(3, 0);
            Thread.sleep(20);
            detectOnce();
            double[] nearestPointPixDistance = getNearestPointPixDistance();
            double v = 1000;
            if (nearestPointPixDistance != null) {
                v = nearestPointPixDistance[0];
            }
            System.out.println(v);
            sumMove += 3;
            if (Math.abs(v) < 3) {
                break;
            }

        }
        System.out.println("总水平转动距离" + sumMove);
        System.out.println("90度对应" + (sumMove / 4.0));
        System.out.println("移动1距离对应的角度" + (360.0 / sumMove));
    }


    /**
     * 测量一个移动操作发送后，到画面显示需要多久
     */
    public double measureMoveDelay() {
        try {
            int skipFrame = 0;
            MemorySegment gameFramePointer = getGameFramePointer();
            detectFrame(gameFramePointer);
            double[] p1 = getNearestPointPixDistance();
            double[] p2 = null;
            double[] p3 = null;
            double delay1, delay2;
            long time2 = 0;
            long time3 = 0;
            long time1 = System.nanoTime();
            MouseHelpWrapper.mouseMove.invokeExact(200, 0);
            Thread.sleep(6);
            while (true) {
                time2 = System.nanoTime();
                detectOnce();
                skipFrame++;
                p2 = getNearestPointPixDistance();
                if (Math.abs(p1[0] - p2[0]) > 130) {
                    delay1 = time2 - time1;
                    // System.out.println((time2 - time1) / 1000000.0);
                    break;
                }
            }
            Thread.sleep(16);
            time2 = System.nanoTime();
            MouseHelpWrapper.mouseMove.invokeExact(-200, 0);
            Thread.sleep(6);
            while (true) {
                time3 = System.nanoTime();
                detectOnce();
                skipFrame++;
                p3 = getNearestPointPixDistance();
                if (Math.abs(p2[0] - p3[0]) > 130) {
                    delay2 = time3 - time2;
                    //  System.out.println((time3 - time2) / 1000000.0);
                    break;
                }
            }
            //todo 整体测试下来，一个移动指令的响应大概是下一帧或者下下帧就，
            //todo 也就是说每次移动后，只需要忽略一帧，第二帧的画面肯定有操作的接过来
            System.out.println("经过帧:" + (skipFrame / 2.0) + ",延迟" + ((delay1 + delay2) / 2000000.0));
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }


        return 0;
    }


    private void detectOnce() throws Throwable {
        try {
            //long startNanos_3_192 = System.nanoTime();
            MemorySegment gameFramePointer = getGameFramePointer();
            detectFrame(gameFramePointer);
            //long endNanos_3_195 = System.nanoTime();
            //System.out.println((endNanos_3_195 - startNanos_3_192) / 1000000.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double[] getNearestWidthHeight() {
        Location location = locations.minDistance(cx, cy);
        double[] result = null;
        if (location != null) {
            result = new double[2];
            result[0] = location.width;                                  //x轴方向
            result[1] = location.height;       //y轴方向
        }
        return result;
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

    public double[] getPixDistance(Location location) {
        double[] result = new double[2];
        result[0] = location.centerX - cx;                                  //x轴方向
        result[1] = location.centerY - (location.height / 10.0) - cy;       //y轴方向
        return result;
    }

    public Location findNearestPoint() {
        Location location = locations.minDistance(cx, cy);
        if (location != null) {
            return location.clone();
        }
        return null;
    }


    public MemorySegment getGameFramePointer() throws Throwable {
        MemorySegment captureRes;
        while (true) {
            captureRes = (MemorySegment) WinCaptureWrapper.game_capture_tick_gpu.invokeExact(dataAddress, 4.0f, screenX, screenY
                    , Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
            if (captureRes.address() == 0) {
                continue;
            }
            break;
        }
        return captureRes;
    }

    public void detectFrame(MemorySegment gpuFramePointer) throws Throwable {
        MemorySegment res = (MemorySegment) DectectWrapper.detect_inferenceGpuData.invokeExact(gpuFramePointer, Config.DETECT_WIDTH, Config.DETECT_HEIGHT, RGBA);

        //返回6*20个float  6个为一组  x,y,width,height,conf,classid
        float[] rects = MemoryAccessHelper.asSegment(res, 6 * 20 * 4).toArray(ValueLayout.JAVA_FLOAT);

        locations.update(screenX, screenY, rects, Time.getTime());


    }


}
