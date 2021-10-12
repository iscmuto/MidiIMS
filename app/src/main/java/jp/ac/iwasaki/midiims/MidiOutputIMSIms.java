package jp.ac.iwasaki.midiims;

import android.Manifest;

import androidx.annotation.NonNull;

import android.util.Log;

import java.util.Set;

import jp.co.toshiba.iflink.epaapi.EPADevice;
import jp.co.toshiba.iflink.imsif.DeviceConnector;
import jp.co.toshiba.iflink.imsif.IfLinkConnector;
import jp.co.toshiba.iflink.ui.PermissionActivity;

public class MidiOutputIMSIms extends IfLinkConnector {
    /**
     * ログ出力用タグ名
     */
    private static final String TAG = "MIDIOUTPUTIMS-IMS";
    /**
     * ログ出力レベル：CustomDevice
     */
    private static final String LOG_LEVEL_CUSTOM_DEV = "CUSTOM-DEV";
    /**
     * ログ出力レベル：CustomDevice
     */
    private static final String LOG_LEVEL_CUSTOM_IMS = "CUSTOM-IMS";

    /**
     * ログ出力切替フラグ
     */
    private boolean bDBG = false;

    /**
     * コンストラクタ.
     */
    public MidiOutputIMSIms() {
        super("MidiOutputIMSIms");
    }

    @NonNull
    @Override
    protected final String getPreferencesName() {
        return MidiOutputIMSDeviceSettingsActivity.PREFERENCE_NAME;
    }

    @Override
    protected final void updateLogLevelSettings(final Set<String> settings) {
        if (bDBG) Log.d(TAG, "LogLevel settings=" + settings);
        super.updateLogLevelSettings(settings);

        boolean isEnabledLog = false;
        if (settings.contains(LOG_LEVEL_CUSTOM_IMS)) {
            isEnabledLog = true;
        }
        bDBG = isEnabledLog;

        isEnabledLog = false;
        if (settings.contains(LOG_LEVEL_CUSTOM_DEV)) {
            isEnabledLog = true;
        }
        for (DeviceConnector device : mDeviceList) {
            device.enableLogLocal(isEnabledLog);
        }
    }

    @Override
    protected void onActivationResult(final boolean result,
                                      final EPADevice epaDevice) {
        MidiOutputIMSDevice dev = new MidiOutputIMSDevice(this);
    }

    @Override
    protected final String[] getPermissions() {
        if (bDBG) Log.d(TAG, "getPermissions");
        // サンプル用：BLE利用の場合のコードを生成しています。
        return new String[]{};
    }

    @Override
    protected void onPermissionGranted() {
        // パーミッションを許可された場合の処理を記述してください。
    }

    @Override
    protected Class getPermissionActivityClass() {
        return PermissionActivity.class;
    }
}