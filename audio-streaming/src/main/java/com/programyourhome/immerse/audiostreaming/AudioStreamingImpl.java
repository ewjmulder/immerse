package com.programyourhome.immerse.audiostreaming;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.springframework.stereotype.Component;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.speakermatrix.SpeakerMatrix;

@Component
public class AudioStreamingImpl implements AudioStreaming {

    @Inject
    private Executor scenarioExecutor;

    @Inject
    private SpeakerMatrix speakerMatrix;

    @Override
    public void playScenario(Room room, Scenario scenario, Set<SoundCard> soundCards, SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration) {
        this.scenarioExecutor.execute(() -> new ScenarioPlayer(this.speakerMatrix, soundCards, room, scenario, soundCardToSpeakerConfiguration).play());
    }

    // TODO: consider making a separate domain module which all others depend on, since there is much overlap, esp if you want to server
    // to be able to persist them!!

    // TODO: handle the info in this bran dump / test below!

    // TODO: support for other PCM formats besides signed.

    public static void main(String[] args) throws Exception {
        for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) {
            System.out.println(
                    thisMixerInfo.getDescription() + " - " + thisMixerInfo.getName() + " - " + thisMixerInfo.getVendor() + " - " + thisMixerInfo.getVersion());
            Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
            for (Line.Info thisLineInfo : thisMixer.getSourceLineInfo()) {
                if (thisLineInfo.getLineClass().getName().equals(
                        "javax.sound.sampled.Port")) {
                    Line thisLine = thisMixer.getLine(thisLineInfo);
                    thisLine.open();
                    System.out.println(" Source Port: "
                            + thisLineInfo.toString());
                    // for (Control thisControl : thisLine.getControls()) {
                    // System.out.println(AnalyzeControl(thisControl));
                    // }
                    thisLine.close();
                }
            }
        }

        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException("Unable to initialize libusb.", result);
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result2 = LibUsb.getDeviceList(null, list);
        if (result2 < 0)
            throw new LibUsbException("Unable to get device list", result);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS)
                    throw new LibUsbException("Unable to read device descriptor", result);

                System.out.println("Bus number: " + LibUsb.getBusNumber(device));
                System.out.println("Port number: " + LibUsb.getPortNumber(device));
                System.out.println(descriptor.dump());
                System.out.println("");
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        /*
         * Finally, I've got the missing link:
         * This command contains both the hardware USB 'path' and the sound card index! :-D
         * Very probably not something you can access via any Java API
         * ζ ls -la /dev/snd/by-path [ff25b50]
         * total 0
         * drwxr-xr-x 2 root root 100 Oct 3 00:23 .
         * drwxr-xr-x 4 root root 400 Oct 3 00:23 ..
         * lrwxrwxrwx 1 root root 12 Oct 3 00:23 pci-0000:00:14.0-usb-0:1:1.0 -> ../controlC1
         * lrwxrwxrwx 1 root root 12 Oct 3 00:23 pci-0000:00:14.0-usb-0:3:1.0 -> ../controlC2
         * lrwxrwxrwx 1 root root 12 Sep 25 10:31 pci-0000:00:1f.3 -> ../controlC0
         * PS: this gives the link as well: udevadm info -a -n /dev/snd/controlC1
         * Nice clean command: find /dev/snd/by-path -type l -printf "%p ---> %l\n"
         */
        // UsbHub hub = new Services().getRootUsbHub();
        // List l = hub.getAttachedUsbDevices();
        // for (UsbDevice subhub : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
        // if (subhub.isUsbHub()) {
        // for (UsbDevice device : (List<UsbDevice>) ((UsbHub) subhub).getAttachedUsbDevices()) {
        // try {
        // System.out.println("Device: " + device.getManufacturerString() + " - " + device.getProductString() + " - "
        // + device.getSerialNumberString());
        // } catch (UsbPlatformException e) {
        // System.out.println("No access to device x");
        // e.printStackTrace();
        // }
        // }
        // }
        // }

        for (int i = 0; i < AudioSystem.getMixerInfo().length; i++) {
            for (int k : new int[] { 8, 11, 22, 32, 44, 48 }) {
                File url = new File("/home/emulder/Downloads/game-music-mono-" + k + "k.wav");
                final AudioInputStream stream = AudioSystem.getAudioInputStream(url);

                AudioFormat inputFormat = stream.getFormat();
                // Explicitly set to stereo to control each speaker individually.
                AudioFormat outputFormat = new AudioFormat(inputFormat.getEncoding(), inputFormat.getSampleRate(), inputFormat.getSampleSizeInBits(),
                        2, 4, inputFormat.getFrameRate(), inputFormat.isBigEndian());
                try {
                    Mixer.Info[] q = AudioSystem.getMixerInfo();
                    SourceDataLine line = AudioSystem.getSourceDataLine(outputFormat, AudioSystem.getMixerInfo()[i]);
                    // SourceDataLine line = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, format));
                    line.open();
                    line.close();
                    // System.out.println(" OK : [" + i + "] - " + k);
                } catch (IllegalArgumentException e) {
                    // System.out.println("FAIL: [" + i + "] - " + k);
                }
            }
        }
    }

    // Note: Seems like plughw:x,0 is a device we can use. The one that is not working, is actually accessable by the defailt (index 0).

}
