package hsb.aimbot.csgo.test;

import hsb.aimbot.csgo.utils.DllFunctionFindHelper;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

import static java.lang.foreign.ValueLayout.*;

/**
 * @author 胡帅博
 * @date 2023/9/11 17:56
 */
public class DecodeCUDADecode {

    static {

        //分析依赖的方式 使用vs的工具  dumpbin /dependents xxx.dll

        /**
         *
         * 可以把dll准备到一个文件夹下
         *
         * 分析依赖的方式 使用vs的工具  dumpbin /dependents xxx.dll
         *
         * 然后分析这些依赖，递归调用，找到不依赖其他或者依赖的不在当前目录下的，加载，之后逐渐就可以加载所有了
         *
         * */


        /**
         * jvm参数
         * --enable-native-access=hsb.yolov5.yolov5utils
         * -Djava.library.path=G:/kaifa_environment/code/clion/ffmpeg-test-2/cmake-build-release/bin
         * */
        System.loadLibrary("libvpl");
        System.loadLibrary("libx264");
        System.loadLibrary("avutil-58");
        System.loadLibrary("swscale-7");
        System.loadLibrary("swresample-4");
        System.loadLibrary("avcodec-60");
        System.loadLibrary("avformat-60");
        System.loadLibrary("playdll");


        createVideoDecode = DllFunctionFindHelper.getFuncOf("createVideoDecode", ADDRESS);

        init = DllFunctionFindHelper.getFuncOf("init", JAVA_BOOLEAN, ADDRESS, ADDRESS, ValueLayout.JAVA_LONG);

        nextFrame = DllFunctionFindHelper.getFuncOf("nextFrame",ADDRESS, ADDRESS, JAVA_BOOLEAN);

        pixels = DllFunctionFindHelper.getFuncOfVoid("pixels",ADDRESS, ADDRESS);

        release = DllFunctionFindHelper.getFuncOfVoid("release",ADDRESS);

        releaseAVFrame = DllFunctionFindHelper.getFuncOfVoid("releaseAVFrame",ADDRESS);

        getPlayTimePkt =DllFunctionFindHelper.getFuncOf("getPlayTimePkt",JAVA_DOUBLE, ADDRESS, ADDRESS);

        getVideoWidth = DllFunctionFindHelper.getFuncOf("videoWidth",JAVA_INT, ADDRESS);

        getVideoHeight = DllFunctionFindHelper.getFuncOf("videoHeight",JAVA_INT, ADDRESS);

        getWindowHWND = DllFunctionFindHelper.getFuncOf("getWindowHWND",JAVA_LONG, ADDRESS);

        renderToScreen = DllFunctionFindHelper.getFuncOfVoid("renderToScreen",JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS);

    }

    public static long findWindow(String name) {
        Arena arena = Arena.ofShared();
        MemorySegment pathSegment = arena.allocateUtf8String(name);
        try {
            return (long) getWindowHWND.invoke(pathSegment);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        arena.close();
        return -1;
    }

    static MethodHandle createVideoDecode;
    static MethodHandle init;
    static MethodHandle nextFrame;
    static MethodHandle pixels;
    static MethodHandle release;
    static MethodHandle releaseAVFrame;
    static MethodHandle getPlayTimePkt;
    static MethodHandle getVideoWidth;
    static MethodHandle getVideoHeight;

    static MethodHandle getWindowHWND;
    static MethodHandle renderToScreen;
    Arena arena;

    MemorySegment decodePoint;

    public DecodeCUDADecode() {
        try {
            decodePoint = (MemorySegment) createVideoDecode.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public boolean init(String videoPath, long hwnd) {
        arena = Arena.ofShared();
        MemorySegment pathSegment = arena.allocateUtf8String(videoPath);
        try {
            return (boolean) init.invoke(decodePoint, pathSegment, hwnd);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }


    public double getPlayTimePkt(MemorySegment frameAddress) {
        try {
            return (double) getPlayTimePkt.invoke(decodePoint, frameAddress);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * true 直接返回解码出来的结果，不一定bgra格式，也有可能像素数据是保存在gpu中的
     * <p>
     * false 返回的是bgra格式，并且数据在cpu
     */
    public MemorySegment getNextFrame(boolean rawFrame) {
        try {
            MemorySegment frameAddress = (MemorySegment) nextFrame.invokeExact(decodePoint, rawFrame);
            return frameAddress.address() == 0 ? null : frameAddress;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public ByteBuffer asByteBuffer(MemorySegment frameAddress) {

        try {
            MemorySegment pixelSegment = (MemorySegment) pixels.invoke(frameAddress);
            MemorySegment memorySegment = frameAddress.reinterpret(1920 * 1080 * 4);
            return memorySegment.asByteBuffer();

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;

    }

    public int width() {
        try {
            return (int) getVideoWidth.invoke(decodePoint);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int height() {
        try {
            return (int) getVideoHeight.invoke(decodePoint);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void release() {
        try {
            release.invoke(decodePoint);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            arena.close();
        }
    }

    public void releaseAVFrame(MemorySegment frameAddress) {
        try {
            releaseAVFrame.invoke(frameAddress);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 这四个int 都是相对于屏幕左上角的值
     */
    public void renderToScreen(int left, int top, int right, int bottom, MemorySegment frameAddress) {
        try {
            renderToScreen.invoke(left, top, right, bottom, decodePoint, frameAddress);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private Object invokeNoExp(MethodHandle handle, Object... args) {
        try {
            return handle.invoke(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
