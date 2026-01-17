# servoview.aar

This is an Android library that contains the Servo binary and Java and Android related sources for ServoView.

## Downloading the latest nightly

Download the latest nightly from: https://download.servo.org/nightly/android/servo-latest.aar

## Building from the Servo repository

Build it from Servo source:

```bash
cd /path/to/servo
./mach build --android --release
cd support/android/apk
./gradlew assembleArm64Release
```

The AAR will be generated at:

`target/android/aarch64-linux-android/release/servoview.aar`




