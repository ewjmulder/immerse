package com.programyourhome.immerse.domain.audio.soundcard;

import java.io.Serializable;

import javax.sound.sampled.Mixer;

import com.programyourhome.immerse.domain.Serialization;

/**
 * The mixer information of a sound card.
 * Has the same fields as the Java Sound Mixer.Info, but with a builder.
 */
public class MixerInfo implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private String name;
    private String description;
    private String vendor;
    private String version;

    private MixerInfo() {
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getVersion() {
        return this.version;
    }

    public MixerInfo fromJavaSoundMixerInfo(Mixer.Info mixerInfo) {
        return builder()
                .name(mixerInfo.getName())
                .description(mixerInfo.getDescription())
                .vendor(mixerInfo.getVendor())
                .version(mixerInfo.getVersion())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final MixerInfo mixerInfo;

        public Builder() {
            this.mixerInfo = new MixerInfo();
        }

        public Builder name(String name) {
            this.mixerInfo.name = name;
            return this;
        }

        public Builder description(String description) {
            this.mixerInfo.description = description;
            return this;
        }

        public Builder vendor(String vendor) {
            this.mixerInfo.vendor = vendor;
            return this;
        }

        public Builder version(String version) {
            this.mixerInfo.version = version;
            return this;
        }

        public MixerInfo build() {
            return this.mixerInfo;
        }

    }

}
