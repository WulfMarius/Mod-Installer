package me.wulfmarius.modinstaller;

import me.wulfmarius.modinstaller.repository.*;

public class Resolution {

    private ModDefinitions install = new ModDefinitions();
    private Installations uninstall = new Installations();

    private ModDependencies missingDependencies;
    private ModDependencies unresolvableDependencies;

    public void addInstall(ModDefinition modDefinition) {
        this.install.addModDefinition(modDefinition);
    }

    public void addUninstalls(Iterable<Installation> uninstalls) {
        this.uninstall.addInstallations(uninstalls);
    }

    public boolean containsUninstall(Installation installation) {
        return this.uninstall.contains(installation);
    }

    public ModDefinitions getInstall() {
        return this.install;
    }

    public ModDependencies getMissingDependencies() {
        return this.missingDependencies;
    }

    public Installations getUninstall() {
        return this.uninstall;
    }

    public ModDependencies getUnresolvableDependencies() {
        return this.unresolvableDependencies;
    }

    public boolean hasMissingDependencies() {
        return this.missingDependencies != null && !this.missingDependencies.isEmpty();
    }

    public boolean hasUnresolvableDependencies() {
        return this.unresolvableDependencies != null && !this.unresolvableDependencies.isEmpty();
    }

    public boolean isEmpty() {
        return this.install.isEmpty();
    }

    public boolean isErroneous() {
        return this.hasMissingDependencies() || this.hasUnresolvableDependencies();
    }

    public void setInstall(ModDefinitions install) {
        this.install = install;
    }

    public void setMissingDependencies(ModDependencies missingDependencies) {
        this.missingDependencies = missingDependencies;
    }

    public void setUninstall(Installations uninstall) {
        this.uninstall = uninstall;
    }

    public void setUnresolvableDependencies(ModDependencies unresolvableDependencies) {
        this.unresolvableDependencies = unresolvableDependencies;
    }
}
