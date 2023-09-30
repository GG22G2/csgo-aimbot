package hsb.aimbot.test;

import hsb.aimbot.csgo.utils.MemoryAccessHelper;
import hsb.aimbot.csgo.wrapper.DectectWrapper;
import hsb.aimbot.csgo.wrapper.MouseHelpWrapper;
import hsb.aimbot.csgo.wrapper.WinCaptureWrapper;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author 胡帅博
 * @date 2023/9/17 23:23
 */
public class DllLoadTest {
    public static void main(String[] args) throws Throwable {
       // MemorySegment result = (MemorySegment) MouseHelpWrapper.get_absolute_move.invokeExact();
        //MemorySegment memorySegment = MemoryAccessHelper.asSegment(result, 4 * 3);

       // long ulButtons = 0xFFFFFFFFFFFFFFFFL & memorySegment.getAtIndex(ValueLayout.JAVA_INT, 2);



        System.out.println(WinCaptureWrapper.cudaFreeProxy);
    }
}
