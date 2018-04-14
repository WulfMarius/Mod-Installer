package me.wulfmarius.modinstaller;

public class Asset {

    private String url;
    private String targetDirectory;
    private String zipDirectory;
    private String type;

    public String getTargetDirectory() {
        return this.targetDirectory;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public String getZipDirectory() {
        return this.zipDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setZipDirectory(String zipDirectory) {
        this.zipDirectory = zipDirectory;
    }
}
