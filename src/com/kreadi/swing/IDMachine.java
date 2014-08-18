package com.kreadi.swing;

import java.util.Enumeration;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import java.io.File;

public class IDMachine {

    public static String get() {

        ComThread.InitMTA();
        try {
            ActiveXComponent wmi = new ActiveXComponent("winmgmts:\\\\.");
            Variant instances = wmi.invoke("InstancesOf", "Win32_BaseBoard");
            Enumeration<Variant> en = new EnumVariant(instances.getDispatch());
            while (en.hasMoreElements()) {
                ActiveXComponent bb = new ActiveXComponent(en.nextElement().getDispatch());
                return bb.getPropertyAsString("SerialNumber");
            }
        } finally {
            ComThread.Release();
        }
        return null;
    }

    public static byte[] mix(int max) {
        String s = get() + new File(".").getAbsolutePath();
        byte[] bytes = new byte[max];
        for (int i = 0; i < s.length(); i++) {
            int code=s.charAt(i);
            bytes[i % max] = (byte) (bytes[i % max] + code+ i%8);
        }
        return bytes;
    }

}
