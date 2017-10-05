package com.programyourhome.immerse.audiostreaming.model.soundcard;

public class PhysicalDeviceInfo {

    private String name;
    private String vendor;
    private String product;
    private String serialNumber;

    public PhysicalDeviceInfo(String name, String vendor, String product, String serialNumber) {
        this.name = name;
        this.vendor = vendor;
        this.product = product;
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return this.name;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getProduct() {
        return this.product;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

}
