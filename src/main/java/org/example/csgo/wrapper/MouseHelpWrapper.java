package org.example.csgo.wrapper;

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author
 * @date 2022/2/12 21:44
 */
public class MouseHelpWrapper {
    static {
        System.load("G:\\kaifa_environment\\code\\C\\mouseHelp\\x64\\Release\\mouseHelp.dll");
    }


    public static MethodHandle mouseMove = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("mouseMove").get(),
            MethodType.methodType(void.class, int.class, int.class),
            FunctionDescriptor.ofVoid(CLinker.C_INT, CLinker.C_INT)
    );

    public static MethodHandle listener_mouse_move = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("listenerMouseMove").get(),
            MethodType.methodType(void.class),
            FunctionDescriptor.ofVoid()
    );

    public static MethodHandle get_absolute_move = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("getAbsoluteMove").get(),
            MethodType.methodType(MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER)
    );

}
