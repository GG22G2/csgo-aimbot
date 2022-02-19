package org.example.csgo;

import org.example.csgo.thread.CaptureThread;
import org.example.csgo.thread.DetectThread;
import org.example.csgo.thread.MouseCorrectThread;
import org.example.csgo.wrapper.CaptureDLLWrapper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.*;

import static org.example.csgo.wrapper.MouseHelpWrapper.*;

/**
 * @author 胡帅博
 * @date 2021/12/10 19:30
 */
public class Main {
    public static void main321(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture capture = new VideoCapture(1,Videoio.CAP_DSHOW);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,1920);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,1080);

        Mat capImg = new Mat();
        for(int i = 0; i < 1; i++) {
            try {
                long startNanos_12_38 = System.nanoTime();
                capture.read(capImg);
                long endNanos_12_40 = System.nanoTime();
                System.out.println((endNanos_12_40 - startNanos_12_38) / 1000000.0);

                Mat bgrImg = new Mat(1080, 1920, CvType.CV_8UC3);


              // Imgproc.cvtColor(capImg, bgrImg, Imgproc.COLOR_RGB2BGR);
              //  Imgproc.cvtColor(capImg, bgrImg, Imgproc.COLOR_BGR2BGR);
             //   Imgproc.cvtColor(capImg, bgrImg, Imgproc.COLOR_BGR2RGB);


                Imgcodecs.imwrite("image\\"+i+".bmp",bgrImg);

                try {
                 Thread.sleep(15);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        capture.release();

    }


    public static void main13131(String[] args) throws Throwable {

        //Tracks track = new Tracks();
        //new Thread(new MouseMoveListenerThread(track)).start();
        Thread.sleep(1000);

        mouse_open.invoke();
        mouse_move.invoke((byte) 100, (byte) 100, (byte) 0);
   //     mouse_move.invokeExact((byte) 0, (byte) 0, (byte) 1);

//        // mouse_move.invoke((byte) 100, (byte) 100, (byte) 0);
//        Thread.sleep(1000);
//        mouse_move.invokeExact((byte) 100, (byte) 0, (byte) 1);
//       // mouse_move.invokeExact((byte) 0, (byte) 0, (byte) 2);
//        Thread.sleep(1000);
//        mouse_move.invokeExact((byte) 0, (byte) 0, (byte) 0);

        mouse_close.invoke();
    }

    public static void whenStop() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                try {
                    mouse_close.invokeExact();
                    //CaptureDLLWrapper.capture_release.invokeExact();
                    //DectectWrapper.detecte_release.invokeExact();
                    System.out.println("成功释放鼠标，截图，识别dll");
                } catch (Throwable e) {
                    e.printStackTrace();
                }


            }
        });

    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            whenStop();
            Robot robot = new Robot();
            //todo 截图使用DXGI方式，好像dxgi和cudaart.lib冲突，所以要确保先初始化截图
            //初始化截图
            int initRes = (int) CaptureDLLWrapper.capture_init.invokeExact();
            byte result = (byte) mouse_open.invokeExact();
            System.out.println(initRes + "," + result);
            Locations locations = new Locations();
            Tracks track = new Tracks();
            //new Thread(new MouseMoveListenerThread(track)).start();
            new Thread(new CaptureThread(locations)).start();
            new Thread(new DetectThread(locations)).start();
            new Thread(new MouseCorrectThread(locations, track)).start();


            //new Thread(new MouseHelpThread(locations, robot, track)).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


}








