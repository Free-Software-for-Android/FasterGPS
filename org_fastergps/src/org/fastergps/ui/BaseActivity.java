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

package org.fastergps.ui;

import java.util.HashMap;

import org.donations.DonationsActivity;
import org.fastergps.R;
import org.fastergps.util.Constants;
import org.fastergps.util.Log;
import org.fastergps.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class BaseActivity extends PreferenceActivity {
    private Activity mActivity;

    private ListPreference mNtpServer;
    private EditTextPreference mNtpServerCustom;

    private Preference mAdvancedSettings;
    private Preference mRevert;

    private Preference mHelp;
    private Preference mAbout;
    private Preference mDonations;

    private HashMap<String, String> config;

    /**
     * Sets summary on preferences based on currentNtpServer
     * 
     * @param currentNtpServer
     */
    private void setSummary(String currentNtpServer) {
        mNtpServer.setSummary(getString(R.string.pref_ntp_server_summary) + "\n"
                + getString(R.string.pref_current_value) + " " + currentNtpServer);
        mNtpServerCustom.setText(currentNtpServer);
    }

    /**
     * Sets NTP Server based on loaded config HashMap
     */
    private void setNtpServerBasedOnConfig() {
        /* set default of list preference and custom ntp server from config */
        String currentNtpServer = config.get("NTP_SERVER");

        if (currentNtpServer != null) {
            setSummary(currentNtpServer);

            boolean ntpServerInList = false;

            CharSequence[] ntpServerList = mNtpServer.getEntryValues();
            String serverValue;
            for (int i = 0; i < ntpServerList.length; i++) {
                serverValue = ntpServerList[i].toString();
                Log.d(Constants.TAG, "possible value: " + serverValue);

                // if current ntp server is in our list of possible servers
                if (currentNtpServer.equals(serverValue)) {
                    mNtpServer.setValue(currentNtpServer);

                    mNtpServerCustom.setEnabled(false);
                    ntpServerInList = true;
                }
            }
            if (ntpServerInList == false) {
                Log.d(Constants.TAG, "current ntp server is not in the list!");
                mNtpServer.setValue("custom");
            }
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        // load preferences from xml
        addPreferencesFromResource(R.xml.base_preference);

        // only if android is rooted, else show dialog
        if (Utils.isAndroidRooted(mActivity)) {

            // load config from /system/etc/gps.conf
            Log.i(Constants.TAG, "Loading /system/etc/gps.conf...");
            config = Utils.getConfig(Constants.GPS_CONF_PATH);
            Utils.debugLogConfig(config);

            // make backup of current config in private files, this backup is used for revert
            Utils.makeBackup(mActivity, config);

            // find preferences
            mNtpServerCustom = (EditTextPreference) findPreference(getString(R.string.pref_ntp_server_custom_key));
            mNtpServer = (ListPreference) findPreference(getString(R.string.pref_ntp_server_key));
            mAdvancedSettings = (Preference) findPreference(getString(R.string.pref_advanced_settings_key));
            mRevert = (Preference) findPreference(getString(R.string.pref_revert_key));
            mHelp = (Preference) findPreference(getString(R.string.pref_help_key));
            mAbout = (Preference) findPreference(getString(R.string.pref_about_key));
            mDonations = (Preference) findPreference(getString(R.string.pref_donations_key));

            // set ntp server based on config
            Log.i(Constants.TAG, "Update displayed NTP Server from config...");
            setNtpServerBasedOnConfig();

            /* ntp server drop down */
            mNtpServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "mNtpServer changed!");

                    if (newValue.equals("custom") || newValue.equals("menu")) {
                        mNtpServerCustom.setEnabled(true);
                    } else {
                        mNtpServerCustom.setEnabled(false);

                        config.put("NTP_SERVER", (String) newValue);
                        Utils.debugLogConfig(config);
                        Utils.writeConfig(mActivity, config);
                        setSummary((String) newValue);
                    }

                    return true;
                }
            });

            /* ntp server custom */
            mNtpServerCustom.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "mNtpServerCustom changed!");

                    config.put("NTP_SERVER", (String) newValue);
                    Utils.debugLogConfig(config);
                    Utils.writeConfig(mActivity, config);
                    setSummary((String) newValue);

                    return true;
                }
            });

            mAdvancedSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(mActivity, AdvancedSettingsActivity.class));

                    return false;
                }

            });

            mRevert.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setPositiveButton(mActivity.getString(R.string.button_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // load backup from /data/data/org.fasterfix/files/old_gps.conf
                                    config = Utils.getConfig(mActivity.getFileStreamPath(
                                            Constants.OLD_GPS_CONF).getAbsolutePath());
                                    // update display
                                    setNtpServerBasedOnConfig();
                                    // write old config
                                    Utils.writeConfig(mActivity, config);
                                }
                            });
                    builder.setNegativeButton(mActivity.getString(R.string.button_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    builder.setTitle(R.string.revert_dialog_title);
                    builder.setMessage(mActivity.getString(R.string.revert_dialog));
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return false;
                }

            });

            mHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(mActivity, HelpActivity.class));

                    return false;
                }

            });

            mAbout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(mActivity, AboutActivity.class));

                    return false;
                }

            });

            mDonations.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(mActivity, DonationsActivity.class));

                    return false;
                }

            });

        }
    }
}