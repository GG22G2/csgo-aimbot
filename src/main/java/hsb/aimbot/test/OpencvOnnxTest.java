package hsb.aimbot.test;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author 胡帅博
 * @date 2023/9/22 16:35
 */
public class OpencvOnnxTest {
    public final static float scoreThreshold = 0.5f;   // 分数阈值

    public static void main(String[] args) throws IOException {
        System.load("G:\\kaifa_environment\\opencv\\opencv_4_8\\opencv\\build\\java\\x64\\opencv_java480.dll");

        // 加载ONNX模型
        String modelPath = "D:\\wg\\apex_320_fd.onnx";

       // modelPath = "G:\\kaifa_environment\\code\\java\\csgo-aimbot\\model\\best_apex.onnx";
        Net net = Dnn.readNetFromONNX(modelPath);

        // 加载输入图像
        String imagePath = "G:\\dataset\\apex\\test\\1-2-78.png";
        Mat temp = Imgcodecs.imread(imagePath);


//        List<DetectResult> detectResults = inferNoReSize(net, temp);
//
//        for (DetectResult detectResult : detectResults) {
//            System.out.println(detectResult);
//            Rect2d rect2d = detectResult.rect2d;
//            int x = (int) (rect2d.x - rect2d.width / 2);
//            int y = (int) (rect2d.y - rect2d.height / 2);
//            Imgproc.rectangle(temp, new Rect(x, y, (int) rect2d.width, (int) rect2d.height), new Scalar(255, 0, 0), 1);
//        }
//
//        Imgcodecs.imwrite("temp/1.jpg", temp);

        detectDirImg(net, "G:\\dataset\\apex\\test\\123.png");

    }


    public static void detectDirImg(Net net, String dir) throws IOException {

        //

        Stream<Path> walk = Files.walk(Path.of(dir));
        //  List<Path> list = walk.toList();
        walk.forEach(path -> {
            if (Files.isDirectory(path)) {
                return;
            }

            Mat temp = Imgcodecs.imread(path.toString());

            List<DetectResult> detectResults = inferNoReSize(net, temp);

            for (DetectResult detectResult : detectResults) {
                System.out.println(detectResult);
                Rect2d rect2d = detectResult.rect2d;
                int x = (int) (rect2d.x - rect2d.width / 2);
                int y = (int) (rect2d.y - rect2d.height / 2);
                Imgproc.rectangle(temp, new Rect(x, y, (int) rect2d.width, (int) rect2d.height), new Scalar(255, 0, 0), 1);
            }

            Imgcodecs.imwrite(STR. "temp/\{ path.getFileName() }" , temp);
        });
    }


    public static List<DetectResult> inferNoReSize(Net net, Mat image) {
        Size size = new Size(320, 320);
        List<Mat> mats = splitImage(image, size);
        List<List<DetectResult>> outputs = new ArrayList<>();
        for (Mat mat : mats) {
            List<DetectResult> output = infer(net, mat, size);
            outputs.add(output);
            mat.release();
        }
        return mergeImagesResult(new Size(image.width(), image.height()), size, outputs);
    }


    public static List<DetectResult> mergeImagesResult(Size origin, Size curSize, List<List<DetectResult>> outputs) {

        List<Integer> splitIndex = splitSize(origin, curSize, 100);

        List<Rect2d> rect2dList = new ArrayList<>();
        List<Float> scoreList = new ArrayList<>();
        List<Integer> typeIndexs = new ArrayList<>();

        for (int i = 0; i < splitIndex.size(); i += 2) {

            List<DetectResult> output = outputs.get(i / 2);

            int rowOffset = splitIndex.get(i);
            int colOffset = splitIndex.get(i + 1);

            for (DetectResult temp : output) {
                Rect2d rect2d = temp.rect2d;
                rect2d.x = rect2d.x + rowOffset;
                rect2d.y = rect2d.y + colOffset;
                rect2dList.add(rect2d);
                scoreList.add(temp.score);
                typeIndexs.add(temp.configId);
            }
        }

        if (scoreList.isEmpty()) {
            return List.of();
        }


        // 假设这是你的检测结果
        MatOfRect2d boxes = new MatOfRect2d();
        boxes.fromList(rect2dList);
        // 假设这是你的边界框得分
        MatOfFloat scores = new MatOfFloat();
        scores.fromList(scoreList);


        List<DetectResult> boxs = nms(boxes, scores, typeIndexs);


        return boxs;
    }

    /**
     * 把图片拆分成多个size大小的图片
     */
    public static List<Mat> splitImage(Mat origin, Size size) {

        List<Integer> splitIndex = splitSize(new Size(origin.width(), origin.height()), size, 100);

        List<Mat> result = new ArrayList<>(splitIndex.size() / 2);
        for (int i = 0; i < splitIndex.size(); i += 2) {
            int rowOffset = splitIndex.get(i);
            int colOffset = splitIndex.get(i + 1);
            Mat submat = origin.submat(new Rect(rowOffset, colOffset, (int) size.width, (int) size.height));
            result.add(submat);
        }

        return result;
    }


    public static List<Integer> splitSize(Size origin, Size dstSize, int interval) {
        int rowSkipCount = (int) Math.ceil((origin.width - dstSize.height) / interval) + 1;
        int colSkipCount = (int) Math.ceil((origin.height - dstSize.height) / interval) + 1;
        List<Integer> result = new ArrayList<>(rowSkipCount * colSkipCount * 2);
        int rowOffset = 0;
        for (int i = 0; i < rowSkipCount; i++) {
            int colOffset = 0;
            for (int j = 0; j < colSkipCount; j++) {
                result.add(rowOffset);
                result.add(colOffset);
                colOffset = (colOffset + interval + dstSize.height) <= origin.height ? colOffset + interval : (int) (origin.height - dstSize.height);
            }
            rowOffset = (rowOffset + interval + dstSize.width) <= origin.width ? rowOffset + interval : (int) (origin.width - dstSize.width);
        }
        return result;
    }

    public static List<DetectResult> infer(Net net, Mat input, Size inputSize) {
//        Mat mat2 = new Mat();
//        input.convertTo(mat2, CvType.CV_32F);
//        Core.divide(mat2, new Scalar(255, 255, 255), mat2);
//
//
//        List<Mat> channels = new ArrayList<>();
//        Core.split(mat2, channels);
//
//        Mat rmat = channels.get(0);
//        Mat bmat = channels.get(2);
//
//        //channels.set(0, bmat);
//        //channels.set(2, rmat);
//
//        Mat transposedImage = new Mat();
//
//        Core.vconcat(channels, transposedImage);
//        seeMatPixel2(transposedImage);
//        Mat image = transposedImage.reshape(0, new int[]{1, 3, (int) inputSize.height, (int) inputSize.width});


        //seeMatPixel(image);



        Mat  image = Dnn.blobFromImage(input, 1.0 / 255, inputSize , new Scalar(0, 0, 0), false, false, CvType.CV_32F);


        //seeMatPixel(image);

        // 设置输入数据
        net.setInput(image);
        // 运行推理
        Mat output = net.forward();
        seeMatPixel(output);

        List<DetectResult> result = getResult(output);


        image.release();
        output.release();

        return result;
    }

    private static void seeMatPixel2(Mat mat) {


        float[] pixles = new float[mat.width() * mat.height() * mat.channels()];
        mat.get(0, 0, pixles);


      //  System.out.println(pixles);
    }

    private static void seeMatPixel(Mat image) {
        int length = 1;
        int[] idx = new int[image.dims()];
        for (int i = 0; i < image.dims(); i++) {
            length *= image.size(i);
        }
        float[] pixles = new float[length];
        image.get(idx, pixles);

        System.out.println(pixles);
    }


    private static float[] getMatPixelF(Mat image) {
        int length = 1;
        int[] idx = new int[image.dims()];
        for (int i = 0; i < image.dims(); i++) {
            length *= image.size(i);
        }
        float[] pixles = new float[length];
        image.get(idx, pixles);
        return pixles;
    }

    public static List<DetectResult> getResult(Mat output) {
        float[] pixles = getMatPixelF(output);


        int resultCount = output.size(1);
        int resultLength = output.size(2);
        int classCount = resultLength - 5;

        List<Rect2d> rect2dList = new ArrayList<>(resultCount);
        List<Float> scoreList = new ArrayList<>(resultCount);
        List<Integer> typeIndex = new ArrayList<>(resultCount);

        int size = resultCount * resultLength;

        for (int i = 0; i < size; i += resultLength) {
            float confidence = pixles[i + 4];
            if (Float.isNaN(confidence)) {
                continue;
            }
            if (Float.isNaN(confidence) || confidence < scoreThreshold) {
                continue;
            }
            rect2dList.add(new Rect2d(pixles[i], pixles[i + 1], pixles[i + 2], pixles[i + 3]));
            scoreList.add(confidence);

            int type = maxIndex(pixles, i + 5, i + resultLength - 1);
            typeIndex.add(type);


        }
        if (scoreList.isEmpty()) {
            return List.of();
        }


        // 假设这是你的检测结果
        MatOfRect2d boxes = new MatOfRect2d();
        boxes.fromList(rect2dList);
        // 假设这是你的边界框得分
        MatOfFloat scores = new MatOfFloat();
        scores.fromList(scoreList);

        List<DetectResult> boxs = nms(boxes, scores, typeIndex);

        return boxs;
    }


    private static int maxIndex(float[] output, int start, int end) {
        double max = output[start];
        int index = start;
        for (int i = start + 1; i <= end; i++) {
            if (output[i] > max) {
                max = output[i];
                index = i;
            }
        }
        return index - start;
    }


    private static List<DetectResult> nms(MatOfRect2d boxes, MatOfFloat scores, List<Integer> typeIndexs) {
        // 执行非极大值抑制
        float overlapThreshold = 0.25f; // 重叠阈值


        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxes(boxes, scores, scoreThreshold, overlapThreshold, indices);

        List<DetectResult> boxs = new ArrayList<>();

        // 获取最终的边界框和得分
        List<Integer> indexList = indices.toList();

        for (int i : indexList) {
            Rect2d rect2d = boxes.toArray()[i];

            int configId = typeIndexs.get(i);
            DetectResult detectResult = new DetectResult();
            detectResult.rect2d = rect2d;
            detectResult.score = (float) scores.get(i, 0)[0];
            detectResult.configId = configId;
            boxs.add(detectResult);
            // int x = (int) (rect2d.x - rect2d.width / 2);
            // int y = (int) (rect2d.y - rect2d.height / 2);
            //boxs.add(new Rect((int) x, (int) y, (int) rect2d.width, (int) rect2d.height));
        }

        indices.release();
        boxes.release();
        scores.release();

        return boxs;
    }


}

class DetectResult {
    Rect2d rect2d;
    float score;

    int configId;

    @Override
    public String toString() {
        return "DetectResult{" +
                "rect2d=" + rect2d +
                ", score=" + score +
                ", configId=" + configId +
                '}';
    }
}