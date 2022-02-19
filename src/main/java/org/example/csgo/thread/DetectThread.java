package org.example.csgo.thread;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.foreign.MemoryAddressImpl;
import org.example.csgo.Config;
import org.example.csgo.Locations;
import org.example.csgo.wrapper.DectectWrapper;
import org.opencv.core.Mat;

/**
 * @author 胡帅博
 * @date 2022/2/12 17:06
 * <p>
 * 定位csgo中警和匪位置的线程
 */
public class DetectThread implements Runnable {
    Locations locations;

    public DetectThread(Locations locations) {
        this.locations = locations;
    }


    @Override
    public void run() {
        try {
            ResourceScope scope = ResourceScope.newConfinedScope();

            //初始化模型
            MemorySegment engineMemory = CLinker.toCString("G:\\dataset\\csgo\\yolov5n_csgo.engine", scope);
            DectectWrapper.detecte_init.invoke(engineMemory.address());

            Mat captureRes;

            int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
            int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);

            while (true) {
                try {
                    Locations.CaptureRecord captureRecord = locations.nextCapture();
                    captureRes = captureRecord.mat;

                    MemoryAddressImpl bgrImgAddress = new MemoryAddressImpl(null, captureRes.dataAddr());

                    MemoryAddress res = (MemoryAddress) DectectWrapper.detecte_inference.invokeExact(bgrImgAddress.address(), Config.DETECT_WIDTH, Config.DETECT_HEIGHT);

                    //返回6*20个float  6个为一组  x,y,width,height,conf,classid
                    float[] rects = res.asSegment(6 * 20 * 4, scope).toFloatArray();
                    locations.update(screenX, screenY, rects, captureRecord.time);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
