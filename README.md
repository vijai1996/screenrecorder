# ScreenRecorder   [![Build Status](https://travis-ci.org/vijai1996/screenrecorder.svg?branch=master)](https://travis-ci.org/vijai1996/screenrecorder)
![App Icon](ic_launcher-web.png?raw=true "App Icon")

Requires Lollipop and above (SDK 21+)

<a href="https://f-droid.org/packages/com.orpheusdroid.screenrecorder/" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"/></a>
<a href="https://play.google.com/store/apps/details?id=com.orpheusdroid.screenrecorder" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="100"/></a>

## User Support
Telegram: [https://goo.gl/TDs42r](https://goo.gl/TDs42r)

[![Telegram Support QR](https://goo.gl/TDs42r.qr "Telegram Support QR")](https://goo.gl/TDs42r.qr)

Slack:    Comming soon!

## Donation/Payments
#### Bitcoin:     1Cbf61y8XNx3BLWvoZB71x4XgBKB7r8BuB
#### PayPal:      [![Paypal Donate](https://www.paypalobjects.com/webstatic/en_US/i/btn/png/gold-pill-paypal-26px.png)](https://paypal.me/vijaichander/5)
#### Flattr:      [![Flattr this git repo](https://button.flattr.com/flattr-badge-large.png)](https://flattr.com/submit/auto?fid=66ngyo&url=https%3A%2F%2Fgithub.com%2Fvijai1996%2Fscreenrecorder)

## Building the app

### Make a copy of the repository

Make sure to have Git installed and clone the repo using

```
git clone https://github.com/vijai1996/screenrecorder
```

### Building the apk
Building apk is possible in 3 ways
* 1.a. [Building debug apk using commandline](https://github.com/vijai1996/screenrecorder#1a-building-debug-apk-using-commandline)
* 1.b. [Building release apk using commandline](https://github.com/vijai1996/screenrecorder#1b-building-release-apk-using-commandline)
* 2.   [Building using AndroidStudio](https://github.com/vijai1996/screenrecorder#2-building-using-androidstudio)

#### 1.a. Building debug apk using commandline
Switch to project root directory and run 
```
gradlew.bat assembleDebug
```

#### 1.b. Building release apk using commandline
Switch to project root directory and make sure to edit `app` module's build.gradle to include signing key information and run
```
gradlew.bat assembleRelease
```

#### 2. Building using AndroidStudio
Open Android Studio -> File -> Import Project -> Choose the cloned project folder and continue with the on-screen instructions

## Contributions
Any contribution to the app is welcome in the form of pull requests.

## Authors

* **Vijai Chander** - *Initial work* - [vijai1996](https://github.com/vijai1996)

## License

This project is licensed under the GNU AGPLv3 - see the [LICENSE](LICENSE) file for details
