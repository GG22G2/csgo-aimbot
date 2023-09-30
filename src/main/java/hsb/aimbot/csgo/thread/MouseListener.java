package hsb.aimbot.csgo.thread;


import hsb.aimbot.csgo.*;
import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static hsb.aimbot.csgo.Config.cx;
import static hsb.aimbot.csgo.Config.cy;

/**
 * @author 胡帅博
 * @date 2022/7/16 18:46
 */
public class MouseListener implements Runnable {

    //准星坐标

    Locations locations;
    Tracks tracks;

    HistoryRecord historyRecord = HistoryRecord.getInstance();

    public MouseListener(Locations locations, Tracks tracks) {
        this.locations = locations;
        this.tracks = tracks;
    }


    @Override
    public void run() {

        try {
            MouseHelpWrapper.listener_mouse_move.invokeExact();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        long lastHelp = 0;
        boolean mouseLeftDown = false;


        while (true) {
            try {
                boolean detecting = Config.detecting;
                MemorySegment result = (MemorySegment) MouseHelpWrapper.get_absolute_move.invokeExact();
                MemorySegment memorySegment = MemoryAccessHelper.asSegment(result, 24);
                long x = memorySegment.getAtIndex(ValueLayout.JAVA_INT, 0);
                long y = memorySegment.getAtIndex(ValueLayout.JAVA_INT, 1);

                long ulButtons = memorySegment.getAtIndex(ValueLayout.JAVA_INT, 2);
                long hdevice = memorySegment.getAtIndex(ValueLayout.JAVA_LONG, 2);

                if (RunTimeConfig.noAllowMouseController && hdevice != 0) {
                    //测速修正鼠标移动
                    MouseHelpWrapper.mouseMove.invokeExact((int)-x,(int)-y);
                }

                //  System.out.println(Time.getTime() +","+x+","+y+","+hdevice);

                if (ulButtons == 64) {
                    detecting = !detecting;
                    Config.detecting = detecting;
                    String msg = detecting ? "开启" : "关闭";
                    System.out.println(msg);
                }

                if (detecting) {
//                    if (ulButtons == 1 || ulButtons == 256) {
//
//
//                        Config.saveImg = true;
//                        Config.saveEndTime = System.currentTimeMillis() + 80;
////                        long curTime = System.currentTimeMillis();
////                        //每一秒最多辅助一次
////                        if ((curTime - lastHelp) > 300) {
////                            onButtonPress(ulButtons, null);
////                            lastHelp = curTime;
////                        }
//                    }
                    if (ulButtons == 1) {
                        //onButtonPress(ulButtons, null);
                        //  historyRecord.addShotRecord(System.currentTimeMillis() - 1);
                        // System.out.println("鼠标左键点击："+(System.currentTimeMillis() - 1));
                    } else if (ulButtons == 2) {
                        //    historyRecord.addShotRecord(System.currentTimeMillis() - 1);
                        // System.out.println("鼠标左键抬起: "+(System.currentTimeMillis() - 1));
                    }


                }


            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    //跟踪最近的人
    public boolean track() {
        Location location = locations.minDistance(cx, cy);
        if (location != null) {
            double xPixel = location.centerX - cx;
            double yPixel = location.centerY - (location.height / 10.0) - cy;
            try {
                //mouseMove.invokeExact((int) ((xPixel) * 1.10), (int) ((yPixel) * 1.2));
                MouseHelpWrapper.mouseMove.invokeExact((int) ((xPixel) * 0.20), (int) ((yPixel) * 0.12));
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
        return false;
    }


    public void onButtonPress(long ulButtons, Detection detection) {

        Location location = null;
        double distance = 0;

        if (detection != null) {
            //    detection.detect();
        }

        if (Config.detect == Config.CT_AND_T) {
            location = locations.minDistance(cx, cy);
        } else {
            location = locations.minDistance(cx, cy, Config.detect);
        }
        if (location != null) {
            distance = Math.sqrt(location.width * location.width + location.height * location.height);
            double xMin = (location.width / 2.0) * 0.7;
            double xMax = distance * 2;
            double xPixel = location.centerX - cx;
            double yPixel = location.centerY - (location.height / 10.0) - cy;

            //根据相对移动距离，判断是否需要修正
            //当前镜头位置到目标的x和y轴距离
//            double absX = Math.abs(xPixel);
//            double absY = Math.abs(yPixel);
            //   if (Math.abs(xPixel) > xMin && Math.abs(xPixel) < xMax) {
            if (Math.abs(xPixel) > 3 && Math.abs(xPixel) < 60) {
                //鼠标侧键按下
                if (ulButtons == 256 || ulButtons == 1) {
                    try {
                        MouseHelpWrapper.mouseMove.invokeExact((int) ((xPixel) * 1.10), (int) ((yPixel) * 1.2));
                        //  mouseMove.invokeExact((int) ((xPixel) * 1.0), (int) ((yPixel) * 0.2));
                        // Thread.sleep(1000);
                        // aimBot((int) ((xPixel* 1.10)), (int) ((yPixel)));
                    } catch (Throwable e) {

                    }

                }
            }

        }

    }


    public static void aimBot(int mouseXMove, int mouseYMove) {

        //System.out.println(mouseXMove + "," + mouseYMove);
        try {
            int x = 0, y = 0;
            for (int i = 0; i < 3; i++) {
                x += (byte) (mouseXMove * 0.33);
                y += (byte) (mouseYMove * 0.33);
                MouseHelpWrapper.mouseMove.invokeExact((int) (mouseXMove * 0.33), (int) (mouseYMove * 0.33));
                Thread.sleep(1);
            }
            MouseHelpWrapper.mouseMove.invokeExact((mouseXMove - x), (mouseYMove - y));

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
