package org.example.screenshot;

import jdk.incubator.foreign.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hushuaibo
 * @date 2021/5/13 13:52
 */
public class CallScreenShotDLL {
    //读取超时
    static int DXGI_ERROR_WAIT_TIMEOUT = 0x887A0027;
    //读取成功
    static int NEW_IMAGE = 123456789;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Path paths = Path.of("G:\\kaifa_environment\\code\\C\\screenShot\\x64\\Release\\screenShot.dll");


        System.load("G:\\kaifa_environment\\code\\C\\screenShot\\x64\\Release\\screenShot.dll");
        MethodHandle init = CLinker.getInstance().downcallHandle(
                SymbolLookup.loaderLookup().lookup("capture_init").get(),
                MethodType.methodType(void.class),
                FunctionDescriptor.ofVoid()
        );

        MethodHandle release = CLinker.getInstance().downcallHandle(
                SymbolLookup.loaderLookup().lookup("capture_release").get(),
                MethodType.methodType(void.class),
                FunctionDescriptor.ofVoid()
        );

        MethodHandle capture = CLinker.getInstance().downcallHandle(
                SymbolLookup.loaderLookup().lookup("capture").get(),
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
        );


        MethodHandle match = CLinker.getInstance().downcallHandle(
                SymbolLookup.loaderLookup().lookup("match").get(),
                MethodType.methodType(void.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER)
        );
        Mat temp2 = Imgcodecs.imread("G:\\dataset\\csgo\\train\\images\\1-11-21.jpg");
        byte[] bs = new byte[1920*1080*3];
        temp2.get(0,0,bs);
        System.out.println(temp2);
        System.out.println(bs);


        Mat temp1 = Imgcodecs.imread("C:\\Users\\h6706\\Pictures\\opencv\\1317.bmp", Imgcodecs.IMREAD_GRAYSCALE);

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(6, 6, 1, TimeUnit.DAYS, new ArrayBlockingQueue<>(30));

        try {
            ResourceScope scope = ResourceScope.newConfinedScope();

            Thread.sleep(1500);
            init.invokeExact();
            MemorySegment segment = MemorySegment.allocateNative(1920 * 1080 * 4, scope);
            int result = -1;

            for (int i = 0; i < 10000; i++) {

                long startNanos_2_72 = System.nanoTime();
                while (true) {
                    //返回的是 四通道，BGRA格式
                    result = (int) capture.invokeExact(segment.address());
                    if (result == NEW_IMAGE) {
                        break;
                    }
                }
                long endNanos_2_80 = System.nanoTime();
                System.out.println("截图用时：" + ((endNanos_2_80 - startNanos_2_72) / 1000000.0));
                long startTime = Core.getTickCount();
                /**
                 * 这种方式数据都在外部内存，省去了很多拷贝操作，速度快
                 * 但是需要注意，每次因为写入和读取图片都是在同一块内存，
                 * 所以必须在新一次调用capture将数据写入内存段前，将上一次数据消费掉
                 * 也可以在创建Mat后直接拷贝一份，
                 * */
                ByteBuffer byteBuffer = segment.asByteBuffer();

                Mat temp = new Mat(1080, 1920, CvType.CV_8UC4, byteBuffer);
                Mat image = temp.clone();
                temp.release();

                Mat targetRectGrayMat = new Mat();
                Imgproc.resize(image, targetRectGrayMat, new Size(680, 680));

                //Imgproc.cvtColor(image, targetRectGrayMat, Imgproc.COLOR_BGRA2GRAY);
                image.release();

                //MultithreadMatchTemplate multithreadMatchTemplate = new MultithreadMatchTemplate(poolExecutor,targetRectGrayMat,temp1);
                // Core.MinMaxLocResult resultPoint = multithreadMatchTemplate.run();
                //  System.out.println("坐标："+resultPoint.minLoc);

                long endTime = Core.getTickCount();
                System.out.println((endTime - startTime) / Core.getTickFrequency() * 1000);

                targetRectGrayMat.release();
            }

            scope.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                release.invokeExact();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            poolExecutor.shutdown();
        }
    }



}
