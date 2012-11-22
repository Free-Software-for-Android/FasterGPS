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

import java.io.IOException;
import java.io.InputStream;

import org.fastergps.R;
import org.fastergps.util.Constants;
import org.fastergps.util.Log;

import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.JellyBeanSpanFixTextView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class HelpHtmlFragment extends Fragment {
    private Activity mActivity;

    /**
     * Workaround for Android Bug. See
     * http://stackoverflow.com/questions/8748064/starting-activity-from
     * -fragment-causes-nullpointerexception
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // load html from html file from /res/raw
        InputStream inputStreamText = this.getActivity().getResources().openRawResource(R.raw.help);

        mActivity = getActivity();

        LinearLayout layout = new LinearLayout(mActivity);

        JellyBeanSpanFixTextView text = new JellyBeanSpanFixTextView(mActivity);

        // padding
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mActivity
                .getResources().getDisplayMetrics());
        text.setPadding(padding, padding, padding, 0);

        layout.addView(text);

        // load html into textview
        HtmlSpanner htmlSpanner = new HtmlSpanner();
        htmlSpanner.setStripExtraWhiteSpace(true);
        try {
            text.setText(htmlSpanner.fromHtml(inputStreamText));
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error while reading raw resources as stream", e);
        }

        // make links work
        text.setMovementMethod(LinkMovementMethod.getInstance());

        // no flickering when clicking textview for Android < 4
        text.setTextColor(getResources().getColor(android.R.color.secondary_text_dark_nodisable));

        return layout;
    }
}