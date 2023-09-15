package org.example.csgo.wrapper;

import jdk.incubator.foreign.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

/**
 * @author 胡帅博
 * @date 2022/7/31 17:38
 */
public class WinCaptureWrapper {



    static {
      //  System.load("G:\\kaifa_environment\\code\\C\\bebo-capture\\x64\\Debug\\bebo-inject-capture.dll");

        System.load("G:\\kaifa_environment\\code\\clion\\screen-record\\cmake-build-release\\bebo-capturedll.dll");


       // System.load("G:\\kaifa_environment\\code\\C\\bebo-capture\\x64\\Release\\bebo-inject-capture.dll");
    }


    public static MethodHandle init_csgo_capture = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("init_csgo_capture").get(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class,MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER,CLinker.C_POINTER,CLinker.C_POINTER)
    );

    public static MethodHandle game_capture_tick_cpu = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("game_capture_tick_cpu").get(),
            MethodType.methodType(MemoryAddress.class,MemoryAddress.class,float.class),
            FunctionDescriptor.of(CLinker.C_POINTER,CLinker.C_POINTER,CLinker.C_FLOAT)
    );
    public static MethodHandle game_capture_tick_gpu = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("game_capture_tick_gpu").get(),
            MethodType.methodType(MemoryAddress.class,MemoryAddress.class,float.class,int.class,int.class,int.class,int.class),
            FunctionDescriptor.of(CLinker.C_POINTER,CLinker.C_POINTER,CLinker.C_FLOAT,CLinker.C_INT,CLinker.C_INT,CLinker.C_INT,CLinker.C_INT)
    );
    public static MethodHandle stop_game_capture = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("stop_game_capture").get(),
            MethodType.methodType(byte.class,MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_CHAR,CLinker.C_POINTER)
    );


    public static  MemoryAddress init_csgo_capture() throws Throwable {
        String windowName = "Counter - Strike: Global Offensive - Direct3D 9";
        String windowClassName = "Valve001";
//        String windowName = "守望先锋";
//        String windowClassName = "TankWindowClass";
     //   String windowName = "Apex Legends";
    //    String windowClassName = "Respawn001";
        ResourceScope scope = ResourceScope.globalScope();
        MemorySegment w1 = CLinker.toCString(windowName, scope);
        MemorySegment w2 = CLinker.toCString(windowClassName, scope);

        return (MemoryAddress) init_csgo_capture.invokeExact(w1.address(),w2.address());
    }



    public static void main(String[] args) throws Throwable {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);




        MemoryAddress res = (MemoryAddress) init_csgo_capture.invokeExact();

        for(int i = 0; i < 1000000; i++) {
            try {
                long startNanos_54_48 = System.nanoTime();
                MemoryAddress captureRes = (MemoryAddress) game_capture_tick_cpu.invokeExact(res, 4.0f);

                if (captureRes.toRawLongValue() == 0) {
                    Thread.sleep(100);
                    continue;
                }

                ResourceScope scope = ResourceScope.newConfinedScope();
                ByteBuffer imageBuffer = captureRes.asSegment(1920 * 1080 * 4, scope).asByteBuffer();

                //截屏返回的是四通道BGRA格式,这里转成BGR
                Mat bgraImg = new Mat(1080, 1920, CvType.CV_8UC4, imageBuffer);
                long endNanos_54_60 = System.nanoTime();
                System.out.println((endNanos_54_60 - startNanos_54_48) / 1000000.0);

                //Imgcodecs.imwrite("G:\\dataset\\csgo\\unBioaji3\\" + System.currentTimeMillis() + ".png", bgraImg);

                bgraImg.release();
                //scope.close();

                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


}
