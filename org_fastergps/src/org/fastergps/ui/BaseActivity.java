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
    private Preference mAbout;
    private Preference mDonations;
    private HashMap<String, String> config;

    private void updateCurrentNtpServer(String currentNtpServer) {
        mNtpServer.setSummary(getString(R.string.pref_ntp_server_summary) + " " + currentNtpServer);
        mNtpServerCustom.setText(currentNtpServer);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        // only if android is rooted, else show dialog
        if (Utils.isAndroidRooted(mActivity)) {

            config = Utils.getConfig();

            Utils.logConfig(config);

            addPreferencesFromResource(R.xml.base);

            // find preferences
            mNtpServerCustom = (EditTextPreference) findPreference(getString(R.string.pref_ntp_server_custom_key));
            mNtpServer = (ListPreference) findPreference(getString(R.string.pref_ntp_server_key));
            mAbout = (Preference) findPreference(getString(R.string.pref_about_key));
            mDonations = (Preference) findPreference(getString(R.string.pref_donations_key));

            String currentNtpServer = config.get("NTP_SERVER");
            Log.d(Constants.TAG, "current ntp server: " + currentNtpServer);

            /* set default of list preference and custom ntp server from config */
            if (currentNtpServer != null) {
                updateCurrentNtpServer(currentNtpServer);

                CharSequence[] ntpServerList = mNtpServer.getEntryValues();
                String serverValue;
                for (int i = 0; i < ntpServerList.length; i++) {
                    serverValue = ntpServerList[i].toString();
                    Log.d(Constants.TAG, "possible value: " + serverValue);

                    // if current ntp server is in our list of possible servers
                    if (currentNtpServer.equals(serverValue)) {
                        mNtpServer.setValue(currentNtpServer);
                        mNtpServerCustom.setEnabled(false);
                    } else {
                        mNtpServer.setValue("custom");
                    }
                }
            }

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
                        Utils.logConfig(config);
                        Utils.writeConfig(mActivity, config);
                        updateCurrentNtpServer((String) newValue);
                    }

                    return true;
                }
            });

            /* ntp server drop down */
            mNtpServerCustom.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(Constants.TAG, "mNtpServerCustom changed!");

                    config.put("NTP_SERVER", (String) newValue);
                    Utils.logConfig(config);
                    Utils.writeConfig(mActivity, config);
                    updateCurrentNtpServer((String) newValue);

                    return true;
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