package hsb.aimbot.csgo;

import hsb.aimbot.csgo.utils.Time;

/**
 * @author 胡帅博
 * @date 2023/9/19 15:27
 */
public class DetectHistory {
    public int time;
    public double xPixels;
    public double yPixels;

    public double speed;



    public void reset(int time){
        this.time=time;
        xPixels=0;
        yPixels=0;
        speed=0;
    }

}
