package com.ionicframework.cordova.webview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.ServiceWorkerController;
import android.webkit.ServiceWorkerClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.engine.SystemWebView;

public class ServoWebViewEngine extends SystemWebViewEngine {
  public static final String TAG = "ServoWebViewEngine";

  /**
   * Used when created via reflection.
   */
  public ServoWebViewEngine(Context context, CordovaPreferences preferences) {
    super(new SystemWebView(context), preferences);
    Log.d(TAG, "Servo WebView Engine Starting Right Up 1...");
  }

  public ServoWebViewEngine(SystemWebView webView) {
    super(webView, null);
    Log.d(TAG, "Servo WebView Engine Starting Right Up 2...");
  }

  public ServoWebViewEngine(SystemWebView webView, CordovaPreferences preferences) {
    super(webView, preferences);
    Log.d(TAG, "Servo WebView Engine Starting Right Up 3...");
  }

  @Override
  public void init(CordovaWebView parentWebView, CordovaInterface cordova, final CordovaWebViewEngine.Client client,
                   CordovaResourceApi resourceApi, PluginManager pluginManager,
                   NativeToJsMessageQueue nativeToJsMessageQueue) {
    ConfigXmlParser parser = new ConfigXmlParser();
    parser.parse(cordova.getActivity());
  }
}
