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
 * @date 2022/2/3 17:15
 */
public class CaptureDLLWrapper {
    static {
        System.load(Config.CAPTURE_DLL_PATH);
    }

    public static MethodHandle capture_init = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("capture_init").get(),
            MethodType.methodType(int.class),
            FunctionDescriptor.of(CLinker.C_INT)
    );

    public static MethodHandle capture_release = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("capture_release").get(),
            MethodType.methodType(void.class),
            FunctionDescriptor.ofVoid()
    );

    public static MethodHandle capture_capture = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("capture").get(),
            MethodType.methodType(MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER)
    );




}
