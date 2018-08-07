package com.programyourhome.immerse.audiostreaming.mixer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.pmw.tinylog.Logger;

/**
 * TODO: documentation: wrapper extends cause no interface, but all public methods forwarded,
 * just writing interception. (is now synchronous writing, so causes slow down, change?)
 */
public class AudioInputStreamWrapper extends AudioInputStream {

    private static final InputStream DUMMY_INPUT_STREAM = null;
    private static final AudioFormat DUMMY_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    private static final int DUMMY_LENGTH = AudioSystem.NOT_SPECIFIED;

    private final AudioInputStream wrapped;
    private final OutputStream outputStream;

    public AudioInputStreamWrapper(AudioInputStream wrapped, OutputStream outputStream) {
        super(DUMMY_INPUT_STREAM, DUMMY_AUDIO_FORMAT, DUMMY_LENGTH);
        this.wrapped = wrapped;
        this.outputStream = outputStream;
    }

    @Override
    public int read() throws IOException {
        int read = this.wrapped.read();
        if (read != -1) {
            this.outputStream.write(read);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        // Just forward the read in this case, otherwise we will have duplicate writes.
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = this.wrapped.read(b, off, len);
        if (bytesRead != -1) {
            this.outputStream.write(b, off, off + bytesRead);
        }
        return bytesRead;
    }

    @Override
    public AudioFormat getFormat() {
        return this.wrapped.getFormat();
    }

    @Override
    public long getFrameLength() {
        return this.wrapped.getFrameLength();
    }

    @Override
    public int available() throws IOException {
        return this.wrapped.available();
    }

    @Override
    public void close() throws IOException {
        try {
            this.outputStream.close();
        } catch (IOException e) {
            Logger.error(e, "Exception during closing of output stream");
        }
        this.wrapped.close();
    }

    @Override
    public void mark(int readlimit) {
        this.wrapped.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return this.wrapped.markSupported();
    }

    @Override
    public void reset() throws IOException {
        this.wrapped.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return this.wrapped.skip(n);
    }

    @Override
    public boolean equals(Object obj) {
        return this.wrapped.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.wrapped.hashCode();
    }

    @Override
    public String toString() {
        return this.wrapped.toString();
    }

}
