package org.example.csgo.wrapper;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;
import org.example.csgo.Config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author 胡帅博
 * @date 2022/2/3 17:17
 */
public class DectectWrapper {
    static {
        System.load(Config.YOLOV5_DETECT_DLL_PATH);
    }

    public static MethodHandle detecte_init = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detecte_init").get(),
            MethodType.methodType(int.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));


    public static MethodHandle detecte_inference = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detecte_inference").get(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class, int.class),
            FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT));


    public static MethodHandle detecte_release = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("detecte_release").get(),
            MethodType.methodType(void.class),
            FunctionDescriptor.ofVoid());

}
