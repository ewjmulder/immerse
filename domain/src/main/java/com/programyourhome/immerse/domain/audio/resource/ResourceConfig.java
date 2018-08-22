package com.programyourhome.immerse.domain.audio.resource;

import java.io.Serializable;

import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * AudioResource configuration that will be used to determine the right internal buffer size.
 */
public class ResourceConfig implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    public static final int DEFAULT_CHUNK_SIZE_IN_MILLIS = 50;
    public static final int DEFAULT_PACKET_SIZE_IN_MILLIS = 10;

    private boolean live;
    private int chunkSize;
    private int packetSize;

    private ResourceConfig() {
    }

    /**
     * Whether the resource is live or not.
     * In this context live means it is generated 'on the fly' and non-repeatable,
     * like microphone input, some kind of (broadcasted) stream or dynamically generated audio.
     * This also indicates that the input is only available at the 'live time rate', meaning that
     * the stream supplies as many bytes per second as a player will play. Normally,
     * that means some fair amount of buffer should be in place, but for Immerse, we want to
     * keep this buffer as small as possible for the best 'live' experience.
     * Therefore, Immerse will handle live audio resources somewhat different than non-live ones.
     */
    public boolean isLive() {
        return this.live;
    }

    /**
     * Chunk size in bytes.
     * A chunk means the amount of data that becomes available at once from the underlying source.
     * This is particularly important in live streams, since any data that will not fit in the buffer
     * will be thrown away. Immerse will make sure the buffer is at least big enough for one chunk of data.
     */
    public int getChunkSize() {
        return this.chunkSize;
    }

    /**
     * Packet size in bytes.
     * A packet means the amount of data that will be communicated at once (mostly over the network).
     * This is particularly important in live streams, since any data that will not fit in the buffer
     * will be thrown away. Immerse will make sure the buffer is at least big enough for one packet of data.
     *
     * It is advised to keep the packet size quite small compared to the chunk size (if under your control).
     * Especially in UDP this can be configured precisely.
     */
    public int getPacketSize() {
        return this.packetSize;
    }

    public static Builder builder(ImmerseAudioFormat format) {
        return new Builder(format);
    }

    public static ResourceConfig defaultNonLive(ImmerseAudioFormat format) {
        return ResourceConfig.builder(format).build();
    }

    public static class Builder {
        private final ImmerseAudioFormat format;
        private final ResourceConfig config;

        public Builder(ImmerseAudioFormat format) {
            this.format = format;
            this.config = new ResourceConfig();
            this.config.live = false;
            this.config.chunkSize = this.millisToBytes(DEFAULT_CHUNK_SIZE_IN_MILLIS);
            this.config.packetSize = this.millisToBytes(DEFAULT_PACKET_SIZE_IN_MILLIS);
        }

        public Builder live() {
            this.config.live = true;
            return this;
        }

        public Builder nonLive() {
            this.config.live = false;
            return this;
        }

        public Builder chunkSize(int chunkSize) {
            this.config.chunkSize = chunkSize;
            return this;
        }

        public Builder chunkSizeInMillis(int chunkSizeInMillis) {
            this.config.chunkSize = this.millisToBytes(chunkSizeInMillis);
            return this;
        }

        public Builder packetSize(int packetSize) {
            this.config.packetSize = packetSize;
            return this;
        }

        public Builder packetSizeInMillis(int packetSizeInMillis) {
            this.config.packetSize = this.millisToBytes(packetSizeInMillis);
            return this;
        }

        private int millisToBytes(int millis) {
            return (int) (millis * this.format.getNumberOfBytesPerMilli());
        }

        public ResourceConfig build() {
            return this.config;
        }

    }

}
