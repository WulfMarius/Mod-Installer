package me.wulfmarius.modinstaller.ui;

import java.util.Optional;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import me.wulfmarius.modinstaller.*;

public class BindingsFactory {

    public static ObservableValue<Boolean> createHasValueBinding(Property<?> property) {
        return Bindings.createBooleanBinding(() -> property.getValue() != null, property);
    }

    public static ObservableValue<Boolean> createIsNotEmptyBinding(Property<String> property) {
        return Bindings.createBooleanBinding(() -> property.getValue() != null && !property.getValue().isEmpty(), property);
    }

    public static ObservableValue<String> createModDefinitionDescriptionBinding(Property<ModDefinition> property) {
        return Bindings.createStringBinding(() -> get(property, ModDefinition::getDescription, null), property);
    }

    public static ObservableValue<Boolean> createModDefinitionInstallBinding(Property<ModDefinition> property,
            ModInstaller modInstaller) {
        return Bindings.createBooleanBinding(() -> get(property, modInstaller::isNoVersionInstalled, false), property);
    }

    public static ObservableValue<String> createModDefinitionNameBinding(Property<ModDefinition> property) {
        return Bindings.createStringBinding(() -> get(property, ModDefinition::getName, null), property);
    }

    public static ObservableValue<Boolean> createModDefinitionUninstallBinding(Property<ModDefinition> property,
            ModInstaller modInstaller) {
        return Bindings.createBooleanBinding(() -> get(property, modInstaller::isAnyVersionInstalled, false), property);
    }

    public static ObservableValue<Boolean> createModDefinitionUpdateBinding(Property<ModDefinition> property,
            ModInstaller modInstaller) {
        return Bindings.createBooleanBinding(() -> get(property, modInstaller::isOtherVersionInstalled, false), property);
    }

    public static ObservableValue<String> createModDefinitionURLBinding(Property<ModDefinition> property) {
        return Bindings.createStringBinding(() -> get(property, ModDefinition::getUrl, null), property);
    }

    public static ObservableValue<String> createModDefinitionVersionBinding(Property<ModDefinition> property) {
        return Bindings.createStringBinding(() -> get(property, ModDefinition::getVersion, null), property);
    }

    private static <T, V> V get(Property<T> property, Function<T, V> function, V defaultValue) {
        return Optional.ofNullable(property.getValue()).map(function).orElse(defaultValue);
    }
}
