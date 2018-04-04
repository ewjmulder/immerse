package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.audiostreaming.StreamUtil.optionalToStream;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import one.util.streamex.StreamEx;

// TODO: Javadoc: method order + OS dependent
public class SoundCardDetector {

    private static final Pattern MIXER_INFO_NAME_PATTERN = Pattern.compile(".*\\[plughw:([0-9]+),0\\]");

    private static final String[] PHYSICAL_PORT_COMMAND = { "find", "/dev/snd/by-path", "-type", "l", "-printf", "%f --> %l\\n" };
    private static final Pattern PHYSICAL_PORT_PATTERN = Pattern.compile("(.*) --> ../controlC([0-9]+)");

    private Map<String, Mixer.Info> physicalPortToSoundCardMapping;

    // TODO: test for sound card index 10 or higher
    // TODO: rewrite to nice Java8 style (possibly use the more powerful stream lib (streamex), if that is not too big of a dependency) -> seems fine!
    public void detectSoundCards() throws IOException {
        Map<Integer, Mixer.Info> javaSoundMixers = this.getJavaSoundCards();
        Map<Integer, String> physicalPorts = this.getPhysicalPorts();

        this.physicalPortToSoundCardMapping = javaSoundMixers.keySet().stream()
                .collect(Collectors.toMap(physicalPorts::get, javaSoundMixers::get));
    }

    public Mixer.Info getMixerInfo(String physicalPort) {
        return this.physicalPortToSoundCardMapping.get(physicalPort);
    }

    private Map<Integer, Mixer.Info> getJavaSoundCards() {
        return StreamUtil.sameKeyAndValue(AudioSystem.getMixerInfo())
                .flatMapKeys(this::matchOnMixerName)
                .toMap();
    }

    private StreamEx<Integer> matchOnMixerName(Mixer.Info mixerInfo) {
        Matcher matcher = MIXER_INFO_NAME_PATTERN.matcher(mixerInfo.getName());
        matcher.matches();
        return optionalToStream(ofNullable(parseInt(matcher.group(1))));
    }

    // OS specific solution, cause there is no Java API for this.
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
