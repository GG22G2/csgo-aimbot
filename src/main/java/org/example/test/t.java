package org.example.test;

import sun.awt.ComponentFactory;

import java.awt.*;
import java.awt.peer.MouseInfoPeer;

/**
 * @author 胡帅博
 * @date 2022/2/3 18:08
 */
public class t {
    public static void main(String[] args) throws AWTException {
        //  Point location = MouseInfo.getPointerInfo().getLocation();
        // System.out.println(location);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point point = new Point(0, 0);
        int deviceNum = 0;
        MouseInfoPeer mouseInfoPeer = ((ComponentFactory) toolkit).getMouseInfoPeer();
        Robot robot = new Robot();
        double cx = 960;
        double cy =553.75;
        for (int i = 0; i < 0; i++) {
            try {
                Thread.sleep(2);
                // Point location = MouseInfo.getPointerInfo().getLocation();
                mouseInfoPeer.fillPointWithCoords(point);
                double x = point.getX() * 1.25;
                double y = point.getY() * 1.25;
                if (x == cx && y == cy) {

                } else
                {
                    double xOffset = Math.abs(cx - x);
                    double yOffset = Math.abs(cy - y);
                    System.out.println("[x=" + x + ",y=" + y + "],x="+xOffset+",y="+yOffset);
                   // System.out.println("x="+xOffset+",y="+yOffset+","+System.currentTimeMillis());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //x轴 154偏移为360度
        // y轴  20偏移为90度
        int xf = 0;
        for(int i = 0; i < 1; i++) {
            try {
                //robot.mouseMove(154  ,0);
                //robot.mouseMove(154/2  ,0);
                robot.mouseMove(0  ,20);
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
