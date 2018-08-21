package com.programyourhome.immerse.audiostreaming.soundcard;

import static com.programyourhome.immerse.toolbox.util.StreamUtil.optionalToStream;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import com.programyourhome.immerse.audiostreaming.util.OSUtil;
import com.programyourhome.immerse.toolbox.util.StreamUtil;

import one.util.streamex.StreamEx;

/**
 * Class that can detect the mapping between a Mixer in the Java Sound API and a physical sound card in the OS.
 * This functionality is currently only supported in Linux.
 *
 * The algorithm works like this:
 * 1. Find the (primary) Java Sound mixers representing the sound cards attached to the system and extract their assigned index.
 * 2. Find the sound card mapping in the OS itself that links the physical device location to their index.
 * 3. Combine 1. and 2. to create a mapping from physical port description to Java Sound mixers.
 *
 * Usage of this class:
 * A. Users of this class should save the physical port description of a sound card and use it to request the mapped mixer info object.
 * B. The returned mixer info object can then be used to open audio streams in the Java Sound API, using the AudioSystem class.
 */
public class SoundCardDetector {

    // Each plughw:X,0 is the primary mixer of an individual physical sound card attached to the computer.
    private static final Pattern MIXER_INFO_NAME_PATTERN = Pattern.compile(".*\\[plughw:([0-9]+),0\\]");

    // Command to loop up the location --> index mapping of sound devices attached to the computer.
    private static final String[] PHYSICAL_PORT_COMMAND = { "find", "/dev/snd/by-path", "-type", "l", "-printf", "%f --> %l\\n" };
    // Each line in the output will look something like this: 'pci-0000:01:00.1 --> ../controlC1'
    private static final Pattern PHYSICAL_PORT_PATTERN = Pattern.compile("(.*) --> ../controlC([0-9]+)");

    private Map<String, Mixer.Info> physicalPortToSoundCardMapping;

    /**
     * Perform the detection algorithm that can map a physical port description to a Java Sound mixer info object.
     * NB: Only supported for Linux. Running on any other OS will throw an exception.
     */
    public void detectSoundCards() throws IOException {
        if (!OSUtil.isLinux()) {
            throw new IllegalStateException("Sound card detection is only supported in Linux");
        }
        Map<Integer, Mixer.Info> javaSoundMixers = this.getJavaSoundCards();
        Map<Integer, String> physicalPorts = this.getPhysicalPorts();

        this.physicalPortToSoundCardMapping = StreamEx.of(javaSoundMixers.keySet())
                .toMap(physicalPorts::get, javaSoundMixers::get);
    }

    /**
     * Return the Java Sound mixer info object that represents the sound card with the given physical port description.
     * Will return null if no mapping was found.
     * NB: First call detectSoundCards to fill the mapping.
     */
    public Mixer.Info getMixerInfo(String physicalPort) {
        return this.physicalPortToSoundCardMapping.get(physicalPort);
    }

    /**
     * Return a mapping between the OS index assigned to a sound card and the Java Sound mixer info object.
     */
    private Map<Integer, Mixer.Info> getJavaSoundCards() {
        return StreamUtil.sameKeyAndValue(AudioSystem.getMixerInfo())
                .flatMapKeys(this::matchOnMixerName)
                .toMap();
    }

    /**
     * Try to perform a match on the mixer name with the defined pattern. If there is a match (= primary mixer device),
     * the OS index of that sound card is extracted and returned as a stream with one element.
     * If there was no match (= secondary mixer device), an empty stream is returned.
     */
    private StreamEx<Integer> matchOnMixerName(Mixer.Info mixerInfo) {
        Matcher matcher = MIXER_INFO_NAME_PATTERN.matcher(mixerInfo.getName());
        Integer soundCardIndex = matcher.matches() ? parseInt(matcher.group(1)) : null;
        return optionalToStream(ofNullable(soundCardIndex));
    }

    /**
     * Determine the mapping between the OS index assigned to a sound card and the physical port description
     * of a sound card (mostly either pci-... or pci-...usb-...).
     */
    private Map<Integer, String> getPhysicalPorts() throws IOException {
        Map<Integer, String> physicalPorts = new HashMap<>();

        Process process = Runtime.getRuntime().exec(PHYSICAL_PORT_COMMAND);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = PHYSICAL_PORT_PATTERN.matcher(line);
            if (matcher.matches()) {
                String physicalName = matcher.group(1);
                int soundCardIndex = Integer.parseInt(matcher.group(2));
                physicalPorts.put(soundCardIndex, physicalName);
            } else {
                throw new IOException("Physical port line: '" + line + "' does not match pattern: '" + PHYSICAL_PORT_PATTERN + "'");
            }
        }
        return physicalPorts;
    }

}
