package hsb.aimbot.csgo.wrapper;


import hsb.aimbot.OpencvUtil;
import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.utils.DllFunctionFindHelper;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * @author
 * @date 2022/2/3 17:17
 */
public class DectectWrapper {
    static {
        loadDLL();

        detecte_init = DllFunctionFindHelper.getFuncOf("detect_init", JAVA_INT, ADDRESS);

        detecte_inference = DllFunctionFindHelper.getFuncOf("detect_inference",ADDRESS,ADDRESS, JAVA_INT, JAVA_INT);

        //最后一个参数 1 代表bgra 格式 ； 0 代表bgr格式
        detect_inferenceGpuData = DllFunctionFindHelper.getFuncOf("detect_inferenceGpuData",ADDRESS,ADDRESS, JAVA_INT, JAVA_INT,JAVA_INT);

        detecte_release = DllFunctionFindHelper.getFuncOfVoid("detect_release");



    }

    private static void loadDLL(){
        OpencvUtil.init();
        System.load(Config.YOLOV5_DETECT_DLL_PATH);
    }



    public static void initDetectEngine() throws Throwable {
        try(Arena arena = Arena.ofConfined()) {
            //初始化模型
            MemorySegment engineMemory = arena.allocateUtf8String(Config.YOLOV5_MODEL_PATH);
            DectectWrapper.detecte_init.invoke(engineMemory);
            System.out.println("yolov5初始化成功");
        } catch (Throwable e) {
            System.out.println("yolov5初始化失败");
            throw e;
        }

    }

    public static MethodHandle detecte_init;
    public static MethodHandle detecte_inference;

    public static MethodHandle detect_inferenceGpuData;

    public static MethodHandle detecte_release;




}
