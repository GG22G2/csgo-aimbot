package org.example.csgo.thread;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;
import org.example.csgo.Config;
import org.example.csgo.Locations;
import org.example.csgo.wrapper.CaptureDLLWrapper;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.nio.ByteBuffer;

/**
 * @author 胡帅博
 * @date 2022/2/13 18:28
 */
public class CaptureThread implements Runnable {

    Locations locations;

    public CaptureThread(Locations locations) {
        this.locations = locations;
    }

    @Override
    public void run() {

        MemoryAddress captureRes;
        long totalTime = 0;
        long captureCount = 0;
        int i = 0;

        VideoCapture capture = new VideoCapture(1, Videoio.CAP_DSHOW);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1920);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 1080);
        Mat capImg = new Mat();

        int screenX = Config.SOURCE_WIDTH / 2 - (Config.DETECT_WIDTH / 2);
        int screenY = Config.SOURCE_HEIGHT / 2 - (Config.DETECT_HEIGHT / 2);

        ResourceScope scope = ResourceScope.newConfinedScope();


        Mat bgrDetectImg = new Mat(Config.DETECT_WIDTH, Config.DETECT_HEIGHT, CvType.CV_8UC3);


        while (true) {
            try {

                long captureStartTime = System.currentTimeMillis();

                //使用dxgi方式截图
                {
                    captureRes = (MemoryAddress) CaptureDLLWrapper.capture_capture.invokeExact();
                    if (captureRes.toRawLongValue() == 0) {
                        continue;
                    }
                    ByteBuffer imageBuffer = captureRes.asSegment(1920 * 1080 * 4, scope).asByteBuffer();

                    //截屏返回的是四通道BGRA格式,这里转成BGR
                    Mat bgraImg = new Mat(1080, 1920, CvType.CV_8UC4, imageBuffer);

                    Rect roi = new Rect(screenX, screenY, Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
                    Mat bgraImgrRoi = new Mat(bgraImg, roi);

                    Imgproc.cvtColor(bgraImgrRoi, bgrDetectImg, Imgproc.COLOR_BGRA2BGR);

                    bgraImg.release();
                    bgraImgrRoi.release();

                    long captureEndTime = System.currentTimeMillis();

                    //减2毫秒是为了接近实际截图时间
                    long t1 = System.nanoTime();
                    locations.addCapture(bgrDetectImg, captureEndTime - 2);
                    // totalTime+=((System.nanoTime()-t1)/1000000.0);
                    // System.out.println("等待用时：" + ((System.nanoTime() - t1) / 1000000.0));

                    long useTime = captureEndTime - captureStartTime;

                    //控制识别速率
                    if (useTime <= Config.CAPTURE_AND_DETECT_TIME) {
                        try {
                            Thread.sleep((int) (Config.CAPTURE_AND_DETECT_TIME - useTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //captureCount++;
                    //System.out.println("平均队列延迟" + (totalTime * 1.0 / captureCount));

                }


               // obs录屏，通过虚拟摄像头获取内容,但是会导致游戏帧率下降比较多
//                {
//
//                    capture.read(capImg);
//
//                    Rect roi = new Rect(screenX, screenY, Config.DETECT_WIDTH, Config.DETECT_HEIGHT);
//                    Mat bgrImgrRoi = new Mat(capImg, roi);
//
//                    bgrImgrRoi.copyTo(bgrDetectImg);
//
//              //     Imgcodecs.imwrite("image\\"+captureStartTime+".bmp",bgrImgrRoi);
//
//                    long captureEndTime = System.currentTimeMillis();
//                    //减1毫秒是为了接近实际截图时间
//                    //  long t1 = System.nanoTime();
//                    locations.addCapture(bgrDetectImg, captureEndTime - 2);
//
//
//                    // totalTime+=((System.nanoTime()-t1)/1000000.0);
//                    // System.out.println("等待用时：" + ((System.nanoTime() - t1) / 1000000.0));
//
//                    long useTime = System.currentTimeMillis() - captureStartTime;
//
//                    //控制识别速率
//                    if (useTime <= Config.CAPTURE_AND_DETECT_TIME) {
//                        try {
//                            Thread.sleep((int) (Config.CAPTURE_AND_DETECT_TIME - useTime));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }


            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }


}
