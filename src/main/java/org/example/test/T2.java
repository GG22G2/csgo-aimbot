package org.example.test;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 胡帅博
 * @date 2022/2/5 20:46
 */
public class T2 {
    public static void main(String[] args) throws AWTException {


//        for(int i = 0; i < 1000; i++) {
//            Point mousepoint = MouseInfo.getPointerInfo().getLocation();
//            System.out.println(mousepoint);
//            try {
//                Thread.sleep(1);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        System.out.println((-60) / 13);

        for(int i = 0; i < 100; i++) {

            System.out.println(ThreadLocalRandom.current().nextInt(2));
        }

    }
}
