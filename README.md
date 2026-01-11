# Servo for Cordova

> [!WARNING]  
> This plugin is built to demonstrate the usage of Servo in Cordova. Due to some workarounds that involve a webserver and websockets it's not really secure and **SHOULD NOT BE USED IN PRODUCTION app**. Read more under [Limitations](#limitations)

Whith this plugin for [Apache Cordova](https://cordova.apache.org) you can test [Servo](https://servo.org) an alternative engine on Android.

## Why?

> Servo aims to empower developers with a lightweight, high-performance alternative for embedding web technologies in applications. 
From [servo.org](http://servo.org)

## How to use

You can add this to any Cordova app with:

```
cordova plugin add https://github.com/NiklasMerz/cordova-plugin-servo-webview
```

Because your app assets are served with a local webserver you need to adjust this in your config.xml:

```xml
<content src="http://localhost:5000" />
```

## Status

This is the first stage of development. Which means this plugin uses a build from Servos main branch without any modifications and just the current APIs. It works with come workarounds.

The second stage is working together with the Servo team to bring new APIs to Servo to make this plugin work without any hacks.

## Limitations

Servo and it's ServoView Java implementation allow embedding Servo in Android apps pretty easily. Servo lacks some APIs that make it a perfect replacement for Android WebView. Therefore some workarounds are part of this version that have some serious drawbacks:

1. The Cordova <-> Native bridge uses a legacy bridge mode and a local websocket server. There it's slower and way less secure
2. Servo does not have something similar to [WebViewAssetLoader](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader) to "host" local files for the WebView therefore this plugins has a local webserver to host the assets in `www`