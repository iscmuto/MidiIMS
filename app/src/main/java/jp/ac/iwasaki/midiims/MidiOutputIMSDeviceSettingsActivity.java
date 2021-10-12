package jp.ac.iwasaki.midiims;

import android.content.Intent;

import androidx.annotation.NonNull;

import jp.co.toshiba.iflink.ui.BaseSettingsActivity;

public class MidiOutputIMSDeviceSettingsActivity extends BaseSettingsActivity {
    /**
     * Preferences名.
     */
    public static final String PREFERENCE_NAME
            = "jp.ac.iwasaki.midiims";

    @Override
    protected final int getPreferencesResId() {
        return R.xml.pref_midioutputimsdevice;
    }

    @NonNull
    @Override
    protected final String getPreferencesName() {
        return PREFERENCE_NAME;
    }

    @Override
    protected final Intent getIntentForService() {
        Intent intent = new Intent(
                getApplicationContext(),
                MidiOutputIMSIms.class);
        intent.setPackage(getClass().getPackage().getName());
        return intent;
    }
}