package com.programyourhome.immerse.testscripts.scenarios;

import static com.programyourhome.immerse.toolbox.audio.playback.ForeverPlayback.forever;
import static com.programyourhome.immerse.toolbox.audio.resource.FileAudioResource.file;
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
import java.util.stream.Collectors;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.domain.ImmerseSettings;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.SampleRate;
import com.programyourhome.immerse.domain.format.SampleSize;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class TesterSparrenBurcht {

    // private static final String CHILL_PINE = "/home/ubuntu/sandbox/ChillingMusic_Loud.wav";
    // private static final String CLAPPING_PINE = "/home/ubuntu/sandbox/clapping-louder-long.wav";
    // private static final String VOICE_PINE = "/home/ubuntu/sandbox/voice-12-sec.wav";
    // private static final String CHILL = "/home/emulder/Downloads/ChillingMusic.wav";
    // private static final String BASS = "/home/emulder/Downloads/doublebass.wav";
    // // TODO: find out why clapping has a tick but clapping-saved (load+save by Audacity) does not.
    // private static final String CLAPPING = "/home/emulder/Downloads/clapping.wav";
    // private static final String VOICE = "/home/emulder/Downloads/voice.wav";

    private static final String SPIRAL = "/home/ubuntu/audio/spiral.wav";

    public static void main(String[] args) throws Exception {
        Speaker speaker1 = speaker(1, 0, 366, 250);
        Speaker speaker2 = speaker(2, 122, 366, 250);
        Speaker speaker3 = speaker(3, 244, 366, 250);
        Speaker speaker4 = speaker(4, 366, 366, 250);
        Speaker speaker5 = speaker(5, 366, 244, 250);
        Speaker speaker6 = speaker(6, 366, 122, 250);
        Speaker speaker7 = speaker(7, 366, 0, 250);
        Speaker speaker8 = speaker(8, 244, 0, 250);
        Speaker speaker9 = speaker(9, 122, 0, 250);
        Speaker speaker10 = speaker(10, 0, 0, 250);
        Speaker speaker11 = speaker(11, 0, 122, 250);
        Speaker speaker12 = speaker(12, 0, 244, 250);
        Room room = room(speaker1, speaker2, speaker3, speaker4, speaker5, speaker6, speaker7, speaker8, speaker9, speaker10, speaker11, speaker12);

        // TODO: convenience class around key frames?
        // TODO: key frames options loop or once
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 0, 250));
        keyFrames.put(3_000L, new Vector3D(0, 366, 250));
        keyFrames.put(6_000L, new Vector3D(366, 366, 250));
        keyFrames.put(9_000L, new Vector3D(366, 0, 250));
        keyFrames.put(12_000L, new Vector3D(0, 0, 250));

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0))); // speaker.getId() == 2 ? 1.0 : 0.0)));
        Scenario scenario1 = scenario(settings(file(SPIRAL),
                fixed(fixedSpeakerVolumeRatios), maxSum(1), forever()));

        // Scenario scenario2 = scenario(settings(file(VOICE_PINE), keyFrames(keyFrames), fixed(180, 180, 150),
        // fieldOfHearing(45), maxSum(1), forever()));
        // fixed(fixedSpeakerVolumeRatios), fractional(), forever()));

        SoundCard soundCard1 = soundCard(1, "platform-1c1b000.ehci1-controller-usb-0:1.2:1.0", speaker9, speaker6);
        SoundCard soundCard2 = soundCard(2, "platform-1c1b000.ehci1-controller-usb-0:1.3:1.0", speaker10, speaker11);
        SoundCard soundCard3 = soundCard(3, "platform-1c1b000.ehci1-controller-usb-0:1.4:1.0", speaker7, speaker4);
        SoundCard soundCard4 = soundCard(4, "platform-1c1b000.ehci1-controller-usb-0:1.1.2:1.0", speaker1, speaker12);
        SoundCard soundCard5 = soundCard(5, "platform-1c1b000.ehci1-controller-usb-0:1.1.3:1.0", speaker8, speaker5);
        // Note: this sound card has left and right switched compared to all other sound cards in use
        SoundCard soundCard6 = soundCard(6, "platform-1c1b000.ehci1-controller-usb-0:1.1.4:1.0", speaker3, speaker2);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseSettings settings = ImmerseSettings.builder()
                .room(room)
                .soundCards(new HashSet<>(Arrays.asList(soundCard1, soundCard2, soundCard3, soundCard4, soundCard5, soundCard6)))
                .outputFormat(outputFormat)
                .build();

        ImmerseMixer mixer = new ImmerseMixer(settings);

        mixer.initialize();
        mixer.start();

        mixer.playScenario(scenario1);

        // for (int i = 0; i < 1; i++) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException e) {}
        //
        // mixer.playScenario(scenario2);
        // }

    }

}
