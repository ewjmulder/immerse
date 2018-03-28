package com.programyourhome.immerse.domain.audio.soundcard;

import javax.sound.sampled.Mixer;

public class MixerInfo extends Mixer.Info {

    // Making constructor public
    public MixerInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String vendor;
        private String description;
        private String version;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public MixerInfo build() {
            return new MixerInfo(this.name, this.vendor, this.description, this.version);
        }

    }

}
