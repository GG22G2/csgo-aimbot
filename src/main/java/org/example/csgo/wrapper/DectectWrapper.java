package org.example.csgo.wrapper;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;
import org.example.OpencvUtil;
import org.example.csgo.Config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author
 * @date 2022/2/3 17:17
 */
public class DectectWrapper {
    static {
       // OpencvUtil.init();
        System.load("G:\\kaifa_environment\\opencv\\opencv\\build\\x64\\vc15\\bin\\opencv_world452d.dll");
        System.load("G:\\kaifa_environment\\code\\clion\\tensorrtx\\yolov5\\cmake-build-release\\bin\\myplugins.dll");
        System.load(Config.YOLOV5_DETECT_DLL_PATH);
    }

    public static MethodHandle detecte_init = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detect_init").get(),
            MethodType.methodType(int.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));


    public static MethodHandle detecte_inference = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detect_inference").get(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class, int.class),
            FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT));


    public static MethodHandle detecte_release = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detect_release").get(),
            MethodType.methodType(void.class),
            FunctionDescriptor.ofVoid());

}
