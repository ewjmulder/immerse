package com.programyourhome.immerse.audiostreaming.mixer.step;

import static com.programyourhome.immerse.toolbox.util.StreamUtil.toMapFixedValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ActiveScenario;
import com.programyourhome.immerse.audiostreaming.soundcard.SoundCardStream;
import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;
import com.programyourhome.immerse.toolbox.util.StreamUtil;

import one.util.streamex.StreamEx;

/**
 * This class contains the algorithm at the heart of Immerse:
 * mixing all active scenario inputs into the right audio outputs.
 * This means performing the calculations to be able to refill the buffers for all sound card streams.
 * It represents one step in the mixer process and a new object should be used for each next step.
 *
 * Since this logic is the most important part of the system, this class and all it's methods are well
 * documented and described to preserve the knowledge about how the algorithm works and make it easier
 * to understand and debug it. Don't just read the Javadoc, but also check out the comments
 * inside the method code for more detailed information.
 */
public class MixerStep {

    private static final int DEFAULT_BUFFER_MILLIS = 30;

    // The scenarios that are currently active (in this step).
    private final Collection<ActiveScenario> stepActiveScenarios;
    // The sound card streams that are configured to receive output.
    private final Collection<SoundCardStream> soundCardStreams;
    // The output audio format.
    private final ImmerseAudioFormat outputFormat;
    // Keeps track of which scenarios should be removed after this step.
    private final Set<ActiveScenario> scenariosToRemove;
    // Keeps track of which scenarios should be restarted after this step.
    private final Set<ActiveScenario> scenariosToRestart;
    // The amount of frames we need to add to the buffer in this step.
    private final int amountOfFramesNeeded;

    /**
     * Create a mixer step with the needed info for the calculations.
     */
    public MixerStep(Collection<ActiveScenario> activeScenarios, Collection<SoundCardStream> soundCardStreams, ImmerseAudioFormat outputFormat) {
        if (!outputFormat.isOutput()) {
            throw new IllegalArgumentException("Output format must be of type 'output'");
        }
        this.soundCardStreams = soundCardStreams;
        this.outputFormat = outputFormat;
        this.scenariosToRemove = new HashSet<>();
        this.scenariosToRestart = new HashSet<>();
        this.amountOfFramesNeeded = this.calculateAmountOfFramesNeeded();

        // First, check if there are any scenarios that should be stopped. If so, add them to the scenarios to remove collection.
        // They will be stopped by the mixer and removed at the next step.
        StreamEx.of(activeScenarios)
                .filter(activeScenarino -> activeScenarino.getPlayback().shouldStop())
                .forEach(this.scenariosToRemove::add);
        // Make a copy of the provided active scenarios, so we can safely change the collection.
        this.stepActiveScenarios = new HashSet<>(activeScenarios);
        // Remove all scenarios to remove, so they will not be taken into account anymore in this step.
        this.stepActiveScenarios.removeAll(this.scenariosToRemove);
    }

    /**
     * Calculate the overall amount of streams needed by taking the max of the amount of frames needed
     * for all sounds card streams.
     */
    private int calculateAmountOfFramesNeeded() {
        // Gather the amount of frames needed for all sound card streams.
        List<Long> allFramesNeededAmounts = StreamEx.of(this.soundCardStreams)
                .map(stream -> stream.getAmountOfFramesNeeded(DEFAULT_BUFFER_MILLIS))
                .toList();
        // Calculate the min and max frames needed.
        long minFramesNeeded = Collections.min(allFramesNeededAmounts);
        long maxFramesNeeded = Collections.max(allFramesNeededAmounts);
        // Trace write the diff between max and min, since that is an indication of how well the sound card streams are in sync.
        Logger.trace("Diff between max and min amount of frames needed: {}", maxFramesNeeded - minFramesNeeded);
        // Since we have to keep in sync with all streams, we take the max frames needed as the amount needed for all streams.
        long amountOfFramesNeeded = maxFramesNeeded;
        // We want an int for easier use as array size and we know it will never be bigger than the limits of Integer.
        return (int) amountOfFramesNeeded;
    }

    public Set<ActiveScenario> getScenariosToRemove() {
        return this.scenariosToRemove;
    }

    public Set<ActiveScenario> getScenariosToRestart() {
        return this.scenariosToRestart;
    }

    public Map<SoundCardStream, byte[]> calculateBufferData() {
        if (this.amountOfFramesNeeded == 0) {
            // If no frames are needed, provide 0-length buffers for all sound card streams.
            return StreamUtil.toMapFixedValue(this.soundCardStreams, new byte[0]).toMap();
        } else if (this.stepActiveScenarios.isEmpty()) {
            // If there are no active scenarios, create buffers with just silence for all sound card streams.
            return this.createSilence();
        } else {
            // If there are active scenarios, perform the actual algorithm to calculate the buffers.
            return this.createAudioBuffers();
        }
    }

    private Map<SoundCardStream, byte[]> createSilence() {
        // Create an output array of the right size.
        byte[] silenceBuffer = new byte[this.amountOfFramesNeeded * this.outputFormat.getNumberOfBytesPerFrame()];
        // Fill the array with just 0's (meaning no amplitudes, so silence).
        Arrays.fill(silenceBuffer, (byte) 0);
        // Map all sound cards streams to the same silence buffer.
        return StreamUtil.toMapFixedValue(this.soundCardStreams, silenceBuffer).toMap();
    }

    private Map<SoundCardStream, byte[]> createAudioBuffers() {
        List<ScenarioResult> scenarioResults = this.calculateScenarioResults();

        if (scenarioResults.isEmpty()) {
            // If there were no scenarios left (because of input read errors), create silence.
            return this.createSilence();
        } else {
            // If there are scenarios left, calculate the actual output buffers.
            return this.calculateOutputBuffers(scenarioResults);
        }
    }

    /**
     * Calculate the scenario results by reading the samples from the input stream
     * and calculating the speaker volumes according to the scenario algorithms.
     */
    private List<ScenarioResult> calculateScenarioResults() {
        return StreamEx.of(this.stepActiveScenarios)
                // Read the samples from the input streams of the scenarios.
                .mapToEntry(this::readSamples)
                // Skip the empty results (in case of I/O error).
                .flatMapValues(StreamUtil::optionalToStream)
                // Calculate the speaker volumes according to the algorithms in the scenario settings.
                .mapKeys(this::calculateSpeakerVolumes)
                // Create new ScenarioResult objects to store all results together.
                .mapKeyValue(ScenarioResult::new)
                .toList();
    }

    /**
     * Read the input samples for this step from the audio input stream of the scenario.
     * The return value is an Optional to signal success (samples read) or failure
     * (exception while reading samples).
     * Additionally, if the end of stream is reached while reading, the playback
     * is queried to decide how to continue (stop or restart).
     */
    private Optional<short[]> readSamples(ActiveScenario activeScenario) {
        try {
            // Create a samples array of the right size (samples needed = frames needed, because the input must be mono).
            short[] samples = new short[this.amountOfFramesNeeded];
            // Read the actual samples into the array and record if there was an end of stream reached while doing so.
            boolean endOfStream = SampleReader.readSamples(activeScenario.getInputStream(), samples);
            if (endOfStream) {
                // End of stream reached, check playback for next action.
                if (activeScenario.getPlayback().endOfStream()) {
                    // Continue with the next playback loop, so restart scenario.
                    this.scenariosToRestart.add(activeScenario);
                } else {
                    // No more playback, remove scenario.
                    this.scenariosToRemove.add(activeScenario);
                }
            }
            // Return the samples as 'successful' optional.
            return Optional.of(samples);
        } catch (IOException e) {
            // Instead of crashing upon an IOException in reading, we just log it and remove that scenario.
            Logger.error(e, "Exception while reading from audio input stream");
            this.scenariosToRemove.add(activeScenario);
            // Return an empty optional to signal failure.
            return Optional.empty();
        }
    }

    /**
     * Calculate the speaker volumes by using the configured dynamic locations
     * and volume algorithms of the given scenario.
     */
    private SpeakerVolumes calculateSpeakerVolumes(ActiveScenario activeScenario) {
        // Default is 0: not started yet.
        long millisSinceStart = 0;
        if (activeScenario.isStarted()) {
            // If the scenario is started: calculate the millis since start.
            millisSinceStart = System.currentTimeMillis() - activeScenario.getStartMillis();
        }
        // Get the listener and source location from the configured dynamic location objects.
        Vector3D listener = activeScenario.getListenerLocation().getLocation(millisSinceStart);
        Vector3D source = activeScenario.getSourceLocation().getLocation(millisSinceStart);
        // Build a snapshot of the scenario to provide to the volume algorithms.
        Snapshot snapshot = Snapshot.builder()
                .scenario(activeScenario.getScenario())
                .source(source)
                .listener(listener)
                .build();
        // Calculate the volume ratios using the configured algorithm.
        SpeakerVolumeRatios speakerVolumeRatios = activeScenario.getVolumeRatiosAlgorithm().calculateVolumeRatios(snapshot);
        // Calculate the actual volumes using the configured normalize algorithm.
        return activeScenario.getNormalizeAlgorithm().calculateVolumes(speakerVolumeRatios);
    }

    /**
     * Calculate the byte buffers that should be fed to the corresponding sound card streams and return that as a mapping.
     * This method does not perform any actual writing to the sound card streams yet,
     * cause that needs to be done asynchronously later.
     */
    private Map<SoundCardStream, byte[]> calculateOutputBuffers(List<ScenarioResult> scenarioResults) {
        // Every sound card stream is coupled to the 'raw' input data.
        return toMapFixedValue(this.soundCardStreams, scenarioResults)
                // Calculate the samples per scenario result and save them in one list per sound card stream.
                .mapToValue(this::calculateCombinedOutputSamples)
                // Merge the samples lists into one list of output samples per sound card stream.
                .mapValues(this::mergeSamples)
                // Write the samples into a byte buffer that can be fed to the sound card stream.
                .mapValues(this::writeSamplesToBuffer)
                .toMap();
    }

    /**
     * Calculate the samples for all scenario results for the given sound card stream.
     */
    private List<short[]> calculateCombinedOutputSamples(SoundCardStream soundCardStream, List<ScenarioResult> scenarioResults) {
        return StreamEx.of(scenarioResults)
                // Just map the individual samples calculation over the list of scenario results.
                .map(scenarioResult -> this.calculateOutputSamples(soundCardStream, scenarioResult.speakerVolumes, scenarioResult.samples))
                .toList();
    }

    /**
     * Calculate the output samples based on the input samples and the configured speaker volumes for this sound card stream.
     */
    private short[] calculateOutputSamples(SoundCardStream soundCardStream, SpeakerVolumes speakerVolumes, short[] sampleBuffer) {
        // Get the volume fractions for the left and right speaker of this sound card stream from the speaker volumes.
        double volumeFractionSpeakerLeft = speakerVolumes.getVolumeFraction(soundCardStream.getSoundCard().getLeftSpeaker().getId());
        double volumeFractionSpeakerRight = speakerVolumes.getVolumeFraction(soundCardStream.getSoundCard().getRightSpeaker().getId());
        // The output samples size will be twice the size of the input, because the output is in stereo.
        short[] stereoSamples = new short[sampleBuffer.length * 2];
        // For all input samples.
        for (int sampleIndex = 0; sampleIndex < sampleBuffer.length; sampleIndex++) {
            // Calculate the left and right sample according to the corresponding volume fractions.
            short leftSample = (short) (sampleBuffer[sampleIndex] * volumeFractionSpeakerLeft);
            short rightSample = (short) (sampleBuffer[sampleIndex] * volumeFractionSpeakerRight);
            // Put the calculated samples at the right place in the output buffer.
            stereoSamples[sampleIndex * 2] = leftSample;
            stereoSamples[sampleIndex * 2 + 1] = rightSample;
        }
        return stereoSamples;
    }

    /**
     * Merge the samples lists by summing the sample values for each index and returning one list of output samples.
     * This method also takes into account that the merged sample value should not be out of bounds of the sample size.
     */
    private short[] mergeSamples(List<short[]> samplesLists) {
        // Optimization: if there is just one input list, it well be equal to the output list, so we can skip the calculations.
        if (samplesLists.size() == 1) {
            return samplesLists.get(0);
        }
        // The merged samples will have the same length as each of the sample lists, so just take the length of first one.
        short[] samples = new short[samplesLists.get(0).length];
        // For each sample index, calculate the merged sample value.
        for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex++) {
            // Merging the buffers is just a matter of summing the amplitudes (=samples) of the different sounds.
            // Use an int for the intermediate calculation, to prevent number overflow issues.
            int totalAmplitude = 0;
            for (short[] sampleBuffer : samplesLists) {
                totalAmplitude += sampleBuffer[sampleIndex];
            }
            short sanitizedAmplitude;
            // Keep amplitude within the boundaries of the sample size.
            if (this.outputFormat.getSampleSize() == SampleSize.ONE_BYTE) {
                sanitizedAmplitude = this.sanitizeAsByte(totalAmplitude);
            } else {
                sanitizedAmplitude = this.sanitizeAsShort(totalAmplitude);
            }
            // The final sample value is calculated, put it in the output buffer.
            samples[sampleIndex] = sanitizedAmplitude;
        }
        return samples;
    }

    /**
     * Write the samples into a byte buffer according to the output format.
     */
    private byte[] writeSamplesToBuffer(short[] streamAmplitudes) {
        return SampleWriter.writeSamples(streamAmplitudes, this.outputFormat);
    }

    /**
     * Bring the sample value back into the boundaries of a byte by cutting it off at the max or min value if needed.
     */
    private byte sanitizeAsByte(int sample) {
        return (byte) Math.max(Math.min(sample, Byte.MAX_VALUE), Byte.MIN_VALUE);
    }

    /**
     * Bring the sample value back into the boundaries of a short by cutting it off at the max or min value if needed.
     */
    private short sanitizeAsShort(int sample) {
        return (short) Math.max(Math.min(sample, Short.MAX_VALUE), Short.MIN_VALUE);
    }

    /**
     * Represents the calculated output of one scenario:
     * - the samples of the scenario input stream
     * - the volumes for each speaker according to the scenario settings
     *
     * Modeled as inner class to wrap these two values and give them a nice name.
     */
    private class ScenarioResult {
        SpeakerVolumes speakerVolumes;
        short[] samples;

        public ScenarioResult(SpeakerVolumes speakerVolumes, short[] samples) {
            this.speakerVolumes = speakerVolumes;
            this.samples = samples;
        }
    }

}
