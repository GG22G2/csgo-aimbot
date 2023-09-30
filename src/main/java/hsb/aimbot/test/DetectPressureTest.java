package hsb.aimbot.test;


import hsb.aimbot.OpencvUtil;
import hsb.aimbot.csgo.Config;
import hsb.aimbot.csgo.wrapper.DectectWrapper;
import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author 胡帅博
 * @date 2023/3/27 13:36
 */
public class DetectPressureTest {


    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();
        int cab = 0;
        Arena arena = Arena.ofShared();

        MemorySegment engineMemory = arena.allocateUtf8String(Config.YOLOV5_MODEL_PATH);
        DectectWrapper.detecte_init.invoke(engineMemory);


        Mat showImg = Imgcodecs.imread("G:\\dataset\\csgo\\train\\images\\1-85-230.png");
        //G:\dataset\csgo\train\images\1-85-230.png
        File file = new File("useTimeRecord.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter outputStream = new OutputStreamWriter(fileOutputStream);


        for (int j = 0; j < 100000000; j++) {
            Mat showImg2 = new Mat();

            long startNanos_21_37 = System.nanoTime();
            showImg.copyTo(showImg2);

            MemorySegment bgrImgAddress = MemorySegment.ofAddress(showImg2.dataAddr());

            MemorySegment res = (MemorySegment) DectectWrapper.detecte_inference.invokeExact(bgrImgAddress, Config.DETECT_WIDTH, Config.DETECT_HEIGHT);

            float[] rects = MemoryAccessHelper.asSegment(res, 6 * 20 * 4).toArray(ValueLayout.JAVA_FLOAT);

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
