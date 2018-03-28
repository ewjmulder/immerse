package com.programyourhome.immerse.domain.audio.soundcard;

import com.programyourhome.immerse.domain.speakers.Speaker;

public class SoundCard {

    private int id;
    private String name;
    private String description;
    private String physicalPort;
    private PhysicalDeviceInfo physicalDeviceInfo;
    private MixerInfo mixerInfo;
    private Speaker leftSpeaker;
    private Speaker rightSpeaker;

    private SoundCard() {
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPhysicalPort() {
        return this.physicalPort;
    }

    public PhysicalDeviceInfo getPhysicalDeviceInfo() {
        return this.physicalDeviceInfo;
    }

    public MixerInfo getMixerInfo() {
        return this.mixerInfo;
    }

    public Speaker getLeftSpeaker() {
        return this.leftSpeaker;
    }

    public Speaker getRightSpeaker() {
        return this.rightSpeaker;
    }

    @Override
    public String toString() {
        return this.id + " - " + this.name + " - " + this.physicalPort;
    }

    public Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SoundCard soundCard;

        public Builder() {
            this.soundCard = new SoundCard();
        }

        public Builder id(int id) {
            this.soundCard.id = id;
            return this;
        }

        public Builder name(String name) {
            this.soundCard.name = name;
            return this;
        }

        public Builder description(String description) {
            this.soundCard.description = description;
            return this;
        }

        public Builder physicalPort(String physicalPort) {
            this.soundCard.physicalPort = physicalPort;
            return this;
        }

        public Builder physicalDeviceInfo(PhysicalDeviceInfo physicalDeviceInfo) {
            this.soundCard.physicalDeviceInfo = physicalDeviceInfo;
            return this;
        }

        public Builder mixerInfo(MixerInfo mixerInfo) {
            this.soundCard.mixerInfo = mixerInfo;
            return this;
        }

        public Builder leftSpeaker(Speaker leftSpeaker) {
            this.soundCard.leftSpeaker = leftSpeaker;
            return this;
        }

        public Builder rightSpeaker(Speaker rightSpeaker) {
            this.soundCard.rightSpeaker = rightSpeaker;
            return this;
        }

        public SoundCard build() {
            return this.soundCard;
        }
    }

}
