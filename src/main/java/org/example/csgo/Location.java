package org.example.csgo;

/**
 * @author
 * @date 2022/2/3 17:39
 */
public class Location {

    public volatile int x, y, width, height;
    public volatile int classId;
    public volatile int centerX, centerY;
    public volatile float conf;
    public volatile long time;


    @Override
    public String toString() {
        return "Location{" +
                "classId=" + classId +
                ", centerX=" + centerX +
                ", centerY=" + centerY +
                ", conf=" + conf +
                '}';
    }
}
