package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.resource.StreamConfig;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource getting it's data from an UDP connection.
 * The supplied packet size must be a multitude of the frame size to keep playing consistent upon packet drop.
 * The recommended packet size is quite low, having around one millisecond of audio data.
 */
public class UdpAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final AudioInputStream audioInputStream;
    private final StreamConfig config;

    public UdpAudioResource(InetAddress host, int port, int chunkSize, int packetSize, ImmerseAudioFormat audioFormat, String startMessage) {
        if (packetSize % audioFormat.getNumberOfBytesPerFrame() != 0) {
            throw new IllegalArgumentException("Packet size must be a multitude of frame size");
        }
        if (startMessage.getBytes().length > packetSize) {
            throw new IllegalArgumentException("Start message bytes should fit into the packet size");
        }
        this.audioInputStream = new AudioInputStream(new UdpInputStream(host, port, packetSize, audioFormat, startMessage),
                audioFormat.toJavaAudioFormat(), AudioSystem.NOT_SPECIFIED);
        this.config = StreamConfig.builder(this.getFormat())
                // UDP resources are always live.
                .live()
                .chunkSize(chunkSize)
                .packetSize(packetSize)
                .build();
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    @Override
    public StreamConfig getConfig() {
        return this.config;
    }

    public static Factory<AudioResource> udp(String host, int port, int chunkSize, int packetSize, ImmerseAudioFormat audioFormat, String startMessage) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                try {
                    return new UdpAudioResource(InetAddress.getByName(host), port, chunkSize, packetSize, audioFormat, startMessage);
                } catch (UnknownHostException e) {
                    throw new IllegalArgumentException("Host not found", e);
                }
            }
        };
    }

    public class UdpInputStream extends InputStream {
        // Timeout in millis before a receive fails and the stream is assumed to have stopped.
        public static final int UDP_SOCKET_TIMEOUT_MILLIS = 1000;

        private final InetAddress host;
        private final int port;
        private final int packetSize;
        private final ImmerseAudioFormat audioFormat;
        private final String startMessage;
        private final DatagramSocket socket;
        // A buffer to store part of a packet that has been received but not yet read.
        private final byte[] subPacketBuffer;
        // The amount of bytes present in the sub packet buffer.
        private int subPacketBufferSize;
        private boolean started;
        private boolean closed;

        public UdpInputStream(InetAddress host, int port, int packetSize, ImmerseAudioFormat audioFormat, String startMessage) {
            this.host = host;
            this.port = port;
            this.packetSize = packetSize;
            this.audioFormat = audioFormat;
            this.startMessage = startMessage;
            // Sub packet buffer will never need to be larger than 1 packet.
            this.subPacketBuffer = new byte[packetSize];
            // We start with no saved bytes in the sub packet buffer.
            this.subPacketBufferSize = 0;
            try {
                this.socket = new DatagramSocket();
                this.socket.setSoTimeout(UDP_SOCKET_TIMEOUT_MILLIS);
            } catch (SocketException e) {
                this.closed = true;
                throw new IllegalStateException("Exception while creating UDP socket", e);
            }
            this.started = false;
            this.closed = false;
        }

        @Override
        public int read() throws IOException {
            throw new IOException("Reading of single bytes is not supported, read into a buffer instead");
        }

        @Override
        public int read(byte[] toBuffer, int offset, int length) throws IOException {
            if (this.closed) {
                return -1;
            }
            if (length % this.audioFormat.getNumberOfBytesPerFrame() != 0) {
                throw new IllegalArgumentException("Amount to read [" + length + "] "
                        + "must be a multitude of frame size [" + this.audioFormat.getNumberOfBytesPerFrame() + "]");
            }
            // If we've not started yet, send the start message to the configured host and port.
            if (!this.started) {
                byte[] startBytes = this.startMessage.getBytes();
                DatagramPacket startPacket = new DatagramPacket(startBytes, startBytes.length, this.host, this.port);
                this.socket.send(startPacket);
                this.started = true;
            }
            // If we have started, we can receive packets from the sender.
            int bytesRead = 0;
            int bytesToRead = length;
            // First copy any bytes left from the sub packet buffer.
            int amountToReadFromSubPacketBuffer = Math.min(bytesToRead, this.subPacketBufferSize);
            System.arraycopy(this.subPacketBuffer, 0, toBuffer, offset, amountToReadFromSubPacketBuffer);
            bytesRead += amountToReadFromSubPacketBuffer;
            this.subPacketBufferSize -= amountToReadFromSubPacketBuffer;
            if (this.subPacketBufferSize > 0) {
                // Re-align the bytes left in the sub packet buffer to the front.
                System.arraycopy(this.subPacketBuffer, amountToReadFromSubPacketBuffer, this.subPacketBuffer, 0, this.subPacketBufferSize);
                // All bytes needed could be read from the sub packet buffer, so no further network reading needed.
            } else {
                // Sub packet buffer was not big enough to fill the toBuffer, start reading from network.
                try {
                    byte[] packetBuffer = new byte[this.packetSize];
                    while (bytesRead < bytesToRead && !this.closed) {
                        DatagramPacket audioPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
                        this.socket.receive(audioPacket);
                        if (audioPacket.getLength() != this.packetSize) {
                            this.closed = true;
                            throw new IllegalStateException(
                                    "Received packet size " + audioPacket.getLength() + " != configured packet size " + this.packetSize);
                        }
                        int amountToReadFromPacketBuffer = Math.min(bytesToRead - bytesRead, packetBuffer.length);
                        if (amountToReadFromPacketBuffer < packetBuffer.length) {
                            // Save the rest of the packet bytes into the sub packet buffer.
                            if (this.subPacketBufferSize > 0) {
                                throw new IllegalStateException("Sub packet size should be 0 when filling it up again");
                            }
                            int amountLeft = packetBuffer.length - amountToReadFromPacketBuffer;
                            System.arraycopy(packetBuffer, amountToReadFromPacketBuffer, this.subPacketBuffer, 0, amountLeft);
                            this.subPacketBufferSize = amountLeft;
                        }
                        System.arraycopy(packetBuffer, 0, toBuffer, offset + bytesRead, amountToReadFromPacketBuffer);
                        bytesRead += amountToReadFromPacketBuffer;
                    }
                } catch (IOException e) {
                    Logger.error(e, "IOException while receiving UDP packet");
                    this.closed = true;
                }
            }
            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }

}
