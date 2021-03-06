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

    private Preference mCurrentNtp;
    private ListPreference mNtpContinent;
    private ListPreference mNtpRegion;
    private EditTextPreference mNtpCustom;

    private Preference mAdvancedSettings;
    private Preference mRevert;

    private Preference mHelp;

    private HashMap<String, String> config;

    /**
     * Sets summary on preferences based on currentNtpServer
     * 
     * @param currentNtpServer
     */
    private void setCurrentNtpTitle(String currentNtpServer) {
        mCurrentNtp.setTitle(getString(R.string.pref_ntp_current) + " " + currentNtpServer);
        mNtpCustom.setText(currentNtpServer);
    }

    /**
     * Sets summary of ListPreference based on new value
     * 
     * @param listPref
     * @param newValue
     */
    private void setListPreferenceSummary(ListPreference listPref, String newValue) {
        String newEntry = null;

        if (newValue != null) {
            int index = listPref.findIndexOfValue(newValue.toString());
            if (index != -1) {
                newEntry = (String) listPref.getEntries()[index];
            }

            if (newValue != null && !newValue.equals("null")) {
                listPref.setSummary(getString(R.string.pref_current_value) + " " + newEntry);
            }
        }
    }

    /**
     * Sets NTP Server based on loaded config HashMap
     */
    private void setCurrentNtpBasedOnConfig() {
        /* set default of list preference and custom ntp server from config */
        String currentNtpServer = config.get("NTP_SERVER");

        if (currentNtpServer != null) {
            setCurrentNtpTitle(currentNtpServer);
        }
    }

    /**
     * Set values of continent and region list based on NTP server from config
     */
    private void loadValuesFromConfig() {
        /* set default of list preference and custom ntp server from config */
        String currentNtpServer = config.get("NTP_SERVER");

        Log.d(Constants.TAG, "val: " + mNtpContinent.getValue());

        if (currentNtpServer != null) {

            boolean ntpServerInList = false;

            String[] continents = Utils.getResourceStringArray("pref_ntp_continent_entries_values",
                    mActivity);

            for (int i = 0; i < continents.length; i++) {
                String regionsArray = "pref_ntp_region_entries_" + continents[i];
                String regionsValuesArray = "pref_ntp_region_entries_" + continents[i] + "_values";

                Log.d(Constants.TAG, "current res string: " + regionsValuesArray);

                try {
                    String[] regions = Utils.getResourceStringArray(regionsValuesArray, mActivity);

                    for (int j = 0; j < regions.length; j++) {
                        Log.d(Constants.TAG, "Current region: " + regions[j]);

                        if (currentNtpServer.equals(regions[j])) {
                            Log.d(Constants.TAG, "Found NTP list value: " + regions[j]);

                            // select continent value
                            mNtpContinent.setValue(continents[i]);

                            // select region value
                            // load entries and entry values based on selection
                            mNtpRegion.setEntries(Utils.getResourceStringArray(regionsArray,
                                    mActivity));
                            mNtpRegion.setEntryValues(Utils.getResourceStringArray(
                                    regionsValuesArray, mActivity));
                            mNtpRegion.setEnabled(true);
                            mNtpRegion.setValue(regions[j]);

                            ntpServerInList = true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.d(Constants.TAG, "Region entries could not be loaded");
                }
            }

            if (ntpServerInList == false) {
                Log.d(Constants.TAG, "current ntp server is not in the list!");
                mNtpContinent.setValue("custom");
                mNtpCustom.setEnabled(true);
            }
        } else {
            Log.d(Constants.TAG, "no current ntp server is set!");

            mNtpContinent.setValue("null");
        }

        // update list prefs summaries based on new selection
        setListPreferenceSummary(mNtpContinent, mNtpContinent.getValue());
        setListPreferenceSummary(mNtpRegion, mNtpRegion.getValue());
    }

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings("deprecation")
    // suppress deprecation warnings to be backward compatible
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
            mCurrentNtp = (Preference) findPreference(getString(R.string.pref_ntp_current_key));
            mNtpCustom = (EditTextPreference) findPreference(getString(R.string.pref_ntp_custom_key));
            mNtpContinent = (ListPreference) findPreference(getString(R.string.pref_ntp_continent_key));
            mNtpRegion = (ListPreference) findPreference(getString(R.string.pref_ntp_region_key));
            mAdvancedSettings = (Preference) findPreference(getString(R.string.pref_advanced_settings_key));
            mRevert = (Preference) findPreference(getString(R.string.pref_revert_key));
            mHelp = (Preference) findPreference(getString(R.string.pref_help_key));

            // set current ntp summary based on config
            setCurrentNtpBasedOnConfig();

            loadValuesFromConfig();

            /* ntp continent drop down */
            mNtpContinent.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "ntp continent changed!");
                    String stringValue = (String) newValue;

                    if (stringValue.equals("custom")) {
                        mNtpCustom.setEnabled(true);
                        mNtpRegion.setEnabled(false);
                    } else if (stringValue.equals("null")) {
                        mNtpCustom.setEnabled(false);
                        mNtpRegion.setEnabled(false);
                    } else {
                        mNtpCustom.setEnabled(false);

                        // load entries and entry values based on selection
                        mNtpRegion.setEntries(Utils.getResourceStringArray(
                                "pref_ntp_region_entries_" + stringValue, mActivity));
                        mNtpRegion.setEntryValues(Utils.getResourceStringArray(
                                "pref_ntp_region_entries_" + stringValue + "_values", mActivity));
                        mNtpRegion.setEnabled(true);
                        // select default entry
                        mNtpRegion.setValue("null");
                    }

                    setListPreferenceSummary(mNtpContinent, stringValue);

                    return true;
                }
            });

            /* ntp region drop down */
            mNtpRegion.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "ntp region changed!");
                    String stringValue = (String) newValue;

                    if (!stringValue.equals("null")) {
                        config.put("NTP_SERVER", (String) newValue);
                        Utils.debugLogConfig(config);
                        Utils.writeConfig(mActivity, config);
                        setCurrentNtpTitle((String) newValue);
                    }

                    setListPreferenceSummary(mNtpRegion, stringValue);

                    return true;
                }
            });

            /* ntp server custom */
            mNtpCustom.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "ntp custom changed!");

                    config.put("NTP_SERVER", (String) newValue);
                    Utils.debugLogConfig(config);
                    Utils.writeConfig(mActivity, config);
                    setCurrentNtpTitle((String) newValue);

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
                                    // load backup from /data/data/org.fastergps/files/old_gps.conf
                                    config = Utils.getConfig(mActivity.getFileStreamPath(
                                            Constants.OLD_GPS_CONF).getAbsolutePath());
                                    // update display
                                    setCurrentNtpBasedOnConfig();
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

        }
    }
}