package hsb.aimbot.csgo.wrapper;

import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.utils.DllFunctionFindHelper;

import java.lang.invoke.MethodHandle;

import static hsb.aimbot.csgo.Config.xMoveDegree;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * @author
 * @date 2022/2/12 21:44
 */
public class MouseHelpWrapper {
    static {
        loadDLL();

        mouseMove = DllFunctionFindHelper.getFuncOfVoid("mouseMove", JAVA_INT, JAVA_INT);
        mouseLeftPress = DllFunctionFindHelper.getFuncOfVoid("mouseLeftPress");
        mouseLeftRelease = DllFunctionFindHelper.getFuncOfVoid("mouseLeftRelease");
        listener_mouse_move = DllFunctionFindHelper.getFuncOfVoid("listenerMouseMove");

        get_absolute_move = DllFunctionFindHelper.getFuncOf("getAbsoluteMove", ADDRESS);

    }

    private static void loadDLL() {
        System.load(Config.MOUSE_HELP_DLL_PATH);
    }

    public static MethodHandle mouseMove;
    public static MethodHandle mouseLeftPress;
    public static MethodHandle mouseLeftRelease;
    public static MethodHandle listener_mouse_move;
    public static MethodHandle get_absolute_move;

    public static void moveDegree(double degree,double scale) throws Throwable {
        int x = (int) (degree / scale);
        MouseHelpWrapper.mouseMove.invoke(x, 0);
    }
}
