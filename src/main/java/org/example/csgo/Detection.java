package org.example.csgo;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.foreign.MemoryAddressImpl;
import org.example.csgo.wrapper.DectectWrapper;
import org.example.csgo.wrapper.WinCaptureWrapper;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

/**
 * @author 胡帅博
 * @date 2022/7/16 18:38
 */
public class Detection {
    Locations locations;

    MemoryAddress captureRes;
    int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
    int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);
    long totalTime = 0;

    ResourceScope captureScope = ResourceScope.newConfinedScope();
    ResourceScope detectScope = ResourceScope.newConfinedScope();

    Mat bgrDetectImg = new Mat(Config.DETECT_WIDTH, Config.DETECT_HEIGHT, CvType.CV_8UC3);

    MemoryAddress dataAddress;


    public Detection(Locations locations) {
        this.locations = locations;
        init();
    }


    public void init() {
        try {
            dataAddress = WinCaptureWrapper.init_csgo_capture();
            //尝试获取一帧
            while (true){
                MemoryAddress captureRes = (MemoryAddress) WinCaptureWrapper.game_capture_tick_cpu.invokeExact(dataAddress, 4.0f);
                if (captureRes.toRawLongValue() == 0) {
                    Thread.sleep(20);
                }else {
                    break;
                }
            }
            System.out.println("成功捕获到一帧");

            MemorySegment engineMemory = CLinker.toCString(Config.YOLOV5_MODEL_PATH, ResourceScope.globalScope());
            Object invoke = DectectWrapper.detecte_init.invoke(engineMemory.address());

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    public void detect() {
        try {



            while (true) {
               // captureRes = (MemoryAddress) CaptureDLLWrapper.capture_capture.invokeExact();
                captureRes = (MemoryAddress) WinCaptureWrapper.game_capture_tick_cpu.invokeExact(dataAddress, 4.0f);
                if (captureRes.toRawLongValue() == 0) {
                    try {
                        System.out.println("重试一次");
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                break;
            }
            ByteBuffer imageBuffer = captureRes.asSegment(1920 * 1080 * 4, captureScope).asByteBuffer();

            //截屏返回的是四通道BGRA格式,这里转成BGR
            Mat bgraImg = new Mat(1080, 1920, CvType.CV_8UC4, imageBuffer);

            Rect roi = new Rect(screenX, screenY, Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
            Mat bgraImgRoi = new Mat(bgraImg, roi);

            Imgproc.cvtColor(bgraImgRoi, bgrDetectImg, Imgproc.COLOR_BGRA2BGR);
            bgraImg.release();
            bgraImgRoi.release();

            MemoryAddressImpl bgrImgAddress = new MemoryAddressImpl(null, bgrDetectImg.dataAddr());

            MemoryAddress res = (MemoryAddress) DectectWrapper.detecte_inference.invokeExact(bgrImgAddress.address(), Config.DETECT_WIDTH, Config.DETECT_HEIGHT);

            float[] rects = res.asSegment(6 * 20 * 4, detectScope).toFloatArray();
            locations.update(screenX, screenY, rects, System.currentTimeMillis());

        } catch (Throwable e) {
            e.printStackTrace();
        }


    }


}
