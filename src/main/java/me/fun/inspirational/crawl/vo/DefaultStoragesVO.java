package me.fun.inspirational.crawl.vo;

public class DefaultStoragesVO {
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getImages() {
        return images;
    }

    public void setImages(byte[] images) {
        this.images = images;
    }

    private String filePath;//image to store images
    private byte[] images;
}
