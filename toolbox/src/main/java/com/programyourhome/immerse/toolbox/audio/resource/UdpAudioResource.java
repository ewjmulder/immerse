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
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource getting it's data from an UDP connection.
 * The supplied packet size must be a multitude of the frame size to keep playing consistent upon packet drop.
 * The recommended packet size is quite low, having just a few milliseconds of audio data.
 */
public class UdpAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final AudioInputStream audioInputStream;

    public UdpAudioResource(InetAddress host, int port, int packetSize, ImmerseAudioFormat audioFormat, String startMessage) {
        if (packetSize % audioFormat.getNumberOfBytesPerFrame() != 0) {
            throw new IllegalArgumentException("Packet size must be a multitude of frame size");
        }
        if (startMessage.getBytes().length > packetSize) {
            throw new IllegalArgumentException("Start message bytes should fit into the packet size");
        }
        this.audioInputStream = new AudioInputStream(new UdpInputStream(host, port, packetSize, startMessage),
                audioFormat.toJavaAudioFormat(), AudioSystem.NOT_SPECIFIED);
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    @Override
    public boolean isLive() {
        // UDP streams are always live.
        return true;
    }

    // TODO: address as String
    public static Factory<AudioResource> udp(InetAddress host, int port, int packetSize, ImmerseAudioFormat audioFormat, String startMessage) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UdpAudioResource(host, port, packetSize, audioFormat, startMessage);
            }
        };
    }

    public class UdpInputStream extends InputStream {

        private final InetAddress host;
        private final int port;
        private final int packetSize;
        private final String startMessage;
        private final DatagramSocket socket;
        private boolean started;

        public UdpInputStream(InetAddress host, int port, int packetSize, String startMessage) {
            this.host = host;
            this.port = port;
            this.packetSize = packetSize;
            this.startMessage = startMessage;
            this.started = false;
            try {
                this.socket = new DatagramSocket();
            } catch (SocketException e) {
                throw new IllegalStateException("Exception while creating UDP socket", e);
            }
        }

        @Override
        public int read() throws IOException {
            throw new IOException("Reading of single bytes is not supported, read into a buffer instead");
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length % this.packetSize != 0) {
                throw new IllegalArgumentException("Amount to read [" + length + "] must be a multitude of packet size [" + this.packetSize + "]");
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
            try {
                byte[] audioBuffer = new byte[this.packetSize];
                for (int i = 0; i < length / this.packetSize; i++) {
                    DatagramPacket audioPacket = new DatagramPacket(audioBuffer, audioBuffer.length);
                    this.socket.receive(audioPacket);
                    if (audioPacket.getLength() != this.packetSize) {
                        throw new IllegalStateException("Received packet size " + audioPacket.getLength() + " != configured packet size " + this.packetSize);
                    }
                    System.arraycopy(audioBuffer, 0, buffer, offset, audioBuffer.length);
                    offset += audioBuffer.length;
                    bytesRead += audioBuffer.length;
                }
            } catch (IOException e) {
                Logger.error(e, "IOException while receiving UDP packet");
                return -1;
            }
            return bytesRead;
        }
    }

    // FIXME: remove this! And below!
    public static void main(String[] args) throws IOException {
        new EchoServer().start();

        String reply = new EchoClient().sendEcho("Echoing");
        System.out.println("Reply from server: " + reply);
        new EchoClient().sendEcho("end");
    }

    // FIXME: remove this!
    public static class EchoClient {
        private final DatagramSocket socket;
        private final InetAddress address;

        private byte[] buf;

        public EchoClient() throws SocketException, UnknownHostException {
            this.socket = new DatagramSocket();
            System.out.println("Client uses port " + this.socket.getLocalPort());
            this.address = InetAddress.getByName("localhost");
        }

        public String sendEcho(String msg) throws IOException {
            this.buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length, this.address, 4445);
            this.socket.send(packet);
            packet = new DatagramPacket(this.buf, this.buf.length);
            this.socket.receive(packet);
            System.out.println("Buffer size: " + this.socket.getReceiveBufferSize());
            this.socket.setReceiveBufferSize(1024);
            System.out.println("Buffer size: " + this.socket.getReceiveBufferSize());
            System.out.println("Send buffer size: " + this.socket.getSendBufferSize());
            String received = new String(
                    packet.getData(), 0, packet.getLength());
            return received;
        }

        public void close() {
            this.socket.close();
        }
    }

    // FIXME: remove this!
    public static class EchoServer extends Thread {

        private final DatagramSocket socket;
        private boolean running;
        private final byte[] buf = new byte[1024];

        public EchoServer() throws SocketException {
            this.socket = new DatagramSocket(4445);
        }

        @Override
        public void run() {
            try {
                this.running = true;

                while (this.running) {
                    DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);
                    this.socket.receive(packet);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    System.out.println("Packet received from " + address + " on port " + port);
                    packet = new DatagramPacket(this.buf, this.buf.length, address, port);
                    String received = new String(packet.getData(), 0, packet.getLength());

                    if (received.equals("end")) {
                        this.running = false;
                        continue;
                    }
                    this.socket.send(packet);
                }
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
