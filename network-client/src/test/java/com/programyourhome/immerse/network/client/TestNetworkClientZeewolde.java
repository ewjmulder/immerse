package com.programyourhome.immerse.network.client;

import static com.programyourhome.immerse.toolbox.audio.playback.ForeverPlayback.forever;
import static com.programyourhome.immerse.toolbox.audio.resource.UrlAudioResource.urlWithType;
import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.KeyFramesDynamicLocation.keyFrames;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.MaxSumNormalizeAlgorithm.maxSum;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.util.TestData.room;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;
import static com.programyourhome.immerse.toolbox.util.TestData.soundCard;
import static com.programyourhome.immerse.toolbox.util.TestData.speaker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseSettings;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.resource.AudioFileType;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.SampleRate;
import com.programyourhome.immerse.domain.format.SampleSize;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class TestNetworkClientZeewolde {

    public static void main(String[] args) {
        Speaker speaker1 = speaker(1, 0, 0, 80);
        Speaker speaker2 = speaker(2, 0, 100, 80);
        Speaker speaker3 = speaker(3, 200, 120, 80);
        Speaker speaker4 = speaker(4, 200, 50, 80);
        Room room = room(UUID.fromString("3e8b23c8-f83b-497f-b0ce-fcfcc6a39ced"), speaker1, speaker2, speaker3, speaker4);

        SoundCard soundCard1 = soundCard(1, "platform-1c1b000.ehci1-controller-usb-0:1.2:1.0", speaker2, speaker1);
        SoundCard soundCard2 = soundCard(2, "platform-1c1b000.ehci1-controller-usb-0:1.4:1.0", speaker3, speaker4);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseClient client = new ImmerseClient("10.42.0.211", 51515);

        ImmerseSettings settings = ImmerseSettings.builder()
                .room(room)
                .soundCards(new HashSet<>(Arrays.asList(soundCard1, soundCard2)))
                .outputFormat(outputFormat)
                .build();

        System.out.println(client.createMixer(settings));

        System.out.println(client.startMixer());

        // TODO: convenience class around key frames?
        // TODO: key frames options loop or once
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 0, 80));
        keyFrames.put(3_000L, new Vector3D(0, 100, 80));
        keyFrames.put(6_000L, new Vector3D(200, 120, 80));
        keyFrames.put(9_000L, new Vector3D(200, 50, 80));
        keyFrames.put(12_000L, new Vector3D(0, 0, 80));

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        Scenario scenario = scenario(room,
                settings(urlWithType("http://10.42.0.1:19161/audio/chill", AudioFileType.WAVE), keyFrames(keyFrames), fixed(100, 60, 80),
                        fixed(fixedSpeakerVolumeRatios), maxSum(1), forever()));
        // fieldOfHearing(60), maxSum(1), forever()));

        System.out.println(client.playScenario(scenario));
    }

}
