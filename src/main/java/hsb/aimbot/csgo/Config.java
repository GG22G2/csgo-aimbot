package hsb.aimbot.csgo;

/**
 * @author
 * @date 2022/2/12 17:09
 */
public class Config {

    //像素格式
    final static public int RGB = 1;
    final static public int RGBA = 2;
    final static public int BGR = 3;
    final static public int BGRA = 4;


    //屏幕缩放， 对应window10中 设置->系统->显示->缩放与布局, 因为我设置的125% ，所以scale是0.8
    final static public double scale = 1;


    //警匪的分类对应的值
    //警
    final static public int CT = 0;
    //匪
    final static public int T = 1;

    final static public int CT_AND_T = -1;

    final static public int detect = CT_AND_T;


    //截图+识别速度控制   每次截图加识别用时小于CAPTURE_and_DETECT_TIME时，sleep(CAPTURE_and_DETECT_TIME-usetime)
    final static public int CAPTURE_AND_DETECT_TIME = 1;

    //游戏中视角转360°，对应鼠标水平方向移动距离 (不同鼠标灵敏度对应不同的值)
    final static public int xDegrees = 1636 * 4;

    //通过原始输入方式，水平方向移动一距离，对应的游戏中视角转动的角度 360.0/6543.0 = 0.0550206
    final static public double xMoveDegree = 360.0/6543.0;

    /**
     * 水平方向 像素距离到对象的鼠标移动距离   鼠标移动距离 = 图片中两点像素差 * xZoom
     * 根据我的配置，在csgo中测试鼠标水平移动距离，对应游戏中转动的像素数
     * 鼠标移动  屏幕移动
     * 100      70
     * 200      141    71
     * 300      214    73
     * 400      290    76
     * 500      371    81
     * 600      458    87
     * 700      563    105
     * 800      690    127
     * 900      841    151
     * <p>
     * 像素小于200时，1.41是一个合理的比值
     */
    final static public double xZoom = 1.41;
    //垂直方向  这个值是大概猜测，不准确
    final static public double yZoom = 1.4;

    //分辨率
    final static public int SOURCE_WIDTH = 1920;
    final static public int SOURCE_HEIGHT = 1080;

    //用于传递给yolov5识别的图像的宽和高
    final static public int DETECT_WIDTH = 864;
    final static public int DETECT_HEIGHT = 416;

    public final static int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
    public final static int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);


    public static final int cx = (int) (Config.SOURCE_WIDTH * Config.scale / 2);
    public static final int cy = (int) (Config.SOURCE_HEIGHT * Config.scale / 2);

//    final static public int DETECT_WIDTH = 640;
//    final static public int DETECT_HEIGHT = 640;



    //各个dll的加载路径
    final static public String CAPTURE_DLL_PATH = "G:\\kaifa_environment\\code\\C\\screenShot\\x64\\Release\\screenShot.dll";
    final static public String YOLOV5_DETECT_DLL_PATH = "G:\\kaifa_environment\\code\\clion\\csgo-util\\cmake-build-release\\game-capture\\yolov5_det_dll.dll";

    final static public String OBS_WIN_CAPTURE_DLL_PATH = "G:\\kaifa_environment\\code\\clion\\csgo-util\\cmake-build-release\\game-capture\\bebo-capturedll.dll";

    final static public String MOUSE_HELP_DLL_PATH = "G:\\kaifa_environment\\code\\C\\mouseHelp\\x64\\Release\\mouseHelp.dll";


    final static public String YOLOV5_MODEL_PATH = "G:\\kaifa_environment\\code\\clion\\csgo-util\\cmake-build-release\\game-capture\\csgo.engine";
    //final static public String YOLOV5_MODEL_PATH = "G:\\kaifa_environment\\code\\C\\csgo-location\\x64\\Release\\yolov5n_apex.engine";
   // final static public String YOLOV5_MODEL_PATH = "G:\\kaifa_environment\\code\\C\\csgo-location\\x64\\Release\\overwatch_yolov5n.engine";


    static volatile public boolean  detecting = true;

    static volatile public boolean saveImg = false;

    static volatile public long saveEndTime = 0;
}
