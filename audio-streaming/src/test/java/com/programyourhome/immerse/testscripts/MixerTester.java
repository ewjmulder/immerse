package com.programyourhome.immerse.testscripts;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class MixerTester {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < AudioSystem.getMixerInfo().length; i++) {
            Mixer.Info info = AudioSystem.getMixerInfo()[i];
            System.out.println(info.getName());
            for (int k : new int[] { 8, 11, 22, 32, 44, 48 }) {
                File url = new File("/home/emulder/Downloads/game-music-mono-" + k + "k.wav");
                final AudioInputStream stream = AudioSystem.getAudioInputStream(url);

                AudioFormat inputFormat = stream.getFormat();
                // Explicitly set to stereo to control each speaker individually.
                AudioFormat outputFormat = new AudioFormat(inputFormat.getEncoding(), inputFormat.getSampleRate(), inputFormat.getSampleSizeInBits(),
                        2, 4, inputFormat.getFrameRate(), inputFormat.isBigEndian());
                try {
                    SourceDataLine line = AudioSystem.getSourceDataLine(outputFormat, info);
                    line.open();
                    line.close();
                    System.out.println(" OK : [" + i + "] - " + k);
                } catch (IllegalArgumentException e) {
                    System.out.println("FAIL: [" + i + "] - " + k);
                }
            }
        }
    }

}
