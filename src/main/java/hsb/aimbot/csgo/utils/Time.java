package hsb.aimbot.csgo.utils;

/**
 * @author 胡帅博
 * @date 2023/9/19 14:53
 */
public class Time {

    /**
     * 这里获取的是一个相对时间，用于和其他时间作比较，不需要太精确
     *
     * */
    public static int getTime(){
        return (int) (System.currentTimeMillis() & 0x7FFFFFF);
    }

    public static void sleep(long millis){
        try {
         Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
