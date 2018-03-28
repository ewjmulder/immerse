package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FileAudioResource implements AudioResource {

    private final File file;

    protected FileAudioResource(String path) {
        this(new File(path));
    }

    protected FileAudioResource(File file) {
        this.file = file;
        if (!this.file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exist.");
        }
    }

    @Override
    public AudioInputStream getAudioStream() throws IOException {
        try {
            return AudioSystem.getAudioInputStream(this.file);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/test/resources/clip-10ms.wav"));
        System.out.println(audioInputStream.getFrameLength());
        try {
            Field field = AudioInputStream.class.getDeclaredField("frameLength");
            field.setAccessible(true);
            field.set(audioInputStream, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(audioInputStream.getFrameLength());

        for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) {
            System.out.println("Mixer: " + thisMixerInfo.getDescription() +
                    " [" + thisMixerInfo.getName() + "]");
            Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
            for (Line.Info thisLineInfo : thisMixer.getSourceLineInfo()) {
                if (thisLineInfo.getLineClass().getName().equals(
                        "javax.sound.sampled.Port")) {
                    Line thisLine = thisMixer.getLine(thisLineInfo);
                    thisLine.open();
                    System.out.println(" Source Port: "
                            + thisLineInfo.toString());
                    // for (Control thisControl : thisLine.getControls()) {
                    // System.out.println(AnalyzeControl(thisControl));
                    // }
                    thisLine.close();
                }
            }
            // for (Line.Info thisLineInfo : thisMixer.getTargetLineInfo()) {
            // if (thisLineInfo.getLineClass().getName().equals(
            // "javax.sound.sampled.Port")) {
            // Line thisLine = thisMixer.getLine(thisLineInfo);
            // thisLine.open();
            // System.out.println(" Target Port: "
            // + thisLineInfo.toString());
            // for (Control thisControl : thisLine.getControls()) {
            // System.out.println(AnalyzeControl(thisControl));
            // }
            // thisLine.close();
            // }
            // }
        }

    }

}
