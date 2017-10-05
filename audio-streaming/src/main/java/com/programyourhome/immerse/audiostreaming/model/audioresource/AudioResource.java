package com.programyourhome.immerse.audiostreaming.model.audioresource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;

public interface AudioResource {

    public AudioInputStream getAudioStream() throws IOException;

    public static AudioResource fromFilePath(String filePath) {
        return new FileAudioResource(filePath);
    }

    public static AudioResource fromFile(File file) {
        return new FileAudioResource(file);
    }

    public static AudioResource fromUrlString(String urlString) {
        return new UrlAudioResource(urlString);
    }

    public static AudioResource fromFile(URL url) {
        return new UrlAudioResource(url);
    }

    public static AudioResource fromInputStream(InputStream inputStream) {
        return new InputStreamAudioResource(inputStream);
    }

}
