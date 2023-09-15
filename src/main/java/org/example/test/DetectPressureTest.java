package org.example.test;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.foreign.MemoryAddressImpl;
import org.example.OpencvUtil;
import org.example.csgo.Config;
import org.example.csgo.wrapper.DectectWrapper;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author 胡帅博
 * @date 2023/3/27 13:36
 */
public class DetectPressureTest {


    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();
        int cab = 0;
        ResourceScope scope = ResourceScope.newConfinedScope();
        MemorySegment engineMemory = CLinker.toCString(Config.YOLOV5_MODEL_PATH, scope);

        DectectWrapper.detecte_init.invoke(engineMemory.address());


        Mat showImg = Imgcodecs.imread("G:\\dataset\\csgo\\train\\images\\1-85-230.png");
        //G:\dataset\csgo\train\images\1-85-230.png
        File file = new File("useTimeRecord.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter outputStream = new OutputStreamWriter(fileOutputStream);


        for (int j = 0; j < 100000000; j++) {
            Mat showImg2 = new Mat();

            long startNanos_21_37 = System.nanoTime();
            showImg.copyTo(showImg2);
            MemoryAddressImpl bgrImgAddress = new MemoryAddressImpl(null, showImg2.dataAddr());
            MemoryAddress res = (MemoryAddress) DectectWrapper.detecte_inference.invokeExact(bgrImgAddress.address(), Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
            float[] rects = res.asSegment(6 * 20 * 4, scope).toFloatArray();
            // System.out.println(Arrays.toString(rects));
            int i = validCount(rects);
            cab += i;
            long endNanos_21_43 = System.nanoTime();


            System.out.println((endNanos_21_43 - startNanos_21_37) / 1000000.0);
            //outputStream.write(""+((endNanos_21_43 - startNanos_21_37) / 1000000.0)+",");
            showImg2.release();
        }
        showImg.release();
        System.out.println(cab);
        DectectWrapper.detecte_release.invoke();
    }


    private static int validCount(float[] rects) {
        int count = 0;
        for (int i = 0, j = 0; j < 120; i++, j += 6) {
            if (rects[j + 2] <= 0) {
                break;
            }
            float conf = rects[j + 4];
            if (conf < 0.7) {
                continue;
            }
            count++;
        }
        return count;
    }

}
