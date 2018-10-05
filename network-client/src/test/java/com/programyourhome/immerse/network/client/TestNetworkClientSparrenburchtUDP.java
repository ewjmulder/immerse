package com.programyourhome.immerse.network.client;

import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.KeyFramesDynamicLocation.keyFrames;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm.fieldOfHearing;
import static com.programyourhome.immerse.toolbox.util.TestData.room;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;
import static com.programyourhome.immerse.toolbox.util.TestData.soundCard;
import static com.programyourhome.immerse.toolbox.util.TestData.speaker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

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
import com.programyourhome.immerse.toolbox.audio.playback.LoopPlayback;
import com.programyourhome.immerse.toolbox.audio.resource.UdpAudioResource;

public class TestNetworkClientSparrenburchtUDP {

    private static final int UDP_PORT = 43512;
    private static final int INTERNAL_MIC_CHUNK_SIZE = 180 * 45 * 2;
    private static final int EXTERNAL_MIC_CHUNK_SIZE = 20 * 45 * 2;
    private static final int PACKET_SIZE = 100;
    private static final String START_MESSAGE = "start";

    public static void main(String[] args) throws Exception {
        new Thread(() -> openMicUdp()).start();

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
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> speaker.getId() == 4 ? 0.5 : 0.0)));
        ImmerseAudioFormat format = ImmerseAudioFormat.fromJavaAudioFormat(new AudioFormat(44100, 16, 1, true, false));
        Scenario scenario = scenario(
                settings(UdpAudioResource.udp("192.168.0.101", UDP_PORT, EXTERNAL_MIC_CHUNK_SIZE, PACKET_SIZE, format, START_MESSAGE),
                        fieldOfHearing(room, keyFrames(keyFrames, true), fixed(5, 5, 5), 45), fractional(), LoopPlayback.once()));

        // Scenario scenario2 = scenario(room, settings(file(VOICE_PINE), keyFrames(keyFrames), fixed(180, 180, 150),
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

        ImmerseClient client = new ImmerseClient("192.168.0.106", 51515);

        ImmerseSettings settings = ImmerseSettings.builder()
                .room(room)
                .soundCards(new HashSet<>(Arrays.asList(soundCard1, soundCard2, soundCard3, soundCard4, soundCard5, soundCard6)))
                .outputFormat(outputFormat)
                .build();

        System.out.println(client.createMixer(settings));

        System.out.println(client.startMixer());

        UUID playbackId = client.playScenario(scenario).getResult();

        System.out.println(playbackId);
    }

    private static void openMicUdp() {
        try {
            DatagramSocket socket = new DatagramSocket(UDP_PORT);
            byte[] buffer = new byte[PACKET_SIZE];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Mic opened, waiting for start package");
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            if (message.equals(START_MESSAGE)) {
                System.out.println("Start message received: " + message);
            } else {
                throw new IllegalStateException("UDP packet received that did not match the start message: " + message);
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            final TargetDataLine line = AudioSystem.getTargetDataLine(format, AudioSystem.getMixerInfo()[7]);
            line.open(format);
            line.start();
            final AudioInputStream micInputStream = new AudioInputStream(line);
            System.out.println("Mic started, sending UDP packages");

            while (true) {
                int amountNeeded = PACKET_SIZE;
                int totalAmountRead = 0;
                while (totalAmountRead < amountNeeded) {
                    int amountRead = micInputStream.read(buffer, totalAmountRead, amountNeeded - totalAmountRead);
                    if (amountRead == -1) {
                        throw new IllegalArgumentException("Mic stream closed");
                    } else {
                        totalAmountRead += amountRead;
                    }
                }

                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Move test code somewhere in a util or so

    private static void testMic() throws IOException, LineUnavailableException {
        final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        final TargetDataLine line = AudioSystem.getTargetDataLine(format, AudioSystem.getMixerInfo()[7]);
        line.open(format);
        line.start();
        final AudioInputStream testMicInputStream = new AudioInputStream(line);

        testMicInputStream.read(new byte[testMicInputStream.available()]);
        int times = 200;
        double totalFramesRead = 0;
        for (int i = 0; i < times; i++) {
            byte[] frame = new byte[2];
            int framesReadInChunk = 0;
            boolean endOfChunk = false;
            while (!endOfChunk) {
                long start = System.nanoTime();
                testMicInputStream.read(frame);
                long end = System.nanoTime();
                framesReadInChunk++;
                // If reading a frame takes more than 1 milli, we've hit a chunk limit (cause it did not come out of a buffer)
                if ((end - start) / 1_000_000 > 1) {
                    endOfChunk = true;
                }
            }
            totalFramesRead += framesReadInChunk;
            System.out.println("# frames in chunk: " + framesReadInChunk);
            System.out.println("# ms in chunk: " + framesReadInChunk / 44.1);
        }
        System.out.println("# frames in " + times + " chunks: " + totalFramesRead);
        System.out.println("# avg ms in chunk: " + totalFramesRead / times / 44.1);
        System.exit(0);
    }

}
