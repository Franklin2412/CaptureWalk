package com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.BuildConfig;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.w3c.dom.Document;

public class HttpConnection {
    public Bitmap DownloadImage(String URL) throws OutOfMemoryError, UnknownHostException, Exception, SecurityException {
        Options bfOptions = new Options();
        bfOptions.inDither = false;
        bfOptions.inPurgeable = true;
        bfOptions.inInputShareable = true;
        bfOptions.inTempStorage = new byte[AccessibilityNodeInfoCompat.ACTION_PASTE];
        InputStream in = OpenHttpConnection(URL);
        System.out.println("Inputstream : " + in);
        return BitmapFactory.decodeStream(in, null, bfOptions);
    }

    public InputStream OpenHttpConnection(String urlString) throws IOException, SecurityException, Exception {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(urlString).openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("GET");
        httpConn.connect();
        if (httpConn.getResponseCode() == Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
            return httpConn.getInputStream();
        }
        return null;
    }

    public String convertStreamToString(InputStream is) throws IOException {
        if (is == null) {
            return BuildConfig.FLAVOR;
        }
        Writer writer = new StringWriter();
        char[] buffer = new char[Barcode.UPC_E];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while (true) {
                int n = reader.read(buffer);
                if (n == -1) {
                    break;
                }
                writer.write(buffer, 0, n);
                System.out.println("Response is " + writer.toString());
            }
            return writer.toString();
        } finally {
            is.close();
        }
    }

    public Document DownloadXML(String URL) throws IOException, SecurityException, Exception {
        InputStream in = OpenHttpConnection(URL);
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        docBuilder.isValidating();
        Document doc = docBuilder.parse(in);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public JSONArray downloadJSON(String URL) throws SecurityException, IOException, Exception {
        return new JSONArray(convertStreamToString(OpenHttpConnection(URL)));
    }

    public static boolean isNetworkAvailable(Activity ctx) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) ctx.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public Bitmap downloadOriginalImage(String URL) {
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return BitmapFactory.decodeStream(in);
    }
}
