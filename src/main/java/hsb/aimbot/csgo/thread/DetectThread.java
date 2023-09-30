package hsb.aimbot.csgo.thread;

import hsb.aimbot.csgo.*;
import hsb.aimbot.csgo.utils.Time;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static hsb.aimbot.csgo.Config.cx;
import static hsb.aimbot.csgo.Config.cy;

/**
 * @date 2022/2/12 17:06
 * <p>
 * 定位csgo中警和匪位置的线程
 */
public class DetectThread implements Runnable {
    Locations locations;

    ReentrantLock lock;
    Condition condition;
    HistoryRecord historyRecord = HistoryRecord.getInstance();

    final List<Long> useTimeRecord = new ArrayList<>(1000);

    CSGODetect detect;

    public DetectThread(CSGODetect detect, Locations locations, ReentrantLock lock, Condition condition) {
        this.locations = locations;
        this.lock = lock;
        this.condition = condition;
        this.detect = detect;

    }


    @Override
    public void run() {

        //保留最后多少次记录的数据
        int recordLength = 4;
        //保留最后五个数据,循环替换
        DetectHistory[] history = new DetectHistory[recordLength];
        for (int i = 0; i < recordLength; i++) {
            history[i] = new DetectHistory();
        }
        int position = 0;

        MemorySegment captureRes;

        int skipFrame = 0;



        while (true) {
            try {
                // Locations.CaptureRecord captureRecord = locations.nextCapture();

                //  captureRes = captureRecord.gpuPoint;


                //识别一帧
                Locations.CaptureRecord record = detect.detectOnce();

                int time = record.time;

                int index = position;
                DetectHistory detectHistory = history[index];


                /**
                 * 识别后可以有多种模式
                 * 1.准星有人就自动开启
                 * 2.追星定位到人身上就自动定位加跟枪一定时间
                 *
                 *   具体判断方法就是枪从远处拉近，记录鼠标事件，记录识别记录，
                 *    根据拉枪速度预测定位时间点，根据鼠标位移判断是否需要定位
                 * 3.灵敏跟枪测速
                 *
                 * */
                if (locations.getCount() == 0) {
                    //detectHistory.reset(time);
                    skipFrame = recordLength - 1;
                } else {
                    //如果鼠标在目标中心，则立即射击
                    Location location = locations.minDistance(cx, cy);
                    double xPixel = location.centerX - cx;
                    double yPixel = location.centerY - (location.height / 10.0) - cy;

                    detectHistory.xPixels = xPixel;
                    detectHistory.yPixels = yPixel;
                    detectHistory.time = time;

                    int lastIndex = index == 0 ? recordLength - 1 : index - 1;
                    DetectHistory first = history[(index + 1) % recordLength];  //四个点中最旧的那个点
                    DetectHistory second = history[(index + 2) % recordLength];
                    DetectHistory three = history[lastIndex];

                    double lastXPixels = history[lastIndex].xPixels; //上一帧索引

                    double oneFrameUseTime = time * 1.0 - history[lastIndex].time;
                    double threeFrameUseTime = time * 1.0 - first.time;  //四帧三个间隔用时

                    double speed = Math.abs((xPixel - lastXPixels) / (oneFrameUseTime));
                    detectHistory.speed = speed;
                   // System.out.println(STR. "\{ time },四个帧用时:\{ threeFrameUseTime },相邻速度:\{ speed },相邻距离:\{ xPixel }" );

                    if (speed > 50) {
                        System.out.println("bug");
                    }

                    if (skipFrame == 0 && (oneFrameUseTime > 20 || threeFrameUseTime > 50)) {
                        //遇到过两帧间隔几百毫秒的，这里遇到太离谱的就直接忽略调
                        skipFrame = recordLength - 2;
                    } else if (skipFrame > 0) {
                        skipFrame--;
                    } else {

                        boolean wantShot = false;
                        boolean inRange = false;
                        double needTime = Math.abs(xPixel / speed);
                        if (cx > location.x && cx < (location.x + location.width) && cy > location.y && cy < (location.y + location.height * 2)) {
                            inRange = true;
                        }

                        if (xPixel > 0) {
                            if (second.xPixels > xPixel && first.xPixels > second.xPixels && second.xPixels > three.xPixels) {
                                wantShot = true;
                            } else if (second.xPixels > xPixel && needTime < 20) {
                                wantShot = true;
                            }
                        } else {
                            if (second.xPixels < xPixel && first.xPixels < second.xPixels && second.xPixels < three.xPixels) {
                                wantShot = true;
                            } else if (second.xPixels > xPixel && needTime < 20) {
                                wantShot = true;
                            }
                        }

                        //根据速率判断多久后接管， 速度快的话提早接管，速度一般的话晚点接管


                        if (wantShot && needTime < 100) {
                            //可能想要射击，但是还不在目标身上，预测一下大概多少毫秒后会定位到人身上

                            //System.out.println("预估时间：" + needTime);

                            //把像素移动换算成鼠标移动
                            double firstXPixels = first.xPixels;
                            double aveSpeed = speed;
                            //先算一下过去四个点累计的平均速度 2个点算有点抖动，四个点还可以
                            if ((xPixel >= 0.0 && firstXPixels >= 0.0) || (xPixel < 0.0 && firstXPixels < 0.0)) {
                                aveSpeed = Math.abs((xPixel - firstXPixels) / (time * 1.0 - first.time));
                            }

                            //计算大概多少毫秒后会移动到人物身上
                            needTime = Math.abs(xPixel / aveSpeed);
                            //System.out.println("速度：" + aveSpeed + "," + needTime + "毫秒后遇上");

                            //这里还需要修改，一定速度下才给辅助，
                            if (needTime < 20 && aveSpeed > 0.5) {
    /*
                        考虑到最新的一帧画面是对几毫秒前数据的响应结果，
                        也就是说现在可能还有几毫秒的的累计偏移游戏引擎已经收到了，但是还没有渲染到画面上，
                        假设这个时间跨度是5毫秒
                        也就是说，我应该在经过needTime-5后开始接管鼠标移动，给一个正向或者反向的作用力
                        比如needTime是15  5毫秒后开始接管输入， 所有移动都给予一个反方向作用力，并且提供一个自己的水平移动里
                        * */

                                //开启一次控制
                                RunTimeConfig.noAllowMouseController = true;
                                int index1 = Math.max(location.width, 10);
                                int index2 = Math.max(location.width, 10);
                                int repairX = (int) (7 * aveSpeed * 1.5);

                                repairX = xPixel > 0 ? -repairX : repairX;

                                //施加一个相反的坐标量

                               // System.out.println(STR. "修补x位移量：\{ repairX }" );

                               // MouseHelpWrapper.mouseMove.invokeExact(repairX, 0);
                                //detect.detectOnce();
                                double[] doubles = detect.positionCenter(300);
                               // System.out.println(STR."实际为定位x移动量\{doubles[0]},有误差的平均速度:\{aveSpeed}");

                                if (false) {
                                    //System.out.println("进入设计窗口,距离:"+xPixel);
                                    MouseHelpWrapper.mouseLeftPress.invoke();
                                    // Thread.sleep(1);
                                    //System.out.println(firstP+","+secondP);
                                    MouseHelpWrapper.mouseLeftRelease.invoke();
                                }

                                //Thread.sleep(1000);

                                RunTimeConfig.noAllowMouseController = false;

                                //睡眠一会，交给用户控制
                                Thread.sleep(500);
                            }

                        }


                    }


                }


                position = position == (recordLength - 1) ? 0 : position + 1;
                //           boolean track = mouseListener.track();
//                if (track) {
//                    Thread.sleep(1);
//                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
//                lock.lock();
//                try {
//                    condition.signalAll();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    lock.unlock();
//                }
            }
        }

    }


}
