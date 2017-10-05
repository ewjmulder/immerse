package com.programyourhome.immerse.audiostreaming;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.IOUtils;

import com.programyourhome.immerse.audiostreaming.model.Scenario;
import com.programyourhome.immerse.audiostreaming.model.soundcard.MixerInfo;
import com.programyourhome.immerse.audiostreaming.model.soundcard.SoundCard;
import com.programyourhome.immerse.audiostreaming.model.soundcard.SoundCardStream;
import com.programyourhome.immerse.audiostreaming.model.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.speakermatrix.SpeakerMatrix;
import com.programyourhome.immerse.speakermatrix.model.Room;
import com.programyourhome.immerse.speakermatrix.model.Scene;
import com.programyourhome.immerse.speakermatrix.model.SpeakerMatrixSettings;
import com.programyourhome.immerse.speakermatrix.model.SpeakerVolumes;
import com.programyourhome.immerse.speakermatrix.model.SurroundMode;
import com.programyourhome.immerse.speakermatrix.model.Vector3D;

public class ScenarioPlayer {

    // TODO: make dynamic based on hardware tests. Although 30 seems like a reasonable default.
    private static final int BUFFER_MILLIS = 30;

    // TODO: make configurable, for instance for refresh rate setting
    private static final int SLEEP_MILLIS = 5;

    private SpeakerMatrix speakerMatrix;
    private Set<SoundCard> soundCards;
    private Room room;
    private Scenario scenario;
    private SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration;
    private long startMillis;
    private AudioInputStream audioStream;
    private byte[] inputBuffer;
    private AudioFormat outputFormat;
    private Set<SoundCardStream> soundCardStreams;

    public ScenarioPlayer(SpeakerMatrix speakerMatrix, Set<SoundCard> soundCards, Room room, Scenario scenario,
            SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration) {
        this.speakerMatrix = speakerMatrix;
        this.soundCards = soundCards;
        this.room = room;
        this.scenario = scenario;
        this.soundCardToSpeakerConfiguration = soundCardToSpeakerConfiguration;
        // TODO: more validation here instead of in play?
        this.soundCardStreams = new HashSet<>();
    }

    public void play() {
        try {
            this.playWithException();
        } catch (IOException | LineUnavailableException e) {
            // TODO: define catch/log of threads
            throw new IllegalStateException("Exception occured during scenario play.", e);
        }
    }

    private void playWithException() throws IOException, LineUnavailableException {
        this.audioStream = this.scenario.getAudioResource().getAudioStream();
        this.inputBuffer = IOUtils.toByteArray(this.audioStream);
        // Create all soundcard streams.
        this.initializeStreams();
        // Fill the initial buffers (no playback yet).
        this.updateStreams();
        // Start playback!
        this.startStreams();
        while (!this.shouldStop()) {
            this.updateStreams();
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {}
        }
        // TODO: this should be done in a finally
        this.stopStreams();
    }

    private boolean shouldStop() {
        return this.soundCardStreams.stream().anyMatch(soundCardStream -> soundCardStream.isDone(this.inputBuffer))
                || this.scenario.getSettings().getStopCriterium().shouldStop();
    }

    private void initializeStreams() throws IOException, LineUnavailableException {
        // TODO: input format validation, like should it be mono?
        AudioFormat inputFormat = this.audioStream.getFormat();
        // Explicitly set to stereo to control each speaker individually.
        // TODO: use extra abstraction layer, like PyhAudioFormat in PYH project
        this.outputFormat = new AudioFormat(Encoding.PCM_SIGNED, inputFormat.getSampleRate(), inputFormat.getSampleSizeInBits(),
                2 /* Stereo = 2 channels */, 2 * (inputFormat.getSampleSizeInBits() / 8), inputFormat.getFrameRate(), true /* Big-endian */);

        for (SoundCard soundCard : this.soundCards) {
            SourceDataLine outputLine;
            // try {
            Mixer.Info systemMixerInfo = this.matchMixerInfo(soundCard.getMixerInfo());
            outputLine = AudioSystem.getSourceDataLine(this.outputFormat, systemMixerInfo);
            // } catch (IllegalArgumentException e) {
            // // In case an exception is thrown for this mixer, it's probably the default, so use that one.
            // // TODO: check on plughw[x,0] otherwise other problem... (OS specific?)
            // outputLine = AudioSystem.getSourceDataLine(this.outputFormat, null);
            // }
            outputLine.open();
            SoundCardStream soundCardStream = new SoundCardStream(inputFormat, outputLine,
                    this.soundCardToSpeakerConfiguration.getSoundCardSpeakers(soundCard.getId()));
            this.soundCardStreams.add(soundCardStream);
        }

    }

    /**
     * Unfortunately in the Java Sound API Mixer.Info objects are matched on object equality and equals() is final, so we have to
     * resort to some 'outside-of-class' equality matching.
     */
    // TODO: this will fail when there are 2 mixers with equal mixer info. To be replaced by the physical -> hw index mapping
    private Info matchMixerInfo(MixerInfo mixerInfo) {
        Info info = null;
        Info[] systemMixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info systemMixerInfo : systemMixerInfos) {
            if (systemMixerInfo.getName().equals(mixerInfo.getName())
                    && systemMixerInfo.getVendor().equals(mixerInfo.getVendor())
                    && systemMixerInfo.getDescription().equals(mixerInfo.getDescription())
                    && systemMixerInfo.getVersion().equals(mixerInfo.getVersion())) {
                info = systemMixerInfo;
                break;
            }
        }
        return info;
    }

    private void startStreams() throws IOException {
        // TODO: how accurate can the start millis be defines for all streams at once (in other words: how long does the loop below take?)
        this.startMillis = System.currentTimeMillis();
        // TODO: multi threading? (use wait/notify?)
        this.soundCardStreams.forEach(SoundCardStream::start);
        this.scenario.getSettings().getStopCriterium().audioStarted();
    }

    private void updateStreams() throws IOException {
        long millisSinceStart = System.currentTimeMillis() - this.startMillis;
        Vector3D listener = this.scenario.getListenerLocation().getLocation(millisSinceStart);
        Vector3D source = this.scenario.getSourceLocation().getLocation(millisSinceStart);
        SpeakerMatrixSettings settings = new SpeakerMatrixSettings(SurroundMode.ANGLE_ONLY);
        Scene scene = new Scene(this.room, listener, source, settings);
        SpeakerVolumes speakerVolumes = this.speakerMatrix.calculateSurroundSound(scene);
        // TODO: multi threading? (use wait/notify?)
        this.soundCardStreams.forEach(soundCardStream -> soundCardStream.update(this.inputBuffer, BUFFER_MILLIS, speakerVolumes));
    }

    private void stopStreams() throws IOException {
        // TODO: multi threading? (use wait/notify?)
        this.soundCardStreams.forEach(SoundCardStream::stop);
    }

}
