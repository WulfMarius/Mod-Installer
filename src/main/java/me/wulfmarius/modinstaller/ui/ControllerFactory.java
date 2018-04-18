package me.wulfmarius.modinstaller.ui;

import java.lang.reflect.Constructor;
import java.nio.file.Paths;

import javafx.util.Callback;
import me.wulfmarius.modinstaller.ModInstaller;

public class ControllerFactory implements Callback<Class<?>, Object> {

    public static final ControllerFactory CONTROLLER_FACTORY = new ControllerFactory();

    private final ModInstaller modInstaller = new ModInstaller(Paths.get("./mod-installer"));

    private ControllerFactory() {
        // hide singleton constructor
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
