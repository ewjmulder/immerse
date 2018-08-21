package com.programyourhome.immerse.audiostreaming.mixer;

import java.util.UUID;

//TODO: javadoc
public class ActiveImmerseSettings {

    private static ImmerseSettings settings;
    private static UUID resetCode;

    public static ImmerseSettings getSettings() {
        if (settings == null) {
            throw new IllegalStateException("No instance of settings present");
        }
        return settings;
    }

    public static UUID init(ImmerseMixer mixer) {
        if (settings != null) {
            throw new IllegalStateException("Already instance of settings present, reset first");
        }
        settings = mixer.getSettings();
        resetCode = UUID.randomUUID();
        return resetCode;
    }

    public static boolean reset(UUID code) {
        boolean success = false;
        if (settings != null && resetCode.equals(code)) {
            settings = null;
            resetCode = null;
            success = true;
        }
        return success;
    }

}
