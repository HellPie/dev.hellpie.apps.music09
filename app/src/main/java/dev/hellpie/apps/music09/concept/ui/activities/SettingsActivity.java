/*
 * Copyright 2017 Diego Rossi (@_HellPie)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.hellpie.apps.music09.concept.ui.activities;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import dev.hellpie.apps.music09.concept.BuildConfig;
import dev.hellpie.apps.music09.concept.R;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Show a Toolbar/ActionBar on top of the activity with a back arrow instead of an hamburger
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

		// Load and display the settings fragment if we're starting clean
		if(savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, new SettingsFragment())
					.commit();
		}
	}

	public static final class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

		private SwitchPreference debugToggle;
		private Preference debugTuner;

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load preferences layout and settings from XML resource
			addPreferencesFromResource(R.xml.preferences);

			// Remove Debug Tuner from the preference screen to avoid exposing unwanted toggles
			debugTuner = findPreference(getString(R.string.preferences_debug_tuner_key));
			getPreferenceScreen().removePreference(debugTuner);

			// Only enable the debug preferences when in debug builds
			debugToggle = (SwitchPreference) findPreference(getString(R.string.preferences_debug_enable_key));
			if(BuildConfig.DEBUG) {
				Preference debug = findPreference(getString(R.string.preferences_debug_key));
				if(debug != null) debug.setEnabled(true);

				// If we can, show the Debug Tuner and register a listener to disable it
				if(debugToggle != null) {
					if(debugToggle.isChecked()) getPreferenceScreen().addPreference(debugTuner);
					debugToggle.setOnPreferenceChangeListener(this);
				}
			} else {

				// Hide the switch
				if(debugToggle != null) debugToggle.setChecked(false);

				// Disable debug in the preferences
				Context context = getActivity().getApplicationContext();
				PreferenceManager.getDefaultSharedPreferences(context).edit()
						.putBoolean(context.getString(R.string.preferences_debug_key), false)
						.apply();
			}

			// Set listener to dynamically apply description to update timer preference
			Preference updates = findPreference(getString(R.string.preferences_update_timer_key));
			if(updates != null) updates.setOnPreferenceChangeListener(this);
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(getString(R.string.preferences_update_timer_key).equals(key)) {
				Resources resources = getResources();
				String[] timers = resources.getStringArray(R.array.preferences_update_timer_entryValues);
				String[] values = resources.getStringArray(R.array.preferences_update_timer_summaryValues);

				// Dynamically update the text in the summary
				// Using a for loop is super-inefficient but allows for extreme modularity
				for(int i = 0; i < timers.length; i++) {
					if(timers[i].equals(newValue)) {
						preference.setSummary(values[i]);
						break;
					}
				}
			} else if(getString(R.string.preferences_debug_enable_key).equals(key)){
				if(!(newValue instanceof Boolean) || debugTuner == null) return false;
				if((boolean) newValue) { // Debug switch has been enabled, add Debug Tuner
					getPreferenceScreen().addPreference(debugTuner);
				} else { // Debug switch has been disabled, remove Debug Tuner
					getPreferenceScreen().removePreference(debugTuner);
				}
			}

			// Update the preference
			return true;
		}
	}
}
