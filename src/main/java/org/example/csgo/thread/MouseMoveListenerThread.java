package org.example.csgo.thread;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import org.example.csgo.Tracks;
import org.example.csgo.wrapper.MouseHelpWrapper;

import static org.example.csgo.wrapper.MouseHelpWrapper.get_absolute_move;

/**
 * @author 胡帅博
 * @date 2022/2/12 17:41
 * <p>
 * 记录鼠标移动轨迹，
 */
public class MouseMoveListenerThread implements Runnable {

    Tracks tracks;

    public MouseMoveListenerThread(Tracks tracks) {
        this.tracks = tracks;
    }

    @Override
    public void run() {
        try {
            MouseHelpWrapper.listener_mouse_move.invokeExact();


            while (true) {
                long start = System.currentTimeMillis();
                MemoryAddress result = (MemoryAddress) get_absolute_move.invokeExact();

                MemorySegment memorySegment = result.asSegment(4 * 3, ResourceScope.globalScope());

                int x = MemoryAccess.getIntAtIndex(memorySegment, 0);
                int y = MemoryAccess.getIntAtIndex(memorySegment, 1);
                long ulButtons = 0xFFFFFFFFFFFFFFFFL & MemoryAccess.getIntAtIndex(memorySegment, 2);


//                int ulRawButtons = MemoryAccess.getIntAtIndex(memorySegment, 3);
//                int ulExtraInformation = MemoryAccess.getIntAtIndex(memorySegment, 4);
//                int usFlags = MemoryAccess.getIntAtIndex(memorySegment, 5);
//                int usButtonData = MemoryAccess.getIntAtIndex(memorySegment, 6);
//                int usButtonFlags = MemoryAccess.getIntAtIndex(memorySegment, 7);


   //             long time = System.currentTimeMillis();

                    System.out.println( x + "," + y + "," + ulButtons);

               // tracks.updateTrack(time, x, y, ulButtons);
//                synchronized (tracks) {
//                    tracks.notify();
//                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public int[] track() {
        return null;
    }


}




