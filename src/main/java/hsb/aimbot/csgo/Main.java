package hsb.aimbot.csgo;

import hsb.aimbot.OpencvUtil;
import hsb.aimbot.csgo.thread.DetectThread;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;
import hsb.aimbot.csgo.thread.CaptureThread;
import hsb.aimbot.csgo.thread.MouseListener;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author
 * @date 2021/12/10 19:30
 */
public class Main {

    /**
     * --add-opens=java.desktop/java.awt.peer=csgo.aimbot
     * --add-opens=java.desktop/sun.awt=csgo.aimbot
     * --enable-native-access=csgo.aimbot
     * */

    //--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-opens=jdk.incubator.foreign/jdk.internal.foreign=ALL-UNNAMED --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED -Djava.library.path=D:\kaifa_environment\opencv\opencv_all_build\java\opencv452;
    public static void main(String[] args) {
        try {
            OpencvUtil.init();
            Locations locations = new Locations();
            CSGODetect detect = new CSGODetect(locations);
            detect.init();
            ReentrantLock lock = new ReentrantLock();
            Condition condition = lock.newCondition();


            //Thread.startVirtualThread(new CaptureThread(detect,locations, lock, condition));
           // Thread.startVirtualThread(new DetectThread(detect,locations, lock, condition));
            new Thread(new DetectThread(detect,locations, lock, condition)).start();

            MouseListener mouseListener = new MouseListener(locations, new Tracks());


            new Thread(mouseListener).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    /**
     * 用于调整水平方向鼠标速度
     * */
    public static void main22(String[] args) throws Throwable {
        Thread.sleep(2000);
        int x = 1636 * 4;
        //mouseMove.invoke(x, 0);
        // mouseMove.invoke(1636, 0);
        for (int i = 0; i < 16 * 4; i++) {
            MouseHelpWrapper.mouseMove.invoke(102, 0);
            Thread.sleep(20);
        }
    }


}








