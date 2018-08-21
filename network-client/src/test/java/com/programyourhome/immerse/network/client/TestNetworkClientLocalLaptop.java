package com.programyourhome.immerse.network.client;

import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.util.TestData.room;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;
import static com.programyourhome.immerse.toolbox.util.TestData.soundCard;
import static com.programyourhome.immerse.toolbox.util.TestData.speaker;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseSettings;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.SampleRate;
import com.programyourhome.immerse.domain.format.SampleSize;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.toolbox.audio.playback.ForeverPlayback;
import com.programyourhome.immerse.toolbox.audio.resource.FileAudioResource;

public class TestNetworkClientLocalLaptop {

    public static void main(String[] args) {
        Speaker speaker1 = speaker(1, 0, 10, 10);
        Speaker speaker2 = speaker(2, 10, 10, 10);
        Room room = room(speaker1, speaker2);

        SoundCard soundCard1 = soundCard(1, "pci-0000:00:1f.3", speaker1, speaker2);
        // SoundCard soundCard1 = soundCard(1, "pci-0000:00:14.0-usb-0:6:1.0", speaker1, speaker2);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        ImmerseAudioFormat format = ImmerseAudioFormat.fromJavaAudioFormat(new AudioFormat(44100, 16, 1, true, false));
        Scenario scenario = scenario(room,
                settings(FileAudioResource.file(new File("/home/emulder/Downloads/audio/clapping.wav")), fixed(5, 10, 10), fixed(5, 5, 5),
                        fixed(fixedSpeakerVolumeRatios), fractional(), ForeverPlayback.forever()));
        // settings(UrlAudioResource.urlWithFormat("http://localhost:19161/adventures/mic-test", format, true), fixed(5, 10, 10), fixed(5, 5, 5),
        // fixed(fixedSpeakerVolumeRatios), fractional(), LoopPlayback.once()));

        ImmerseClient client = new ImmerseClient("localhost", 51515);

        ImmerseSettings settings = ImmerseSettings.builder()
                .room(room)
                .soundCards(new HashSet<>(Arrays.asList(soundCard1)))
                .outputFormat(outputFormat)
                .build();

        System.out.println(client.createMixer(settings));

        System.out.println(client.startMixer());

        UUID playbackId = client.playScenario(scenario).getResult();

        System.out.println(playbackId);

        // try {
        // Thread.sleep(10000);
        // } catch (InterruptedException e) {}
        //
        // System.out.println(client.stopPlayback(playbackId));
    }

}
