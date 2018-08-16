package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource getting it's data from an UDP connection.
 * The maximum packet length is 1024, longer packets will be truncated.
 *
 * NB: The sender of the packets must make sure that the size of a packet is a multitude of the size of an audio frame.
 * If not, audio will become noise after missing 1 packet or receiving them out of sync.
 */
public class UdpAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private static final int MAX_PACKET_SIZE = 1024;

    private final InetAddress host;
    private final int port;
    private final ImmerseAudioFormat audioFormat;
    private final String startMessage;
    private final AudioInputStream audioInputStream;

    public UdpAudioResource(InetAddress host, int port, ImmerseAudioFormat audioFormat, String startMessage) {
        this.host = host;
        this.port = port;
        this.audioFormat = audioFormat;
        this.startMessage = startMessage;
        this.audioInputStream = null; // TODO: We need some kind of UDPInputStream, see:
                                      // https://github.com/SINTEF-9012/dliver/blob/master/dliver-desktop/src/main/java/com/rbnb/utility/UDPInputStream.java
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

    public static Factory<AudioResource> udp(InetAddress host, int port, ImmerseAudioFormat audioFormat, String startMessage) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UdpAudioResource(host, port, audioFormat, startMessage);
            }
        };
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
        private final byte[] buf = new byte[MAX_PACKET_SIZE];

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
