package com.kreadi.swing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LOG {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
    public static String stdoutFile = "stdout.log";
    public static String stderrFile = "stderr.log";

    private static void append(String filename, String msg) {
        append(filename, msg, false);
    }

    private static void append(String filename, String msg, boolean error) {
        if (filename != null) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
                out.println(msg);
                out.flush();
            } catch (IOException e) {
            }
        } else {
            if (error) {
                System.err.println(msg);
            } else {
                System.out.println(msg);
            }
        }
    }

    public static void log(String msg) {
        append(stdoutFile, sdf.format(new Date()) + " " + msg);
    }

    public static void err(String err) {
        append(stderrFile, sdf.format(new Date()) + " " + err, true);
    }

    public static void err(Exception ex) {
        err("", ex);
    }

    public static void err(String err, Exception ex) {
        append(stderrFile, sdf.format(new Date()) + (err.length() > 0 ? (" " + err) : " ") + ex.getLocalizedMessage().trim().replace("\\s+", " "), true);
        StackTraceElement[] stack = ex.getStackTrace();
        String classname;
        for (StackTraceElement t : stack) {
            classname = t.getClassName();
            append(stderrFile, "\t" + t.getClassName() + " -> " + classname + " : " + t.getLineNumber(), true);
        }
    }

}
