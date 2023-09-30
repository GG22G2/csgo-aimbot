package hsb.aimbot.csgo.wrapper;


import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.utils.DllFunctionFindHelper;
import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

import static java.lang.foreign.ValueLayout.*;

/**
 * @author 胡帅博
 * @date 2022/7/31 17:38
 */
public class WinCaptureWrapper {


    static {
        loadDLL();

        init_csgo_capture = DllFunctionFindHelper.getFuncOf("init_csgo_capture", ADDRESS, ADDRESS, ADDRESS);

        game_capture_tick_cpu = DllFunctionFindHelper.getFuncOf("game_capture_tick_cpu", ADDRESS, ADDRESS, JAVA_FLOAT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT);

        game_capture_tick_gpu = DllFunctionFindHelper.getFuncOf("game_capture_tick_gpu", ADDRESS, ADDRESS, JAVA_FLOAT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT);

        stop_game_capture = DllFunctionFindHelper.getFuncOf("stop_game_capture", JAVA_BYTE, ADDRESS);

        cudaFreeProxy = DllFunctionFindHelper.getFuncOfVoid("cudaFreeProxy",ADDRESS);
    }

    private static void loadDLL(){
        System.load(Config.OBS_WIN_CAPTURE_DLL_PATH);
    }


    public static MethodHandle init_csgo_capture;
    public static MethodHandle game_capture_tick_cpu;

    public static MethodHandle game_capture_tick_gpu;

    public static MethodHandle stop_game_capture;

    public static MethodHandle cudaFreeProxy;

    public static MemorySegment init_csgo_capture() throws Throwable {
        String windowName = "Counter-Strike 2";
        String windowClassName = "SDL_app";
//        String windowName = "守望先锋";
//        String windowClassName = "TankWindowClass";
//        String windowName = "Apex Legends";
//        String windowClassName = "Respawn001";
        Arena arena = Arena.ofShared();

        MemorySegment w1 = arena.allocateUtf8String(windowName);
        MemorySegment w2 = arena.allocateUtf8String(windowClassName);
        return (MemorySegment) init_csgo_capture.invokeExact(w1, w2);
    }


    public static MemorySegment initCapture() throws Throwable {
        MemorySegment dateAddress;
        while (true) {
            dateAddress = WinCaptureWrapper.init_csgo_capture();

            if (dateAddress.address() == 0) {
                Thread.sleep(20);
            } else {
                while (true) {
                    MemorySegment captureRes = (MemorySegment) WinCaptureWrapper.game_capture_tick_cpu.invokeExact(dateAddress, 4.0f,0,0,1,1);
                    if (captureRes.address() != 0) {
                        break;
                    }
                }
                System.out.println("截图初始化成功，成功捕获到游戏画面");
                MemorySegment finalDateAddress = dateAddress;
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            Config.detecting = false;
                            WinCaptureWrapper.stop_game_capture.invoke(finalDateAddress);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                break;
            }
        }
        return dateAddress;
    }

    public static void main(String[] args) throws Throwable {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        MemorySegment res = (MemorySegment) init_csgo_capture.invokeExact();

        for (int i = 0; i < 1000000; i++) {
            try {

                MemorySegment captureRes = (MemorySegment) game_capture_tick_cpu.invokeExact(res, 4.0f, 0, 0, 1920, 1080);

                if (captureRes.address() == 0) {
                    Thread.sleep(100);
                    continue;
                }

                Arena arena = Arena.ofShared();
                ByteBuffer imageBuffer = MemoryAccessHelper.asSegment(captureRes, 1080 * 1920 * 4).asByteBuffer();
                //截屏返回的是四通道BGRA格式,这里转成BGR
                Mat bgraImg = new Mat(1080, 1920, CvType.CV_8UC4, imageBuffer);


                Imgcodecs.imwrite("G:\\dataset\\csgo\\unBioaji3\\" + System.currentTimeMillis() + ".png", bgraImg);

                bgraImg.release();
                arena.close();

                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


}
