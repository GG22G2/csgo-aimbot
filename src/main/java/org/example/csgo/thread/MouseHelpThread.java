//package org.example.csgo.thread;
//
//import org.example.csgo.Config;
//import org.example.csgo.Location;
//import org.example.csgo.Locations;
//import org.example.csgo.Tracks;
//import sun.awt.ComponentFactory;
//
//import java.awt.*;
//import java.awt.peer.MouseInfoPeer;
//import java.util.Arrays;
//import java.util.Random;
//import java.util.concurrent.ThreadLocalRandom;
//
//import static org.example.csgo.wrapper.MouseHelpWrapper.mouseMove;
//
///**
// * @author 胡帅博
// * @date 2022/2/12 17:05
// * <p>
// * 帮助调整鼠标位置的线程
// */
//public class MouseHelpThread implements Runnable {
//    Locations locations;
//
//    //准星坐标
//    final double cx = Config.SOURCE_WIDTH * Config.scale / 2;
//    final double cy = Config.SOURCE_HEIGHT * Config.scale / 2;
//
//
//    Random random = ThreadLocalRandom.current();
//
//    Toolkit toolkit = Toolkit.getDefaultToolkit();
//    Point point = new Point(0, 0);
//    MouseInfoPeer mouseInfoPeer = ((ComponentFactory) toolkit).getMouseInfoPeer();
//    Robot robot;
//    Tracks track;
//
//    public MouseHelpThread(Locations locations, Robot robot, Tracks track) {
//        this.locations = locations;
//        this.robot = robot;
//        this.track = track;
//    }
//
//    @Override
//    public void run() {
//        try {
//
//            while (true) {
//                mouseInfoPeer.fillPointWithCoords(point);
//                double x = point.getX() / Config.scale;
//                double y = point.getY() / Config.scale;
//                double xOffset = Math.abs(cx - x);
//                double yOffset = Math.abs(cy - y);
//
//                if (locations.isNewData()) {
//                    Location location = locations.minDistance((int) cx, (int) cy);
//                    if (location != null) {
//
//                        double distance = Math.sqrt(location.width * location.width + location.height * location.height);
//                        double xMove = location.centerX - cx;
//                        double yMove = location.centerY - cy + location.height / 3;
//
//                        double absX = Math.abs(xMove);
//                        double absY = Math.abs(yMove);
//
//                        /**
//                         *  190   10 ， 50
//                         *  100
//                         * */
//
//                        double xMin = location.width / 2 + 2, xMax = distance * 0.65;
//                        double yMin = 20, yMax = distance * 0.7;
//
//                        int mouseXMove = (int) (absX * Config.xZoom) + 1;
//                        int mouseYMove = (int) (absY * Config.yZoom) + 1;
//
//                        // distance * 0.2
//                        if (mouseXMove >= xMin && mouseXMove <= xMax && mouseYMove < 30) {
//                            long curTime = System.currentTimeMillis();
//                            mouseXMove = xMove < 0 ? -mouseXMove : mouseXMove;
//                            if (mouseYMove < 4) {
//                                mouseYMove = 0;
//                            }
//                            mouseYMove = yMove < 0 ? -mouseYMove : mouseYMove;
//                           // System.out.println("distance:" + (int) distance + ", conf:" + location.conf + ",移动:" + mouseXMove + "," + mouseYMove);
//                            simulateMove(mouseXMove, mouseYMove, xMin, xMax, false);
//                            synchronized (locations) {
//                                //鼠标移动后，这里应该等待游戏更新画面，可以让LocationThread线程跳过2帧的时间
//                                locations.ingore(2);
//                                locations.clear();
//                            }
//                            Thread.sleep(10);
//                        }
//
//                    }
//                }
//                Thread.sleep(1);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    class TrackResult {
//
//        boolean sameDirect = false;
//        boolean xSameDirect = true;
//        boolean ySameDirect = true;
//        double xAverageMove;
//        double yAverageMove;
//
//        //这四个点的整体速度变换趋势，增大还是减小
//        int[] xIncreaseTrend;
//        int[] yIncreaseTrend;
//
//        int xMax, xMin;
//        int yMax, yMin;
//
//        @Override
//        public String toString() {
//            return "TrackResult{" +
//                    "sameDirect=" + sameDirect +
//                    ", xSameDirect=" + xSameDirect +
//                    ", ySameDirect=" + ySameDirect +
//                    ", xAverageMove=" + xAverageMove +
//                    ", yAverageMove=" + yAverageMove +
//                    ", xIncreaseTrend=" + Arrays.toString(xIncreaseTrend) +
//                    ", yIncreaseTrend=" + Arrays.toString(yIncreaseTrend) +
//                    ", xMax=" + xMax +
//                    ", xMin=" + xMin +
//                    ", yMax=" + yMax +
//                    ", yMin=" + yMin +
//                    '}';
//        }
//    }
//
//    //分析轨迹 ，主要是x轴方向
//    public TrackResult analysisTrack(int[] xyTrack) {
//        TrackResult trackResult = new TrackResult();
//        final int xZ = 0x80000000;
//        final int yZ = 0x80000000;
//
//        //从第一个点获取方向
//        int xDirect = xyTrack[0] & 0x80000000 | 0x7FFFFFFF;
//        int yDirect = xyTrack[1] & 0x80000000 | 0x7FFFFFFF;
//        int[] xIncreaseTrend = new int[xyTrack.length / 2 - 1];
//        int[] yIncreaseTrend = new int[xyTrack.length / 2 - 1];
//        int xSum = 0;
//        int ySum = 0;
//        int xMax = 0, xMin = Integer.MAX_VALUE;
//        int yMax = 0, yMin = Integer.MAX_VALUE;
//        for (int i = 2; i < xyTrack.length; i+=2) {
//            int x = xyTrack[i];
//            int y = xyTrack[i + 1];
//            if ((xDirect & x) != x) {
//                //方向不同
//                trackResult.xSameDirect = false;
//            }
//            if ((yDirect & y) != y) {
//                //方向不同
//                trackResult.ySameDirect = false;
//            }
//
//            if (Math.abs(xMax) < Math.abs(x)) {
//                xMax = x;
//            }
//            if (Math.abs(yMax) < Math.abs(y)) {
//                yMax = y;
//            }
//            if (Math.abs(xMin) > Math.abs(x)) {
//                xMin = x;
//            }
//            if (Math.abs(yMin) > Math.abs(y)) {
//                yMin = y;
//            }
//            int j = (i >> 1) - 1;
//            xIncreaseTrend[j] = x - xyTrack[i - 2];
//            yIncreaseTrend[j] = y - xyTrack[i - 1];
//
//            xSum += x;
//            ySum += y;
//        }
//        trackResult.xMax = xMax;
//        trackResult.yMax = yMax;
//        trackResult.yMin = yMin;
//        trackResult.xMin = xMin;
//
//        trackResult.xAverageMove = xSum * 1.0 / (xyTrack.length / 2);
//        trackResult.yAverageMove = ySum * 1.0 / (xyTrack.length / 2);
//
//        trackResult.xIncreaseTrend=xIncreaseTrend;
//        trackResult.yIncreaseTrend=yIncreaseTrend;
//
//        if (trackResult.xSameDirect && trackResult.ySameDirect) {
//            trackResult.sameDirect = true;
//        }
//
//        return trackResult;
//    }
//
//
//    public void simulateMove(int x, int y, double xMin, double xMax, boolean simulate) {
//        TrackResult trackResult = analysisTrack(track.getTrack());
//        System.out.println(trackResult);
//        try {
//            if (simulate) {
//                mouseMove.invoke(x, y);
//                return;
//            }
//
//            int oneLimit = 20;
//
//            int signedX = x < 0 ? -1 : 1;
//            int signedY = y < 0 ? -1 : 1;
//            x = Math.abs(x);
//            y = Math.abs(y);
//            int MaxError = random.nextInt((int) (xMin * 0.3));
//
//            //假设这里是鼠标从人身上往外侧移动，
//            if ((x - xMin) < 6) {
//
//                int step = 2;
//                int len = (int) ((x - xMin) / step) + 1;
//                for (int i = 0; i < len; i++) {
//                    mouseMove.invoke(signedX * step, 0);
//                    Thread.sleep(5);
//                }
//
//                return;
//            }
//
//            if (Math.abs(x) <= oneLimit) {
//                mouseMove.invoke(signedX * (x - MaxError), signedY * y);
//                return;
//            }
//
//            int rx = 0;
//            int ry = 0;
//            //一毫秒最多移动60像素
//
//            int count = x / oneLimit;
//            int yseg = y / count;
//
//            for (int i = 0; i < count; i++) {
//                int randomX = random.nextInt(17) - 8;
//                int randomY = random.nextInt(5) - 2;
//                mouseMove.invoke(signedX * (oneLimit + randomX), signedY * (yseg + randomY));
//                rx += randomX;
//                ry += randomY;
//                Thread.sleep(4);
//            }
//            int lastMoveX = x - (oneLimit * count + rx - MaxError);
//            int lastMoveY = y - (yseg * count + ry);
//            mouseMove.invoke(signedX * lastMoveX, signedY * lastMoveY);
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//}
