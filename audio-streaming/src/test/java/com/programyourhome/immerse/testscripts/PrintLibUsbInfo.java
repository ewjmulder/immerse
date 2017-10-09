package com.programyourhome.immerse.testscripts;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class PrintLibUsbInfo {

    public static void main(String[] args) {
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
    }

}
