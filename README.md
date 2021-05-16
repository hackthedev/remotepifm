# Remote Pi FM <img src="https://img.shields.io/badge/Google Play Store-Awaiting Approval-orange" /> <img src="https://img.shields.io/badge/Amazon App Store-Awaiting Approval-orange" />

This is an Android App which allows you to control you Raspberry Pi (3B+ prefered) in combination with the [PiFmRds Project](https://github.com/ChristopheJacquet/PiFmRds). 
I made this app beccause I wanted to be able to control everything from my Smartphone. I once made a C# Application which is exactly like this app but was a WinForm App 
that I've emulated using mono on the Raspberry Pi. It worked, but my Touchscreen stopped working for the Pi.

This was originally a private repo, but since some of my friends say this is really cool I thought I'd try publishing it. I hope this App is going to help you somehow or makes
controlling it easier. 

<br>

### Contents
- [Requirements](https://github.com/hackthedev/remotepifm#requirements)
- [Software Installation](https://github.com/hackthedev/remotepifm#software-installation)
- [Hardware Setup](https://github.com/hackthedev/remotepifm#hardware-setup)
- [Features](https://github.com/hackthedev/remotepifm#features)
- [App Store](https://github.com/hackthedev/remotepifm#app-store)
- [Planned Features](https://github.com/hackthedev/remotepifm#planned-features)
- [About Updates](https://github.com/hackthedev/remotepifm#about-updates)
- [Download](https://github.com/hackthedev/remotepifm#download)
- [Screenshots](https://github.com/hackthedev/remotepifm#screenshots)

<br>


## Requirements
- [PiFmRds Project](https://github.com/ChristopheJacquet/PiFmRds)
- Raspberry Pi (tested with 3B+)
  - <i>Quote: It is compatible with both the Raspberry Pi 1 (the original one) and the Raspberry Pi 2, 3 and 4.</i>

<br>

## Software Installation
To install [PiFmRds](https://github.com/ChristopheJacquet/PiFmRds), enter the following lines.
```
apt-get update -y
apt-get upgrade -y
apt-get install sudo

sudo apt-get install libsndfile1-dev
git clone https://github.com/ChristopheJacquet/PiFmRds.git
cd PiFmRds/src
make clean
make
```

<br>

To verify if it's working, you can run `sudo ./pi_fm_rds -freq 105.3 -audio sound.wav`. If you've set a nearby radio to the FM Frequency `105.3` and hear the `sound.wav` file, it is working. Now you could [download](https://github.com/hackthedev/remotepifm/releases/download/1.1.1/app-release.apk) the Android App and try connecting to your Raspberry Pi. Please Note that your Smartphone and Raspberry Pi need a internet connection.

<br>

## Hardware Setup
Hardware Setup is really simple. On the Raspberry Pi 3B+, hook a 1 meter long cable to the GPIO4 like shown below.

<img src="https://shy-devils.life-is-pa.in/RYXUx9.jpeg" />

<br>

## Features
- Wireless SSH connection to your Raspberry Pi
- Play and Stop FM Broadcast
- Select <b>uploaded</b> files to play
- Set your custom frequency
- Convert uploaded .mp3 files to .wav
  - Also converts file names (spaces are being removed, brackets will be also removed etc...)

<br>

## App Store
I've submitted this Application to Google Play and Amazon's App Store. Therefore don't just copy it and publish it on your own. Since I already submitted it and I don't like 
the idea of someone copying and republishing my app. See the license file anyway. 
  
<br>

## Planned Features
- Save connections
- Upload files via App (implemented but disabled, else it would crash app)
- Auto-Installer (e.g. automatically install PiFmRds, sndfile lib, etc..)

<br>

## About Updates
I've planed to keep updating this app and add new features. When everything works fine I've planed to clean up the code and improve it. For now it is a little bit messy, but this will be fixed! This is my ever first android app.

<br>

## Download
If you want to download <b>the app</b> (=apk file), [click here](https://github.com/hackthedev/remotepifm/releases/download/1.1.1/app-release.apk). You have to have "Unknown Sources" to be enabled or wait until my submission on Google Play Store was approved and download it from there.

<br>

## Screenshots
<img width="407" height="863" src="https://shy-devils.life-is-pa.in/124VaF.png" />
<img width="407" height="863" src="https://shy-devils.life-is-pa.in/o2Eogf.png" />
(yeah i really love nightcore)
