package com.programyourhome.immerse.network.client;

import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.util.TestData.room;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;
import static com.programyourhome.immerse.toolbox.util.TestData.soundCard;
import static com.programyourhome.immerse.toolbox.util.TestData.speaker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

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
import com.programyourhome.immerse.toolbox.audio.resource.UdpAudioResource;

public class TestNetworkClientLocalLaptopUDP {

    private static final int UDP_PORT = 43512;
    private static final int PACKET_SIZE = 100;
    private static final String START_MESSAGE = "start";

    public static void main(String[] args) throws Exception {
        new Thread(() -> openMicUdp()).start();

        Speaker speaker1 = speaker(1, 0, 10, 10);
        Speaker speaker2 = speaker(2, 10, 10, 10);
        Room room = room(speaker1, speaker2);

        SoundCard soundCard1 = soundCard(1, "pci-0000:00:1f.3", speaker1, speaker2);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        ImmerseAudioFormat format = ImmerseAudioFormat.fromJavaAudioFormat(new AudioFormat(44100, 16, 1, true, false));
        Scenario scenario = scenario(room,
                settings(UdpAudioResource.udp(InetAddress.getLocalHost(), UDP_PORT, PACKET_SIZE, format, START_MESSAGE),
                        fixed(5, 10, 10), fixed(5, 5, 5), fixed(fixedSpeakerVolumeRatios), fractional(), ForeverPlayback.forever()));

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

    private static void openMicUdp() {
        try {
            {
                final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                final TargetDataLine line = AudioSystem.getTargetDataLine(format, AudioSystem.getMixerInfo()[7]);
                // final TargetDataLine line = AudioSystem.getTargetDataLine(format);
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
            }
            System.exit(0);

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
            // final TargetDataLine line = AudioSystem.getTargetDataLine(format);
            line.open(format);
            line.start();
            final AudioInputStream micInputStream = new AudioInputStream(line);
            System.out.println("Mic started, sending UDP packages");

            while (true) {
                int amountNeeded = PACKET_SIZE;
                int totalAmountRead = 0;
                while (totalAmountRead < amountNeeded) {
                    // System.out.println(System.currentTimeMillis() + " - just before mic read");
                    // System.out.println(System.currentTimeMillis() + " - line available: " + line.available());
                    int amountRead = micInputStream.read(buffer, totalAmountRead, amountNeeded - totalAmountRead);
                    // System.out.println(System.currentTimeMillis() + " - just after mic read");
                    if (amountRead == -1) {
                        throw new IllegalArgumentException("Mic stream closed");
                    } else {
                        totalAmountRead += amountRead;
                    }
                }

                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                // System.out.println(System.currentTimeMillis() + " - packet sent");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}