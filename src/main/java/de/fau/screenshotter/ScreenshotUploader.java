package de.fau.screenshotter;

public interface ScreenshotUploader {
    public String upload(String filename) throws Exception;
    public String prefetchUrl(String filename);
}