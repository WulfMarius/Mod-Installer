package me.wulfmarius.modinstaller.ui;

import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.nio.file.*;

import javafx.util.Callback;
import me.wulfmarius.modinstaller.ModInstaller;

public class ControllerFactory implements Callback<Class<?>, Object> {

    public static final ControllerFactory CONTROLLER_FACTORY = new ControllerFactory();

    private final ModInstaller modInstaller;
    private final Path baseDirectory;

    private ControllerFactory() {
        try {
            this.baseDirectory = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            this.modInstaller = new ModInstaller(this.baseDirectory.resolve("mod-installer"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not determine current baseDirectory.");
        }
    }

    public static Path getBaseDirectory() {
        return CONTROLLER_FACTORY.baseDirectory;
    }

    public static boolean isInstallationDirectoryValid() {
        if (Files.exists(ControllerFactory.getBaseDirectory().resolve("TLD.exe"))) {
            return true;
        }

        if (Files.exists(ControllerFactory.getBaseDirectory().resolve("tld.app"))) {
            return true;
        }

        if (Files.exists(ControllerFactory.getBaseDirectory().resolve("tld.x86"))) {
            return true;
        }

        if (Files.exists(ControllerFactory.getBaseDirectory().resolve("tld.x86_64"))) {
            return true;
        }

        return false;

    }

    @Override
    public Object call(Class<?> controllerClass) {
        try {
            for (Constructor<?> eachConstructor : controllerClass.getConstructors()) {
                if (eachConstructor.getParameterCount() == 1 && eachConstructor.getParameterTypes()[0] == ModInstaller.class) {
                    return eachConstructor.newInstance(this.modInstaller);
                }
            }

            return controllerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
