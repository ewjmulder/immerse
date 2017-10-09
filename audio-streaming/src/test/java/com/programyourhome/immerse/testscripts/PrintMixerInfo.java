package com.programyourhome.immerse.testscripts;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

public class PrintMixerInfo {

    public static void main(String[] args) throws Exception {
        for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) {
            System.out.println(thisMixerInfo.getDescription() + " - " + thisMixerInfo.getName() + " - "
                    + thisMixerInfo.getVendor() + " - " + thisMixerInfo.getVersion());
            Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
            for (Line.Info thisLineInfo : thisMixer.getSourceLineInfo()) {
                if (thisLineInfo.getLineClass().getName().equals("javax.sound.sampled.Port")) {
                    Line thisLine = thisMixer.getLine(thisLineInfo);
                    thisLine.open();
                    System.out.println(" Source Port: " + thisLineInfo.toString());
                    thisLine.close();
                }
            }
        }
    }

}
