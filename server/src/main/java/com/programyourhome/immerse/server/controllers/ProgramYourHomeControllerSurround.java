package com.programyourhome.immerse.server.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.immerse.audiostreaming.AudioStreaming;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.settings.AudioStreamingSettings;
import com.programyourhome.immerse.domain.audio.soundcard.MixerInfo;
import com.programyourhome.immerse.domain.audio.soundcard.PhysicalDeviceInfo;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardSpeakers;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.domain.audio.stopcriterium.NoStopCriterium;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.HorizontalCircleDynamicLocation;
import com.programyourhome.immerse.domain.location.dynamic.StaticDynamicLocation;
import com.programyourhome.immerse.domain.speakers.Speaker;

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
                new StaticDynamicLocation(new Vector3D(5, 5, 5)),
                new StaticDynamicLocation(new Vector3D(5, 6, 12)),
                new AudioStreamingSettings(new NoStopCriterium()));
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
        Scenario scenario = new Scenario("Test", audioResource, new StaticDynamicLocation(
                new Vector3D(5, 5, 5)),
                new HorizontalCircleDynamicLocation(5, 5, 10, 0, 5, true, 14),
                new AudioStreamingSettings(new NoStopCriterium()));
        this.audioStreaming.playScenario(room, scenario, soundCards, soundCardToSpeakerConfiguration);
    }

}
