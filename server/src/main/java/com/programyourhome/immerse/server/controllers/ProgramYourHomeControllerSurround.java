package com.programyourhome.immerse.server.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.immerse.audiostreaming.AudioStreaming;
import com.programyourhome.immerse.domain.ImmerseSettings;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.soundcard.MixerInfo;
import com.programyourhome.immerse.domain.audio.soundcard.PhysicalDeviceInfo;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardSpeakers;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.domain.audio.stopcriterium.NoStopCriterium;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.location.dynamic.HorizontalCircleDynamicLocation;
import com.programyourhome.immerse.domain.location.dynamic.FixedDynamicLocation;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.MaxSumNormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.OnlyClosestVolumeRatiosAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

@RestController
@RequestMapping("surround")
public class ProgramYourHomeControllerSurround {

    @Inject
    private AudioStreaming audioStreaming;

    @RequestMapping("test2")
    public void test2() throws FileNotFoundException {
        Room room = new Room("Test room", "");
        room.addSpeaker(new Speaker(1, "Speaker 1", "", 1, new Vector3D(0, 5, 10)));
        room.addSpeaker(new Speaker(2, "Speaker 2", "", 1, new Vector3D(10, 5, 10)));

        // TODO: get from DB config of some sort + let 'user' manage this via REST calls
        Set<SoundCard> soundCards = new HashSet<>(Arrays.asList(
                new SoundCard(1, "One", "", "/dev/1", new PhysicalDeviceInfo("NoName1", "Vendor1", "Product1", ""),
                        new MixerInfo("PCH [plughw:0,0]", "ALSA (http://www.alsa-project.org)",
                                "Direct Audio Device: HDA Intel PCH, ALC3235 Analog, ALC3235 Analog", "4.8.0-040800-generic"))));

        Map<Integer, SoundCardSpeakers> mapping = new HashMap<>();
        mapping.put(1, new SoundCardSpeakers(1, 2));
        SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration = new SoundCardToSpeakerConfiguration("Test", "", mapping);

        // AudioResource audioResource = AudioResource.fromInputStream(this.getClass().getResourceAsStream("/game-music-mono-44k.wav"));
        AudioResource audioResource = AudioResource.fromFile(new File("/tmp/game-music-mono-44k-2sec.wav"));
        Scenario scenario = new Scenario("Test", audioResource,
                new FixedDynamicLocation(new Vector3D(5, 5, 5)),
                new FixedDynamicLocation(new Vector3D(5, 6, 12)),
                new ImmerseSettings(new FieldOfHearingVolumeRatiosAlgorithm(45),
                        new MaxSumNormalizeAlgorithm(1), new NoStopCriterium()));
        this.audioStreaming.playScenario(room, scenario, soundCards, soundCardToSpeakerConfiguration);
    }

    @RequestMapping("test6/{speakerId}")
    public void test5(@PathVariable("speakerId") final int speakerId) {
        MixerInfo internal = new MixerInfo("PCH [plughw:0,0]", "ALSA (http://www.alsa-project.org)",
                "Direct Audio Device: HDA Intel PCH, ALC3235 Analog, ALC3235 Analog", "4.8.0-040800-generic");
        MixerInfo virtual71 = new MixerInfo("Set [plughw:2,0]", "ALSA (http://www.alsa-project.org)",
                "Direct Audio Device: C-Media USB Headphone Set, USB Audio, USB Audio", "4.8.0-040800-generic");
        MixerInfo threeD = new MixerInfo("Device [plughw:1,0]", "ALSA (http://www.alsa-project.org)",
                "Direct Audio Device: USB PnP Sound Device, USB Audio, USB Audio", "4.8.0-040800-generic");

        Room room = new Room("Desk test", "");
        room.addSpeaker(new Speaker(1, "Speaker 1", "", 1, new Vector3D(0, 0, 10)));
        room.addSpeaker(new Speaker(2, "Speaker 2", "", 1, new Vector3D(0, 10, 10)));
        room.addSpeaker(new Speaker(3, "Speaker 3", "", 1, new Vector3D(5, 10, 10)));
        room.addSpeaker(new Speaker(4, "Speaker 4", "", 1, new Vector3D(10, 10, 10)));
        room.addSpeaker(new Speaker(5, "Speaker 5", "", 1, new Vector3D(10, 0, 10)));
        // room.addSpeaker(new Speaker(6, "Speaker 6", "", 1, new Vector3D(5, 0, 10)));

        // Virtual 7.1 has left and right reversed!! (or cable??)
        // TODO: get from DB config of some sort + let 'user' manage this via REST calls
        Set<SoundCard> soundCards = new HashSet<>(Arrays.asList(
                new SoundCard(1, "One", "", "/dev/1", new PhysicalDeviceInfo("NoName1", "Vendor1", "Product1", ""), internal),
                new SoundCard(2, "Two", "", "/dev/2", new PhysicalDeviceInfo("NoName2", "Vendor2", "Product2", ""), virtual71),
                new SoundCard(3, "Three", "", "/dev/3", new PhysicalDeviceInfo("NoName3", "Vendor3", "Product3", ""), threeD)));

        Map<Integer, SoundCardSpeakers> mapping = new HashMap<>();
        mapping.put(1, new SoundCardSpeakers(1, 2));
        mapping.put(2, new SoundCardSpeakers(3, 4));
        mapping.put(3, new SoundCardSpeakers(5, 5));
        SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration = new SoundCardToSpeakerConfiguration("Test", "", mapping);

        DynamicLocation sourceLocation = new HorizontalCircleDynamicLocation(5, 5, 10, 0, 5, true, 14);
        // DynamicLocation sourceLocation = new StaticDynamicLocation(new Vector3D(0, 0, 10));

        double vol = 0;
        if (speakerId == 9) {
            vol = 1;
        }

        Map<Integer, Double> volumeMap = new HashMap<>();
        volumeMap.put(1, vol);
        volumeMap.put(2, vol);
        volumeMap.put(3, vol);
        volumeMap.put(4, vol);
        volumeMap.put(5, vol);
        volumeMap.put(6, vol);
        // Only this speaker.
        volumeMap.put(speakerId, 1.0);

        VolumeRatiosAlgorithm algo = new OnlyClosestVolumeRatiosAlgorithm();
        // SpeakerVolumesAlgorithm algo = new StaticSpeakerVolumesAlgorithm(new SpeakerVolumes(volumeMap));

        AudioResource audioResource = AudioResource.fromFile(new File("/tmp/game-music-mono-44k.wav"));
        Scenario scenario = new Scenario("Test", audioResource, new FixedDynamicLocation(
                new Vector3D(5, 5, 5)),
                sourceLocation,
                new ImmerseSettings(algo, new MaxSumNormalizeAlgorithm(1), new NoStopCriterium()));
        this.audioStreaming.playScenario(room, scenario, soundCards, soundCardToSpeakerConfiguration);
    }

    @RequestMapping("test8")
    public void test8() {
        Room room = new Room("Test room", "");
        room.addSpeaker(new Speaker(1, "Speaker 1", "", 1, new Vector3D(0, 0, 10)));
        room.addSpeaker(new Speaker(2, "Speaker 2", "", 1, new Vector3D(0, 5, 10)));
        room.addSpeaker(new Speaker(3, "Speaker 3", "", 1, new Vector3D(0, 10, 10)));
        room.addSpeaker(new Speaker(4, "Speaker 4", "", 1, new Vector3D(5, 10, 10)));
        room.addSpeaker(new Speaker(5, "Speaker 5", "", 1, new Vector3D(10, 10, 10)));
        room.addSpeaker(new Speaker(6, "Speaker 6", "", 1, new Vector3D(10, 5, 10)));
        room.addSpeaker(new Speaker(7, "Speaker 7", "", 1, new Vector3D(10, 0, 10)));
        room.addSpeaker(new Speaker(8, "Speaker 8", "", 1, new Vector3D(5, 0, 10)));

        // TODO: get from DB config of some sort + let 'user' manage this via REST calls
        Set<SoundCard> soundCards = new HashSet<>(Arrays.asList(
                new SoundCard(1, "One", "", "/dev/1", new PhysicalDeviceInfo("NoName1", "Vendor1", "Product1", ""), new MixerInfo("Mixer", "Vendor1", "", "")),
                new SoundCard(2, "Two", "", "/dev/2", new PhysicalDeviceInfo("NoName2", "Vendor2", "Product2", ""), new MixerInfo("Mixer", "Vendor2", "", "")),
                new SoundCard(3, "Three", "", "/dev/3", new PhysicalDeviceInfo("NoName3", "Vendor3", "Product3", ""),
                        new MixerInfo("Mixer", "Vendor3", "", "")),
                new SoundCard(4, "Four", "", "/dev/4", new PhysicalDeviceInfo("NoName4", "Vendor4", "Product4", ""),
                        new MixerInfo("Mixer", "Vendor4", "", ""))));

        Map<Integer, SoundCardSpeakers> mapping = new HashMap<>();
        mapping.put(1, new SoundCardSpeakers(1, 2));
        mapping.put(2, new SoundCardSpeakers(3, 4));
        mapping.put(3, new SoundCardSpeakers(5, 6));
        mapping.put(4, new SoundCardSpeakers(7, 8));
        SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration = new SoundCardToSpeakerConfiguration("Test", "", mapping);

        AudioResource audioResource = AudioResource.fromInputStream(this.getClass().getResourceAsStream("/game-music-mono-44k.wav"));
        Scenario scenario = new Scenario("Test", audioResource, new FixedDynamicLocation(
                new Vector3D(5, 5, 5)),
                new HorizontalCircleDynamicLocation(5, 5, 10, 0, 5, true, 14),
                new ImmerseSettings(new FieldOfHearingVolumeRatiosAlgorithm(45),
                        new MaxSumNormalizeAlgorithm(1), new NoStopCriterium()));
        this.audioStreaming.playScenario(room, scenario, soundCards, soundCardToSpeakerConfiguration);
    }

}
