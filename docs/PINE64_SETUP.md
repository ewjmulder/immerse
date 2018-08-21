### TODO: Refactor this document into a nicely readable format


## Step 1: Getting an Ubuntu image on the Pine64
For the (original) Pine A64(+), download the base Ubuntu Xenial image from:
http://wiki.pine64.org/index.php/Pine\_A64\_Software\_Release#Xenial\_Base\_Image

For the newer Pine A64-LTS, download the Ubuntu Xenial Mate image from: (unfortunately no headless version available)
http://wiki.pine64.org/index.php/SOPINE\_Software\_Release#Xenial\_Mate

Install the image on a micro SD card, see: https://help.ubuntu.com/community/Installation/FromImgFiles/
Or in Ubuntu double-click the downloaded file and it will ask you where to restore the disk.
Be careful to select your USB card or you might damage your hard disk!

## Step 2: Booting the Pine64 and performing some system updates

Put the SD card into the Pine, connect it to the internet using a cable and plug in the power. You might be tempted to
connect a screen with an HDMI cable, but the Pine64 has a very bad reputation with connecting screens (at least the older model).
Instead find out through your router's list of attached devices what IP the Pine64 has and ssh into it. Username/password are ubuntu/ubuntu.


```
# Resize the partition to use the full size of the SD card.
sudo /usr/local/sbin/resize_rootfs.sh
# Reboot for the changes to take effect.
sudo reboot -h now

# Update uboot.
sudo /usr/local/sbin/pine64\_update\_uboot.sh 
# Reboot for the changes to take effect.
sudo reboot -h now

# Update the kernel.
sudo /usr/local/sbin/pine64\_update\_kernel.sh 
# Reboot for the changes to take effect.
sudo reboot -h now
```

NB: after the reboot command your ssh session might hang: just ctrl-c (or kill the process) and reconnect after the boot has completed.

## Step 3: Configure the system for proper use

```
# Give the ubuntu user access to the audio devices
sudo usermod -a -G audio ubuntu

# Turn up the volume of the sound cards (TODO: generic solution)
# Select USB sound cards one by one with F6 and turn volume to your prefered max level (prob. 80% - 100%)
alsamixer 

# Install any more command line tools you might need (optional).
# The following lines are suggestions for things that might come in handy.

# Simple mode-less text editor (much easier than vi)
sudo apt-get install nano

# Package to easily test play audio files on the command line (command: play)
sudo apt-get install sox
```


## Step 4: Install the software needed for Immerse

For now, we will buid and run Immerse directly from the git repo. In the future it might be available from Maven central.

```
# Update the apt repos with the most recent information.
sudo apt-get update
# Install git to be able to clone the repo.
sudo apt-get install -y git
# Install OpenJDK 8 for running Immerse.
# It must be OpenJDK and not Oracle JDK, because OpenJDK includes the Java Sound API in the headless version while the Oracle JDK does not.
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install -y openjdk-8-jdk
```

## Step 5: Clone, build and run Immerse

```
# Go to the desired location on the disk.
# If the machine is just for running Immerse (which is recommended), /home/ubuntu should be fine.
cd /home/ubuntu
# Clone the repo
git clone https://github.com/ewjmulder/immerse
# Go to the repo folder
cd immerse
# Build the project
./gradlew build

//TODO: create the properties file for your system

# Run the project
# This could be done with ./gradlew run, but reality has proven that this makes quite a difference in memory and performance,
# so better do it like this.
//TODO: command line for running
```


Example output of running resize_rootfs

```
ubuntu@pine64:~$ sudo /usr/local/sbin/resize_rootfs.sh
[sudo] password for ubuntu: 
+ DEVICE=/dev/mmcblk0
+ PART=2
+ resize
+ fdisk -l /dev/mmcblk0
+ grep /dev/mmcblk0p2
+ awk {print $2}
+ start=143360
+ echo 143360
143360
+ set +e
+ fdisk /dev/mmcblk0

Welcome to fdisk (util-linux 2.27.1).
Changes will remain in memory only, until you decide to write them.
Be careful before using the write command.


Command (m for help): Disk /dev/mmcblk0: 29.6 GiB, 31724666880 bytes, 61962240 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: dos
Disk identifier: 0x62b90133

Device         Boot  Start     End Sectors  Size Id Type
/dev/mmcblk0p1       40960  143359  102400   50M  c W95 FAT32 (LBA)
/dev/mmcblk0p2      143360 7679999 7536640  3.6G 83 Linux

Command (m for help): Partition number (1,2, default 2): 
Partition 2 has been deleted.

Command (m for help): Partition type
   p   primary (1 primary, 0 extended, 3 free)
   e   extended (container for logical partitions)
Select (default p): Partition number (2-4, default 2): First sector (2048-61962239, default 2048): Last sector, +sectors or +size{K,M,G,T,P} (143360-61962239, default 61962239): 
Created a new partition 2 of type 'Linux' and of size 29.5 GiB.

Command (m for help): The partition table has been altered.
Calling ioctl() to re-read partition table.
Re-reading the partition table failed.: Device or resource busy

The kernel still uses the old table. The new table will be used at the next reboot or after you run partprobe(8) or kpartx(8).

+ set -e
+ partx -u /dev/mmcblk0
+ resize2fs /dev/mmcblk0p2
resize2fs 1.42.13 (17-May-2015)
Filesystem at /dev/mmcblk0p2 is mounted on /; on-line resizing required
old_desc_blocks = 1, new_desc_blocks = 2
The filesystem on /dev/mmcblk0p2 is now 7727360 (4k) blocks long.

+ echo Done!
Done!
```