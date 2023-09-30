package hsb.aimbot.test;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/9/27 15:42
 */
public class OpencvTorchTest {
    public static void main(String[] args) throws IOException {

            System.load("G:\\kaifa_environment\\opencv\\opencv_all_build\\java\\opencv452\\opencv_java452.dll");

            // 加载ONNX模型
            String modelPath = "G:\\kaifa_environment\\code\\yolov5\\python\\runs\\train\\exp10\\weights\\best.onnx";

            //modelPath = "G:\\kaifa_environment\\code\\java\\csgo-aimbot\\model\\best_apex.onnx";
            Net net = Dnn.readNetFromONNX(modelPath);

            // 加载输入图像
            String imagePath = "G:\\dataset\\csgo\\111\\1-57-35.png";
            Mat temp = Imgcodecs.imread(imagePath);


//        List<DetectResult> detectResults = inferNoReSize(net, temp);



        List<DetectResult> detectResults = OpencvOnnxTest.infer(net, temp,new Size(864, 416));
        for (DetectResult detectResult : detectResults) {
            System.out.println(detectResult);
            Rect2d rect2d = detectResult.rect2d;
            int x = (int) (rect2d.x - rect2d.width / 2);
            int y = (int) (rect2d.y - rect2d.height / 2);
            Imgproc.rectangle(temp, new Rect(x, y, (int) rect2d.width, (int) rect2d.height), new Scalar(255, 0, 0), 1);
        }

        Imgcodecs.imwrite("temp/1.jpg", temp);
     //   OpencvOnnxTest.detectDirImg(net, "G:\\dataset\\apex\\train\\images");


    }
}
