package com.programyourhome.immerse.domain.audio.soundcard;

public class PhysicalDeviceInfo {

    private String name;
    private String vendor;
    private String product;
    private String serialNumber;

    public PhysicalDeviceInfo() {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PhysicalDeviceInfo info;

        public Builder() {
            this.info = new PhysicalDeviceInfo();
        }

        public Builder name(String name) {
            this.info.name = name;
            return this;
        }

        public Builder vendor(String vendor) {
            this.info.vendor = vendor;
            return this;
        }

        public Builder product(String product) {
            this.info.product = product;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.info.serialNumber = serialNumber;
            return this;
        }

        public PhysicalDeviceInfo build() {
            return this.info;
        }
    }

}
