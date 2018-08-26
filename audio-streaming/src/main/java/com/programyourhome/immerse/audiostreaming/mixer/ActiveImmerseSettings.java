package com.programyourhome.immerse.audiostreaming.mixer;

import java.util.UUID;

import com.programyourhome.immerse.domain.ImmerseSettings;
import com.programyourhome.immerse.domain.ImmerseSettings.TechnicalSettings;

/**
 * This class holds a reference to the 'active' Immerse settings, meaning the settings that should
 * be used for the currently running Immerse Mixer. The limitation is that there can be just one
 * Immerse mixer active per JVM, but that is a very reasonable one, since they will have to compete
 * for resources otherwise anyway.
 *
 * The design is not really very nice with the static access, but that prevents having to pass a reference
 * to the settings all over the place. And we don't want to introduce a whole framework just for that.
 * As some kind of precaution a resetCode is used so only the mixer that initialized the active settings
 * can reset them again and no other can just 'take over' and mess with the settings.
 */
public class ActiveImmerseSettings {

    private static ImmerseSettings settings;
    private static UUID resetCode;

    /**
     * Whether or not there are active settings.
     */
    public static boolean hasSettings() {
        return settings != null;
    }

    /**
     * Get the active settings (throws IllegalStateException if none present).
     */
    public static ImmerseSettings getSettings() {
        if (!hasSettings()) {
            throw new IllegalStateException("No instance of settings present");
        }
        return settings;
    }

    /**
     * Get the active technical settings (throws IllegalStateException if none present).
     */
    public static TechnicalSettings getTechnicalSettings() {
        return getSettings().getTechnicalSettings();
    }

    /**
     * Initializes the active settings (throws IllegalStateException if already present)
     */
    public static UUID init(ImmerseSettings newSettings) {
        if (hasSettings()) {
            throw new IllegalStateException("Already instance of settings present, reset first");
        }
        settings = newSettings;
        resetCode = UUID.randomUUID();
        return resetCode;
    }

    /**
     * Resets (clears) the active settings if the reset code matches.
     * Returns true if successful.
     * Returns false if the code did not match.
     * Throws IllegalStateException if no active settings are present
     */
    public static boolean reset(UUID code) {
        if (!hasSettings()) {
            throw new IllegalStateException("No instance of settings present");
        }
        boolean success = resetCode.equals(code);
        if (success) {
            settings = null;
            resetCode = null;
        }
        return success;
    }

}
