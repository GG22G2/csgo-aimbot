package org.example.csgo.thread;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import org.example.csgo.Config;
import org.example.csgo.Location;
import org.example.csgo.Locations;
import org.example.csgo.Tracks;
import org.example.csgo.wrapper.MouseHelpWrapper;

import static org.example.csgo.wrapper.MouseHelpWrapper.get_absolute_move;
import static org.example.csgo.wrapper.MouseHelpWrapper.mouse_move;

/**
 * @author 胡帅博
 * @date 2022/2/13 19:18
 */
public class MouseCorrectThread implements Runnable {
    //准星坐标
    final int cx = (int) (Config.SOURCE_WIDTH * Config.scale / 2);
    final int cy = (int) (Config.SOURCE_HEIGHT * Config.scale / 2);
    Locations locations;
    Tracks tracks;

    public MouseCorrectThread(Locations locations, Tracks tracks) {
        this.locations = locations;
        this.tracks = tracks;
    }


    public void onButtonPress(long ulButtons) {

        long lastCaptureTime = 0;

        Location location = null;

        double distance = 0;
        double xMin = 0, xMax = 0;
        int xPixel = 0, yPixel = 0;

        //判断需不需要修改鼠标位置
        long captureTime = locations.captureTime;
        //截图更新了
        if (captureTime > lastCaptureTime) {
            lastCaptureTime = captureTime;
        }

        if (Config.detect == Config.CT_AND_T) {
            location = locations.minDistance(cx, cy);
        } else {
            location = locations.minDistance(cx, cy, Config.detect);
        }
        if (location != null) {

            distance = Math.sqrt(location.width * location.width + location.height * location.height);
            xMin = (location.width / 2) * 0.8;
            xMax = distance * 3;
            xPixel = location.centerX - cx;
            yPixel = location.centerY - (location.height / 10 * 3)  - cy ;

            //根据相对移动距离，判断是否需要修正
            //当前镜头位置到目标的x和y轴距离
            double absX = Math.abs(xPixel);
            double absY = Math.abs(yPixel);

            int mouseXMove = (int) (absX * Config.xZoom);
            int mouseYMove = (int) (absY * Config.yZoom);


            //鼠标侧键按下
            if (ulButtons == 256) {
                zimiao((int) ((xPixel) * 1.40), (int) ((yPixel) * Config.yZoom));
            } else if (ulButtons == 1) {

                if (mouseXMove >= xMin && mouseXMove <= xMax && mouseYMove < 50) {
                    mouseXMove = (xPixel) < 0 ? -mouseXMove : mouseXMove;
                    mouseYMove = (yPixel) < 0 ? -mouseYMove : mouseYMove;

                    System.out.println("distance:" + (int) distance + ",移动:" + mouseXMove + "," + mouseYMove);
                    try {
                        simulateMove(mouseXMove, mouseYMove, xMin, xMax, 1, false);
                        // mouse_move.invokeExact((byte) x, (byte) y, (byte) 0);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }


    @Override
    public void run() {


        try {
            MouseHelpWrapper.listener_mouse_move.invokeExact();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        long lastHelp = 0;
        boolean close = false;
        while (true) {
            try {

                MemoryAddress result = (MemoryAddress) get_absolute_move.invokeExact();
                MemorySegment memorySegment = result.asSegment(4 * 3, ResourceScope.globalScope());

                //int x = MemoryAccess.getIntAtIndex(memorySegment, 0);
                //int y = MemoryAccess.getIntAtIndex(memorySegment, 1);
                long ulButtons = 0xFFFFFFFFFFFFFFFFL & MemoryAccess.getIntAtIndex(memorySegment, 2);

                if (ulButtons == 64) {
                    close = !close;
                    String msg = close ? "关闭" : "开启";
                    System.out.println(msg);
                }

                if (!close) {
                    if (ulButtons == 1 || ulButtons == 256) {
                        long curTime = System.currentTimeMillis();
                        //每一秒最多辅助一次
                        if ((curTime-lastHelp)>300){
                            onButtonPress(ulButtons);
                            lastHelp=curTime;
                        }

                    }
                }


            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }


    public void fuzhu() {


    }


    public void zimiao(int mouseXMove, int mouseYMove) {
        try {

            int x = 0, y = 0;
            for (int i = 0; i < 10; i++) {
                x += (byte) (mouseXMove * 0.1);
                y += (byte) (mouseYMove * 0.1);
                mouse_move.invokeExact((byte) (mouseXMove * 0.1), (byte) (mouseYMove * 0.1), (byte) 0);
                Thread.sleep(2);
            }
            mouse_move.invokeExact((byte) (mouseXMove - x), (byte) (mouseYMove - y), (byte) 0);
            Thread.sleep(2);

            //开枪
            mouse_move.invokeExact((byte) 0, (byte) 0, (byte) 1);
            Thread.sleep(500);
            //释放鼠标
            mouse_move.invokeExact((byte) 0, (byte) 0, (byte) 0);

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    /**
     *
     * 分析鼠标移动轨迹
     *
     * 54毫秒，移动了360像素，对应鼠标移动507，平均1毫秒移动9.3
     *
     * 
     * */
    public void simulateMove(int x, int y, double xMin, double xMax, double averSpeed, boolean simulate) {
        try {
            if (!simulate) {
                int mouseXMove=x, mouseYMove=y;
                x = 0;
                y = 0;
                for (int i = 0; i < 3; i++) {
                    x += (byte) (mouseXMove * 0.33);
                    y += (byte) (mouseYMove * 0.33);
                    mouse_move.invokeExact((byte) (mouseXMove * 0.33), (byte) (mouseYMove * 0.33), (byte) 0);
                    Thread.sleep(2);
                }
                mouse_move.invokeExact((byte) (mouseXMove - x), (byte) (mouseYMove - y), (byte) 0);

                return;
            }
            int sleep = 2;


            int signedX = x < 0 ? -1 : 1;
            int signedY = y < 0 ? -1 : 1;
            x = Math.abs(x);
            y = Math.abs(y);


            x = (int) (x - 6 * averSpeed);

            double useTime = x / averSpeed;
            double moveC = useTime / sleep;
            double st = x / moveC;

            double st2 = Math.ceil(st);
            sleep = (int) (sleep * (st2 / st));
            int moveCount = (int) (useTime / (sleep * (st2 / st)));
            int step = (int) st2;


            //假设这里是鼠标从人身上往外侧移动，
//            if ((x - xMin) < 6) {
//                int step = 2;
//                int len = (int) ((x - xMin) / step) + 1;
//                for (int i = 0; i < len; i++) {
//                    mouse_move.invokeExact((byte) (signedX * step), (byte) 0);
//                    Thread.sleep(sleep);
//                }
//                return;
//            }

            for (int i = 0; i < moveCount; i++) {
                mouse_move.invokeExact((byte) (signedX * (step)), (byte) (signedY * (0)), (byte) 0);
                System.out.println((byte) (signedX * (step)) + "," + (byte) (signedY * (0)));
                Thread.sleep(sleep);
            }
            // int lastMoveX = x - (step * moveCount);
            // int lastMoveY = y - (yseg * count + ry);
            //   mouse_move.invokeExact((byte) (signedX * lastMoveX), (byte) (signedY * 0));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
