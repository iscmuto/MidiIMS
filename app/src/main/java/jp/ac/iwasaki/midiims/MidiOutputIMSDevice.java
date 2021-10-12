package jp.ac.iwasaki.midiims;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

//Toast Sample start
import android.widget.Toast;
//Toast Sample end

import jp.co.toshiba.iflink.imsif.IfLinkConnector;
import jp.co.toshiba.iflink.imsif.DeviceConnector;
import jp.co.toshiba.iflink.imsif.IfLinkSettings;
import jp.co.toshiba.iflink.imsif.IfLinkAlertException;
import jp.co.toshiba.iflink.ui.PermissionActivity;

public class MidiOutputIMSDevice extends DeviceConnector {
    /**
     * ログ出力用タグ名.
     */
    private static final String TAG = "MIDIOUTPUTIMS-DEV";
    /**
     * メッセージを取得するキー.
     */
    private static final String CHANNEL = "channel";
    /**
     * ログ出力切替フラグ.
     */
    private boolean bDBG = false;

    private Context appContext;
    private MidiManager mMidiManager;
    private MidiInputPort inputPort = null;

    /**
     * コンストラクタ.
     *
     * @param ims IMS
     */
    public MidiOutputIMSDevice(final IfLinkConnector ims) {
        super(ims, MONITORING_LEVEL4, PermissionActivity.class);
        mDeviceName = "MidiOutputDevice";
        mDeviceSerial = "epa";

        mSchemaName = "midioutputimsdevice";
        setSchema();

        mCookie = IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_VALUE_CONFIG
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_VALUE_ALERT
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_TYPE_VALUE_JOB
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_DEVICE + "=" + mDeviceName
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_ADDRESS + "=" + IfLinkConnector.EPA_COOKIE_VALUE_ANY;

        mAssetName = "MIDIOUTPUTDEVICE_EPA";

        // サンプル用：ここでデバイスを登録します。
        // 基本は、デバイスとの接続確立後、デバイスの対応したシリアル番号に更新してからデバイスを登録してください。
        addDevice();
        // 基本は、デバイスとの接続が確立した時点で呼び出します。
        notifyConnectDevice();

        appContext = ims.getApplicationContext();
    }

    @Override
    public boolean onStartDevice() {
        if (bDBG) Log.d(TAG, "onStartDevice");

        mMidiManager = (MidiManager)appContext.getSystemService(Context.MIDI_SERVICE);
        ArrayList<MidiDeviceInfo> midiDevices = getMidiDevices(false); // method defined in snippet above
        if (midiDevices.size() > 0){
            mMidiManager.openDevice(midiDevices.get(0),
            new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    Log.d("MIDI", device.toString());
                    inputPort = device.openInputPort(0);
                }
            },null);
        }

        // 送信開始が別途完了通知を受ける場合には、falseを返してください。
        return true;
    }

    private ArrayList<MidiDeviceInfo> getMidiDevices(boolean isOutput){
        ArrayList filteredMidiDevices = new ArrayList<>();

        for (MidiDeviceInfo midiDevice : mMidiManager.getDevices()){
            if (isOutput){
                if (midiDevice.getOutputPortCount() > 0) filteredMidiDevices.add(midiDevice);
            } else {
                if (midiDevice.getInputPortCount() > 0) filteredMidiDevices.add(midiDevice);
            }
        }
        return filteredMidiDevices;
    }

    @Override
    public boolean onStopDevice() {
        if (bDBG) Log.d(TAG, "onStopDevice");
        // デバイスからのデータ送信停止処理を記述してください。
        try {
            inputPort.close();
        } catch (IOException e) {
        }

        // 送信停止が別途完了通知を受ける場合には、falseを返してください。
        return true;
    }

    @Override
    public boolean onJob(final HashMap<String, Object> map) {
        //Toast Sample start
        if (map.containsKey(CHANNEL) && inputPort != null) {
            final String channelVal = String.valueOf(map.get("channel"));
            final String noteVal = String.valueOf(map.get("note"));
            final String velocityVal = String.valueOf(map.get("velocity"));

            Log.d(TAG, "CH: " + channelVal + " NOTE:" + noteVal + " VEL:" + velocityVal);

            int channel = Integer.valueOf(channelVal);
            int note = Integer.valueOf(noteVal);
            int velocity = Integer.valueOf(velocityVal);

            byte[] buffer = new byte[32];
            buffer[0] = (byte)(0x90 + (channel - 1)); // 鍵盤を押された場合のステータス
            buffer[1] = (byte)note; // ノート番号
            buffer[2] = (byte)velocity; // ベロシティ
            try {
                inputPort.send(buffer, 0, 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Toast Sample end
        return false;
    }

    @Override
    public void enableLogLocal(final boolean enabled) {
        bDBG = enabled;
    }

    @Nullable
    @Override
    protected XmlResourceParser getResourceParser(final Context context) {
        Resources resources = context.getResources();
        if (resources != null) {
            return context.getResources().getXml(R.xml.schema_midioutputdevice);
        } else {
            return null;
        }

    }

    @Override
    protected void onUpdateConfig(@NonNull IfLinkSettings settings) throws IfLinkAlertException {
        if (bDBG) Log.d(TAG, "onUpdateConfig");
        String key = mIms.getString(R.string.pref_midioutputimsdevice_settings_parameter_key);
        int param = settings.getIntValue(key, 1);
        if (bDBG) Log.d(TAG, "parameter[" + key + "] = " + param);
        // 設定パラメータを更新する処理を記述してください。
        // insert routine for reflecting received parameter

    }

    @Override
    protected final String[] getPermissions() {
        if (bDBG) Log.d(TAG, "getPermissions");
        return new String[]{};
    }

    @Override
    protected void onPermissionGranted() {
        // パーミッションを許可された後の処理を記述してください。
    }

    @Override
    public final boolean checkPathConnection() {
        if (bDBG) Log.d(TAG, "checkPathConnection");
        // デバイスとの接続経路(WiFi, BLE, and so on・・・)が有効かをチェックする処理を記述してください。
        return true;
    }

    @Override
    public final boolean reconnectPath() {
        if (bDBG) Log.d(TAG, "reconnectPath");
        // デバイスとの接続経路(WiFi, BLE, and so on・・・)を有効にする処理を記述してください。
        return true;
    }

    @Override
    public final boolean checkDeviceConnection() {
        if (bDBG) Log.d(TAG, "checkDeviceConnection");
        // デバイスとの接続が維持されているかをチェックする処理を記述してください。
        return true;
    }

    @Override
    public final boolean reconnectDevice() {
        if (bDBG) Log.d(TAG, "reconnectDevice");
        // デバイスとの再接続処理を記述してください。
        return true;
    }

    @Override
    public final boolean checkDeviceAlive() {
        if (bDBG) Log.d(TAG, "checkDeviceAlive");
        // デバイスから定期的にデータ受信が出来ているかをチェックする処理を記述してください。
        return true;
    }

    @Override
    public final boolean resendDevice() {
        if (bDBG) Log.d(TAG, "resendDevice");
        // デバイスからのデータ受信を復旧する処理を記述してください。
        return true;
    }
}