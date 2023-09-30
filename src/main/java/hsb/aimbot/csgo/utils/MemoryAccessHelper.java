package hsb.aimbot.csgo.utils;

import java.lang.foreign.MemorySegment;

/**
 * @author 胡帅博
 * @date 2023/9/17 22:54
 */
public class MemoryAccessHelper {


    public static MemorySegment asSegment(MemorySegment zeroLengthSegment, int byteSize) {
        return   zeroLengthSegment.reinterpret(byteSize);
        //return MemorySegment.ofAddress(zeroLengthSegment.address(), byteSize, zeroLengthSegment.scope());
    }

}
