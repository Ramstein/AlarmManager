package com.makrov.alarmmanager;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RegisterUser extends AsyncTask<String, Void, String> {


    private static String macAddrWlan0 = getMacAddrWlan0();

    private static String getMacAddrWlan0() {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null) return "";
            StringBuilder buf = new StringBuilder();
            for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
            if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected String doInBackground(String... Strings) {
        try {
            String n_url = "https://youtvbe.co/RegisterUser.php";
            Object localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);

            String data = URLEncoder.encode("macAddressWlan0", "UTF-8") + "=" + URLEncoder.encode(macAddrWlan0, "UTF-8");
            data += "&" + URLEncoder.encode("userRegisteredOn", "UTF-8") + "=" + URLEncoder.encode((String) localObject1, "UTF-8");

            URL url = new URL(n_url);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write(data);
            outputStreamWriter.flush();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return bufferedReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);
        if (aVoid.equals("success")) {
            Log.e("CheckUserRegistered", aVoid);
        } else {
            Log.e("CheckUserRegistered", "failed");
        }
    }

}
