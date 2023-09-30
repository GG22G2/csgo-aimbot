//package hsb.aimbot.test;
//
//import java.awt.*;
//import java.awt.peer.RobotPeer;
//import java.io.*;
//import java.lang.reflect.Field;
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * @author 胡帅博
// * @date 2022/12/13 19:22
// */
//public class ImageToVideo {
//
//    static String ffmpeg = "D:\\tools\\ffmpeg-n5.0-latest-win64-gpl-5.0\\bin\\ffmpeg.exe";
//
//    public static void main(String[] args) throws Exception, FileNotFoundException {
//
//        File ffmpeg_output_msg = new File("ffmpeg_output_msg.txt");
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10240000);
//
//            ProcessBuilder extractBuilder =
//                new ProcessBuilder(
//                        ffmpeg
//                        , "-f", "image2pipe"
//                        , "-y"
//                ,"-re"
//                        ,"-vsync","vfr"
////                        , "-codec", "mjpeg"
////                        , "-codec", "bmp"
//                        , "-i", "pipe:0"
//                        , "-loglevel"
//                        , "quiet"
//                        ,"-c:v", "libx264"
//                        ,"-preset" ,"ultrafast"
//                        ,"-pix_fmt","bgr24"
//                        ,"-crf","10"
//                        , "-f", "avi"
//                        ,"-shortest"
//                        , "-y"
//                      , "pipe:1"
//                );
//
//        System.out.println(extractBuilder.command().toString());
//        extractBuilder.redirectErrorStream(true);
//        extractBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
//
//        try {
//            Process process = extractBuilder.start();
//            OutputStream ffmpegInput = process.getOutputStream();
//            InputStream inputStream = process.getInputStream();
//
//            new Thread(() -> {
//                byte[] buffer = new byte[1024000];
//                int length = 0;
//                int position = 0;
//                try {
//                    FileOutputStream outputStream2 = new FileOutputStream(new File("d4.avi"));
//                    while ((length = inputStream.read(buffer, position, buffer.length - position)) != -1) {
//                        position += length;
//                        if (position > 0.5 * buffer.length) {
//                            outputStream2.write(buffer, 0, position);
//                            position = 0;
//                        }
//                    }
//                    outputStream2.write(buffer, 0, position);
//                    outputStream2.flush();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//
//            RobotPeer robotPeer = getRobotPeer();
//            BlockingDeque<byte[]> blockingDeque = new LinkedBlockingDeque<byte[]>();
//
//            AtomicBoolean hashImg = new AtomicBoolean(true);
//            new Thread(()->{
//                while (hashImg.get()){
//                    try {
//                        byte[] bmpPixels = blockingDeque.take();
//                        long startNanos_1_125 = System.nanoTime();
//                        ffmpegInput.write(bmpPixels);
//                        long endNanos_1_127 = System.nanoTime();
//                        System.out.println((endNanos_1_127 - startNanos_1_125) / 1000000.0);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                try {
//                    Thread.sleep(1000);
//                    ffmpegInput.close();
//                } catch (Exception e) {
//
//                }
//
//            }).start();
//
//
//            for (int i = 0; i < 30; i++) {
//                byte[] bmpPixels = screenToBmp(robotPeer);
//                blockingDeque.add(bmpPixels);
//            }
//
//            hashImg.set(false);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public static RobotPeer getRobotPeer() {
//        try {
//            Robot robot = new Robot();
//
//            Field peerField = robot.getClass().getDeclaredField("peer");
//            peerField.setAccessible(true);
//            RobotPeer peer = (RobotPeer) peerField.get(robot);
//            return peer;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static byte[] screenToBmp(RobotPeer peer) {
//
//        //这个头中包含了视频是 1920 * 1080 * 3的格式了
//        byte[] bmpHeader = {0x42, 0x4d, 0x36, (byte) 0xec, 0x5e, 0, 0, 0, 0, 0, 0x36, 0, 0, 0, 0x28, 0, 0, 0, (byte) 0x80, 0x7, 0, 0, 0x38, 0x4, 0, 0, 0x1, 0, 0x18, 0, 0, 0, 0, 0, 0, (byte) 0xec, 0x5e, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        Rectangle screenRect = new Rectangle(0, 0, 1920, 1080);
//        //截图
//        int[] pixels = peer.getRGBPixels(screenRect);
//
//        //解析像素，返回的是bgr格式
//
//        int length = screenRect.width * screenRect.height;
//        byte[] imgBytes = new byte[length * 3 + bmpHeader.length];
//        System.arraycopy(bmpHeader, 0, imgBytes, 0, bmpHeader.length);
//
//        int byteIndex = 0;
//        for (int i = 0, pixel = 0; i < length; i++) {
//            int startRow = 1079 - i / 1920;
//            byteIndex = startRow * 1920 + (i % 1920);
//            byteIndex = byteIndex * 3+54;
//            pixel = pixels[i];
//            //  pixel中是按照rgb格式排序，但是opencv默认是bgr格式
//            imgBytes[byteIndex] = (byte) (pixel);
//            pixel = pixel >> 8;
//            imgBytes[byteIndex + 1] = (byte) (pixel);
//            imgBytes[byteIndex + 2] = (byte) (pixel >> 8);
//        }
//
//        return imgBytes;
//    }
//
//
//
//}