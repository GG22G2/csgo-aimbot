package org.example.csgo.wrapper;

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author 胡帅博
 * @date 2022/2/12 21:44
 */
public class MouseHelpWrapper {
    static {
        System.load("D:\\kaifa_environment\\code\\C\\mouseHelp\\x64\\Release\\mouseHelp.dll");
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


    //罗技鼠标安装ghub后 可以使用的方式
    public static MethodHandle mouse_open = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("mouse_open").get(),
            MethodType.methodType(byte.class),
            FunctionDescriptor.of(CLinker.C_CHAR)
    );


    public static MethodHandle mouse_close = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("mouse_close").get(),
            MethodType.methodType(void.class),
            FunctionDescriptor.ofVoid()
    );


    public static MethodHandle mouse_move = CLinker.getInstance().downcallHandle(
            SymbolLookup.loaderLookup().lookup("mouse_move").get(),
            MethodType.methodType(void.class, byte.class, byte.class, byte.class),
            FunctionDescriptor.ofVoid(CLinker.C_CHAR, CLinker.C_CHAR, CLinker.C_CHAR)
    );


}
