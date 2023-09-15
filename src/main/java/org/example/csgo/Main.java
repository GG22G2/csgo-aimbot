package org.example.csgo;

import org.example.OpencvUtil;
import org.example.csgo.thread.CaptureThread;
import org.example.csgo.thread.DetectThread;
import org.example.csgo.thread.MouseListener;
import org.opencv.core.Core;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.csgo.wrapper.MouseHelpWrapper.mouseMove;

/**
 * @author
 * @date 2021/12/10 19:30
 */
public class Main {


    //--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-opens=jdk.incubator.foreign/jdk.internal.foreign=ALL-UNNAMED --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED -Djava.library.path=D:\kaifa_environment\opencv\opencv_all_build\java\opencv452;
    public static void main(String[] args) {
        try {
            OpencvUtil.init();
            ReentrantLock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            Locations locations = new Locations();
            new Thread(new CaptureThread(locations, lock, condition)).start();

            DetectThread detectThread = new DetectThread(locations, lock, condition);

            //等待截图初始化完成
            new Thread(detectThread).start();

            MouseListener mouseListener = new MouseListener(locations, new Tracks());
            detectThread.mouseListener = mouseListener;

            new Thread(mouseListener).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    public static void main22(String[] args) throws Throwable {
        Thread.sleep(2000);
        int x = 1636 * 4;
        //mouseMove.invoke(x, 0);
        // mouseMove.invoke(1636, 0);
        for (int i = 0; i < 16 * 4; i++) {
            mouseMove.invoke(102, 0);
            Thread.sleep(20);
        }
    }


}








