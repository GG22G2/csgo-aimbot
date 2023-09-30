//package hsb.aimbot.csgo.test;
//
//
//import hsb.aimbot.OpencvUtil;
//import javafx.application.Application;
//
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//
//import javafx.scene.layout.*;
//
//import javafx.stage.Stage;
//
//import java.lang.foreign.MemorySegment;
//
///**
// * @author 胡帅博
// * @date 2023/9/10 1:24
// */
//public class ImageDisplayApp extends Application {
//
//    @Override
//    public void start(Stage primaryStage) {
//        OpencvUtil.init();
//        // primaryStage.initStyle(StageStyle.UNDECORATED);
//        // 创建画布
//        Canvas canvas = new Canvas();
//
//        // 创建布局并将画布添加到其中
//        AnchorPane stackPane = new AnchorPane();
//
//
//        // 创建场景并将布局添加到场景中
//        Scene scene = new Scene(stackPane, 500, 300);
//        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
//
//        // Long windowPointer = WindowUtil.getWindowPointer(primaryStage);
//
//        stackPane.getChildren().addAll(canvas);
//
//        // 设置舞台的标题和场景
//        primaryStage.setTitle("test_hsb_d3d");
//        primaryStage.setScene(scene);
//
//        // 显示舞台
//        primaryStage.show();
//
//        canvas.widthProperty().bind(stackPane.widthProperty());
//        canvas.heightProperty().bind(stackPane.heightProperty());
//
//
//        long fegwewwgwew = DecodeCUDADecode.findWindow("test_hsb_d3d");
//        System.out.println(fegwewwgwew);
//
//        new Thread(() -> {
//            for (int i = 0; i < 100000; i++) {
//                play(canvas, fegwewwgwew);
//            }
//        }).start();
//
//    }
//
//
//    private void play(Canvas canvas, long hwnd) {
//        DecodeCUDADecode decode = new DecodeCUDADecode();
//        boolean init = decode.init("G:\\dataset\\csgo\\1.mp4",hwnd);
//
//        MemorySegment nextFrame = null;
//
//        long baseClockTime = System.nanoTime();
//
//        while (true) {
//
//            nextFrame = decode.getNextFrame(true);
//            if (nextFrame == null) {
//                break;
//            }
//            //double showInMillSeconds = nextFrame.pts() * av_q2d(decode.video_st.time_base()) * 1000;
//
//            double showInMillSeconds =   decode.getPlayTimePkt(nextFrame);
//
//            int showWidth = (int) canvas.getWidth();
//            int showHeight = (int) canvas.getHeight();
//
//
//            long curClockTime = System.nanoTime();
//
//            double usedMillSeconds = (curClockTime - baseClockTime) / 1000000.0; //已经过了多少毫秒
//
//            if (showInMillSeconds > usedMillSeconds) {
//                long sleepTime = (long) Math.max(showInMillSeconds - usedMillSeconds - 1, 0);
//                //  System.out.println((showInMillSeconds - usedMillSeconds));
//                //睡眠等待并展示
//                //  Thread.sleep(sleepTime);
//            }else {
//                System.out.println("性能真垃圾");
//            }
//
//            decode.renderToScreen(0,0, (int) ( canvas.getWidth()*1.25), (int) (canvas.getHeight()*1.25-100),nextFrame);
//
//        }
//        decode.release();
//    }
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}