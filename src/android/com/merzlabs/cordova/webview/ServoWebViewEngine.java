package com.merzlabs.cordova.webview;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.View;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaBridge;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.LOG;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import fi.iki.elonen.NanoHTTPD;

public class ServoWebViewEngine implements CordovaWebViewEngine {
    public static final String TAG = "ServoWebViewEngine";
    protected final CordovaServoView servoView;

    protected String currentUrl = "";
    protected boolean canGoBack = false;
    protected CordovaPreferences preferences;
    protected CordovaBridge bridge;
    protected CordovaWebViewEngine.Client client;
    protected CordovaWebView parentWebView;
    protected CordovaInterface cordova;
    protected PluginManager pluginManager;
    protected CordovaResourceApi resourceApi;
    protected NativeToJsMessageQueue nativeToJsMessageQueue;
    
    private AssetHttpServer assetServer;
    private int serverPort = 0;

    /**
     * Used when created via reflection.
     */
    public ServoWebViewEngine(Context context, CordovaPreferences preferences) {
        this(new CordovaServoView(context), preferences);
        LOG.d(TAG, "Servo WebView Engine Starting Right Up 1...");
    }

    public ServoWebViewEngine(CordovaServoView webView) {
        this(webView, null);
        LOG.d(TAG, "Servo WebView Engine Starting Right Up 2...");
    }

    public ServoWebViewEngine(CordovaServoView webView, CordovaPreferences preferences) {
        this.servoView = webView;
        this.preferences = preferences;
        LOG.d(TAG, "Servo WebView Engine Starting Right Up 3...");
    }

    @Override
    public void init(CordovaWebView parentWebView, CordovaInterface cordova, final CordovaWebViewEngine.Client client,
            CordovaResourceApi resourceApi, PluginManager pluginManager,
            NativeToJsMessageQueue nativeToJsMessageQueue) {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(cordova.getActivity());

        this.parentWebView = parentWebView;
        this.cordova = cordova;
        this.client = client;
        this.resourceApi = resourceApi;
        this.pluginManager = pluginManager;
        this.nativeToJsMessageQueue = nativeToJsMessageQueue;
        
        // Start local HTTP server for serving assets
        startAssetServer();
        
        servoView.init(this, cordova);
    }

    @Override
    public CordovaWebView getCordovaWebView() {
        return parentWebView;
    }

    @Override
    public ICordovaCookieManager getCookieManager() {
        // TODO ServoView does not have a cookie manager, yet
        return null;
    }

    @Override
    public View getView() {
        return servoView;
    }

    @Override
    public void loadUrl(final String url, boolean clearNavigationStack) {
        LOG.d(TAG, "loadUrl called with: " + url + " (clearStack=" + clearNavigationStack + ")");
        currentUrl = url;
        
        // Convert android_asset URLs to localhost HTTP URLs
        String servoUrl = url;
        if (url != null && url.startsWith("file:///android_asset/")) {
            String assetPath = url.substring("file:///android_asset/".length());
            LOG.d(TAG, "Converting android_asset URL. Asset path: " + assetPath);
            
            // Use local HTTP server instead of file:// URLs
            servoUrl = "http://localhost:" + serverPort + "/" + assetPath;
            LOG.d(TAG, "Converted to HTTP URL: " + servoUrl);
        }
        
        LOG.d(TAG, "Loading URL in Servo: " + servoUrl);
        servoView.loadUri(servoUrl);
    }

    @Override
    public String getUrl() {
        return currentUrl;
    }

    @Override
    public void stopLoading() {
        servoView.stop();
    }

    @Override
    public void clearCache() {
        // ServoView doesn't have a direct clearCache method
        LOG.w(TAG, "clearCache not implemented for ServoView");
    }

    @Override
    public void clearHistory() {
        // ServoView doesn't have a direct clearHistory method
        canGoBack = false;
        LOG.w(TAG, "clearHistory not implemented for ServoView");
    }

    @Override
    public boolean canGoBack() {
        return canGoBack;
    }

    /**
     * Go to previous page in history.
     *
     * @return true if we went back, false if we are already at top
     */
    @Override
    public boolean goBack() {
        if (canGoBack) {
            servoView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void setPaused(boolean value) {
        if (value) {
            servoView.onPause();
        } else {
            servoView.onResume();
        }
    }

    @Override
    public void destroy() {
        // Stop the asset server
        if (assetServer != null) {
            assetServer.stop();
            assetServer = null;
        }
        
        // ServoView cleanup
        this.servoView.stop();
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        // Servo doesn't have evaluateJavascript
        LOG.w(TAG, "evaluateJavascript not implemented for ServoView");

        // For now, just log and callback with null
        if (callback != null) {
            callback.onReceiveValue(null);
        }
    }

    /**
     * Handle JavaScript bridge messages from alert()
     */
    protected void handleJsBridgeMessage(String message) {
        LOG.w(TAG, "handleJsBridgeMessage not implemented for ServoView: " + message);
    }
    
    private void startAssetServer() {
        try {
            // Find an available port
            serverPort = findAvailablePort();
            
            assetServer = new AssetHttpServer(serverPort, cordova.getActivity().getAssets());
            assetServer.start();
            
            LOG.d(TAG, "Started asset HTTP server on port: " + serverPort);
        } catch (Exception e) {
            LOG.e(TAG, "Failed to start asset server", e);
        }
    }
    
    private int findAvailablePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException e) {
            LOG.e(TAG, "Failed to find available port", e);
            return 8080; // fallback
        }
    }
    
    /**
     * Simple HTTP server that serves files from Android assets
     */
    private static class AssetHttpServer extends NanoHTTPD {
        private static final String TAG = "AssetHttpServer";
        private final AssetManager assetManager;
        
        public AssetHttpServer(int port, AssetManager assetManager) {
            super(port);
            this.assetManager = assetManager;
        }
        
        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            
            // Remove leading slash
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            
            // Default to index.html if requesting root
            if (uri.isEmpty() || uri.equals("/")) {
                uri = "www/index.html";
            } else if (!uri.startsWith("www/")) {
                uri = "www/" + uri;
            }
            
            LOG.d(TAG, "Serving asset: " + uri);
            
            try {
                InputStream inputStream = assetManager.open(uri);
                String mimeType = getMimeType(uri);
                
                return newChunkedResponse(Response.Status.OK, mimeType, inputStream);
            } catch (IOException e) {
                LOG.e(TAG, "Asset not found: " + uri, e);
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
            }
        }
        
        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".gif")) return "image/gif";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain";
        }
    }
}
