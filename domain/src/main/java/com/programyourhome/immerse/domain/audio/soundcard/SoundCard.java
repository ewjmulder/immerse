package com.programyourhome.immerse.domain.audio.soundcard;

public class SoundCard {

    private int id;
    private String name;
    private String description;
    private String physicalPort;
    private PhysicalDeviceInfo physicalDeviceInfo;
    private MixerInfo mixerInfo;

    public SoundCard(int id, String name, String description, String physicalPort, PhysicalDeviceInfo physicalDeviceInfo, MixerInfo mixerInfo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.physicalPort = physicalPort;
        this.physicalDeviceInfo = physicalDeviceInfo;
        this.mixerInfo = mixerInfo;
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

}
