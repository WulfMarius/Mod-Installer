package me.wulfmarius.modinstaller;

import java.io.*;
import java.util.Arrays;

public class JarStarter {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(Arrays.toString(args));

        String javaHome = System.getProperty("java.home");
        System.out.println("java.home = " + javaHome);

        String userDir = System.getProperty("user.dir");
        System.out.println("user.dir = " + userDir);

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(userDir));

        String command = "\"" + javaHome + File.separator + "bin" + File.separator + "java.exe\"";
        System.out.println("command = " + command);
        builder.command(command, "-jar", "mod-installer.jar");

        builder.inheritIO();

        Process process = builder.start();
        System.out.println(process);

        Thread.sleep(1000);
    }
}
