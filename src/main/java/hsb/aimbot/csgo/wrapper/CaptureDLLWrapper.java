package hsb.aimbot.csgo.wrapper;


import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.utils.DllFunctionFindHelper;

import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;


/**
 * @author
 * @date 2022/2/3 17:15
 */
public class CaptureDLLWrapper {

    static {
        loadDLL();

        capture_init = DllFunctionFindHelper.getFuncOf("capture_init", ValueLayout.JAVA_INT);

        capture_release = DllFunctionFindHelper.getFuncOfVoid("capture_release");

        capture_capture = DllFunctionFindHelper.getFuncOf("capture", ValueLayout.ADDRESS);
    }

    private static void loadDLL(){
        System.load(Config.CAPTURE_DLL_PATH);
    }


    public static MethodHandle capture_init;
    public static MethodHandle capture_release;
    public static MethodHandle capture_capture;

}
