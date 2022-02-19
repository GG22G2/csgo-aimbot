//package org.example.test;
//
//import jdk.incubator.foreign.CLinker;
//import jdk.incubator.foreign.FunctionDescriptor;
//import jdk.incubator.foreign.LibraryLookup;
//import jdk.incubator.foreign.MemoryAccess;
//import jdk.incubator.foreign.MemoryAddress;
//import jdk.incubator.foreign.MemoryHandles;
//import jdk.incubator.foreign.MemorySegment;
//import jdk.internal.foreign.MemoryAddressImpl;
//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.openjdk.jol.vm.VM;
//import sun.misc.Unsafe;
//
//import java.lang.invoke.MethodHandle;
//import java.lang.invoke.MethodType;
//import java.lang.reflect.Field;
//import java.nio.ByteBuffer;
//import java.nio.file.Path;
//import java.util.Arrays;
//
///**
// * @author 胡帅博
// * @date 2021/5/16 12:35
// */
//public class UnsafeTest{
//    public static void main(String[] args) {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        Path paths = Path.of("C:\\Users\\h6706\\source\\repos\\screenShot\\x64\\Release\\screenShot.dll");
//        MethodHandle getByteFromPoint = CLinker.getInstance().downcallHandle(
//                LibraryLookup.ofPath(paths).lookup("getByteFromPoint").get(),
//                MethodType.methodType(int.class, MemoryAddress.class),
//                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
//        );
//  /*      MethodHandle getIntFromPoint = CLinker.getInstance().downcallHandle(
//                LibraryLookup.ofPath(paths).lookup("getIntFromPoint").get(),
//                MethodType.methodType(int.class, MemoryAddress.class),
//                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
//        );*/
//
//        MethodHandle getIntFromPoint = CLinker.getInstance().downcallHandle(
//                LibraryLookup.ofPath(paths).lookup("getIntFromPoint").get(),
//                MethodType.methodType(int.class, byte[].class),
//                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
//        );
//
//
//        byte[] b1 = new byte[24];
//
//        long address = VM.current().addressOf(b1);
//
//        try {
//            Field theInternalUnsafe1 = Unsafe.class.getDeclaredField("theInternalUnsafe");
//            theInternalUnsafe1.setAccessible(true);
//            jdk.internal.misc.Unsafe theInternalUnsafe = (jdk.internal.misc.Unsafe) theInternalUnsafe1.get(null);
//
//            long ARRAY_BASE_OFFSET = theInternalUnsafe.arrayBaseOffset(byte[].class);
//            //b[4]设置为3 ,这里尝试一下java直接操作内存
//            theInternalUnsafe.putByte(null, VM.current().addressOf(b1) + ARRAY_BASE_OFFSET + 4, (byte) 3);
//
//            try {
//                b1[0] = 8;
//                b1[3] = 1;
//                // byte[]也是对象，他也有对象头， 8字节基本信息，4字节指针，4字节数组长度，所有ARRAY_BASE_OFFSET = 16
//                int result = (int) getByteFromPoint.invoke(new MemoryAddressImpl(null, address + ARRAY_BASE_OFFSET));
//                int result2 = (int) getIntFromPoint.invoke(new MemoryAddressImpl(null, address + ARRAY_BASE_OFFSET));
//                System.out.println(result);
//                System.out.println(result2);
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//
//            System.out.println(Arrays.toString(b1));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 这个获取地址的思路是,调用getInt，
//     * 获取array中偏移16字节位置的值，也就是从16位置开始取4字节，其实也就是获取到了Object[0]的值，肯定是一个地址
//     * <p>
//     * 但这个地址在开启指针压缩的jvm中是还需要再放大8倍才是实际地址
//     */
//    public static long getAddress(jdk.internal.misc.Unsafe theInternalUnsafe, Object target) {
//        Object[] array = new Object[1];
//        array[0] = target;
//        //这里其实有两个情况，如果jvm没有开启压缩制作，地址只占有4字节，也可以调用getInt
//        long anInt = theInternalUnsafe.getLong(array, 16);
//        /**
//         * 如果开启指针压缩，则上一步获取的地址并不是实际地址，
//         * 指针压缩是，内存被jvm按照8字节（不是8比特）分块，anInt就代表是第几块内存
//         *
//         * 所以左移3次，放大八倍就可以获得到实际内存地址，当然这里也可能不是3，只是目前在我的电脑上查看是3
//         *
//         * 至于为啥还要加个0，这个我也不清楚,但好像和调试有关
//         *
//         * */
//        anInt = 0 + (anInt << 3);
//        return anInt;
//    }
//}
