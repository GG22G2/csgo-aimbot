package hsb.aimbot.csgo;

/**
 * @author
 * @date 2022/2/3 17:39
 */
public class Location implements Cloneable{

    public  int x, y, width, height;
    public  int classId;
    public  int centerX, centerY;
    public  float conf;
    public  long time;


    @Override
    protected Location clone() {
        try {
            return (Location)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

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
