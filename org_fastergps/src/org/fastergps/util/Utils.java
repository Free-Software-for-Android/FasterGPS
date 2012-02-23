/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This file is part of FasterGPS.
 * 
 * FasterGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FasterGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FasterGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.fastergps.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastergps.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.stericson.RootTools.RootTools;

public class Utils {

    /**
     * Check if Android is rooted, check for su binary and busybox and display possible solutions if
     * they are not available
     * 
     * @param activity
     * @return true if phone is rooted
     */
    public static boolean isAndroidRooted(final Activity activity) {
        boolean rootAvailable = false;

        // root check can be disabled for debugging in emulator
        if (Constants.DEBUG_DISABLE_ROOT_CHECK) {
            rootAvailable = true;
        } else {
            // check for root on device and call su binary
            try {
                if (!RootTools.isAccessGiven()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setCancelable(false);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle(activity.getString(R.string.no_root_title));

                    // build view from layout
                    LayoutInflater factory = LayoutInflater.from(activity);
                    final View dialogView = factory.inflate(R.layout.no_root_dialog, null);
                    builder.setView(dialogView);

                    builder.setNeutralButton(activity.getResources()
                            .getString(R.string.button_exit),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    activity.finish(); // finish current activity, means exiting app
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    rootAvailable = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                rootAvailable = false;
            }
        }

        return rootAvailable;
    }

    /**
     * Gets resource string from strings.xml
     * 
     * @param name
     * @param context
     * @return
     */
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string",
                context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }

    /*
     * simple parsing regex for key value pairs in gps.conf
     */
    static final private String GPS_CONF_REGEX = "^\\s*(\\S+)\\s*\\=\\s*(\\S+)\\s*(?:\\#.*)*\\s*$";
    private static Pattern mGpsConfPattern;
    private static Matcher mGpsConfMatcher;

    static {
        mGpsConfPattern = Pattern.compile(GPS_CONF_REGEX);
    }

    /**
     * Opens current gps.conf and parses the config into a HashMap
     * 
     * @return config as HashMap
     */
    public static HashMap<String, String> getConfig() {

        HashMap<String, String> currentConfig = new HashMap<String, String>();

        // read gps.conf line by line and parse it into config hash map
        try {
            FileInputStream fstream = new FileInputStream(Constants.GPS_CONF_PATH);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String nextLine = new String();
            String key = new String();
            String value = new String();
            while ((nextLine = reader.readLine()) != null) {
                mGpsConfMatcher = mGpsConfPattern.matcher(nextLine);

                if (mGpsConfMatcher.matches()) {
                    key = mGpsConfMatcher.group(1);
                    value = mGpsConfMatcher.group(2);

                    currentConfig.put(key, value);
                } else {
                    Log.d(Constants.TAG, "Line does not match: " + nextLine);
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(Constants.TAG, "IO Exception");
            e.printStackTrace();
        }

        return currentConfig;
    }

    /**
     * Writes config to private files and then copies it using RootTools to system partition
     * 
     * @param config
     * @return true if writing worked
     */
    public static boolean writeConfig(Context context, HashMap<String, String> config) {

        /* remount for write access */
        Log.i(Constants.TAG, "Remounting for RW...");
        if (!RootTools.remount(Constants.GPS_CONF_PATH, "RW")) {
            Log.e(Constants.TAG, "remount failed!");

            return false;
        }

        Log.i(Constants.TAG, "Writing gps.conf to private files...");

        try {
            FileOutputStream fos = context.openFileOutput(Constants.GPS_CONF, Context.MODE_PRIVATE);
            OutputStreamWriter ow = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(ow);

            // write every key value pair from config
            Iterator<Entry<String, String>> it = config.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<String, String> pairs = it.next();

                Log.d(Constants.TAG, pairs.getKey() + " = " + pairs.getValue());

                // write only if pair has a value
                if (pairs.getValue() != null && !pairs.getValue().equals("")) {
                    writer.write(pairs.getKey() + "=" + pairs.getValue() + Constants.LINE_SEPERATOR);
                }
            }

            // Close the output stream
            writer.close();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error while writing gps.conf: " + e.getMessage());
            e.printStackTrace();

            return false;
        }

        Log.i(Constants.TAG, "Copying gps.conf from private files to system partition...");

        String privateDir = context.getFilesDir().getAbsolutePath();
        String privateFile = privateDir + Constants.FILE_SEPERATOR + Constants.GPS_CONF;

        List<String> output = null;
        try {

            // copy file from /data/data/org.fastergps/gps.conf to /system/etc/gps.conf and make
            // chmod 644 on it
            output = RootTools.sendShell(new String[] {
                    Constants.COMMAND_COPY + " " + privateFile + " " + Constants.GPS_CONF_PATH,
                    Constants.COMMAND_CHMOD_644 + " " + Constants.GPS_CONF_PATH }, 1, -1);

            Log.d(Constants.TAG, "output of sendShell commands: " + output.toString());
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception: " + e);
            e.printStackTrace();

            return false;
        } finally {
            // after all remount system back as read only
            RootTools.remount(Constants.GPS_CONF_PATH, "RO");
        }

        return true;
    }

    /**
     * Returns HashMap with possible keys for gps.conf
     * 
     * @return
     */
    public static HashMap<String, String> getPossibleConfig() {
        HashMap<String, String> possibleConfig = new HashMap<String, String>();
        possibleConfig.put("NTP_SERVER", "");
        possibleConfig.put("XTRA_SERVER_1", "");
        possibleConfig.put("XTRA_SERVER_2", "");
        possibleConfig.put("XTRA_SERVER_3", "");
        possibleConfig.put("XTRA_SERVER_4", "");
        possibleConfig.put("XTRA_SERVER_5", "");

        possibleConfig.put("DEBUG_LEVEL", "");
        possibleConfig.put("INTERMEDIATE_POS", "");
        possibleConfig.put("ACCURACY_THRES", "");
        possibleConfig.put("REPORT_POSITION_USE_SUPL_REFLOC", "");
        possibleConfig.put("ENABLE_WIPER", "");

        possibleConfig.put("SUPL_HOST", "");
        possibleConfig.put("SUPL_PORT", "");
        possibleConfig.put("SUPL_NO_SECURE_PORT", "");
        possibleConfig.put("SUPL_SECURE_PORT", "");

        possibleConfig.put("C2K_HOST", "");
        possibleConfig.put("C2K_PORT", "");

        possibleConfig.put("CURRENT_CARRIER", "");
        possibleConfig.put("DEFAULT_AGPS_ENABLE", "");
        possibleConfig.put("DEFAULT_SSL_ENABLE", "");
        possibleConfig.put("DEFAULT_USER_PLANE", "");

        return possibleConfig;
    }

    /**
     * Displays config with Logcat when DEBUG is enabled
     * 
     * @param config
     */
    public static void debugLogConfig(HashMap<String, String> config) {
        if (Constants.DEBUG) {
            // print config
            Iterator<String> iterator = config.keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = config.get(key).toString();

                Log.d(Constants.TAG, key + " = " + value);
            }
        }
    }

}
