package hsb.aimbot.csgo.utils;

import java.io.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 胡帅博
 * @date 2023/3/26 17:21
 */
public class RecordImageDetect {

    static String ffmpeg = "D:\\tools\\ffmpeg-n5.0-latest-win64-gpl-5.0\\bin\\ffmpeg.exe";

    int count = 0;

    BlockingDeque<byte[]> blockingDeque = new LinkedBlockingDeque<byte[]>();

    AtomicBoolean hashImg = new AtomicBoolean(true);

    public void RecordImageDetect() {

    }


    public void addMat(byte[] bmpPixels) {
      //  if (count > 1000) {
       //     end();
     //       return;
      //  }
        count++;
        blockingDeque.add(bmpPixels);
    }


    public void end() {
        hashImg.set(false);
    }

    public void start() {
        blockingDeque.clear();
        hashImg.set(true);
        ProcessBuilder extractBuilder =
                new ProcessBuilder(
                        ffmpeg
                        , "-f", "rawvideo"
                        , "-video_size", " 864x416"
                        , "-pixel_format", "bgr24"
                        , "-framerate", "240.0"
                        , "-i", "pipe:0"
                        , "-loglevel"
                        , "quiet"
                        , "-c:v", "libx264"
                        , "-preset", "ultrafast"
                        , "-pix_fmt", "bgr24"
                        , "-crf", "10"
                        , "-shortest"
                        , "-y"
                        , "output.mkv"
                );

        extractBuilder.redirectErrorStream(true);
        extractBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = extractBuilder.start();
            OutputStream ffmpegInput = process.getOutputStream();

            new Thread(() -> {
                while (hashImg.get()) {
                    try {
                        byte[] bmpPixels = blockingDeque.take();
                       // long startNanos_1_125 = System.nanoTime();
                        ffmpegInput.write(bmpPixels);
                      //  long endNanos_1_127 = System.nanoTime();
                     //   System.out.println((endNanos_1_127 - startNanos_1_125) / 1000000.0);
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                    ffmpegInput.close();
                } catch (Exception e) {

                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
