package com.byagowi.simplesynth;

import android.media.midi.MidiDevice.MidiConnection;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceInfo.PortInfo;
import android.media.midi.MidiDeviceService;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.billthefarmer.mididriver.MidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MidiSynthesizerService extends MidiDeviceService {
    private static String TAG = "MidiSynthesizerService";

    private MidiDriver mMidiSynthesizer;
    private static MidiDriver staticSynthesizerHolder;
    private MidiManager mMidiManager;
    private MidiInputPort mSynthesizerInputPort;
    private Map<PortInfo, MidiConnection> openConnections = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mMidiSynthesizer = new MidiDriver();
        mMidiSynthesizer.setOnMidiStartListener(() -> {
            staticSynthesizerHolder = mMidiSynthesizer;
        });
        mMidiSynthesizer.start();

        mMidiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        MidiDeviceInfo synthesizerDeviceInfo = getDeviceInfo();
        mMidiManager.openDevice(synthesizerDeviceInfo, destinationDevice -> {
            mSynthesizerInputPort = destinationDevice.openInputPort(0);

            for (MidiDeviceInfo info : mMidiManager.getDevices())
                if (info.getId() != synthesizerDeviceInfo.getId())
                    connectedDeviceToSynth(info);

            mMidiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
                @Override
                public void onDeviceAdded(MidiDeviceInfo info) {
                    Log.i(TAG, "new device added");
                    connectedDeviceToSynth(info);
                }

                @Override
                public void onDeviceRemoved(MidiDeviceInfo info) {
                    if (openConnections.containsKey(info)) {
                        try {
                            openConnections.get(info).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        openConnections.remove(info);
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        }, null);
    }

    public static void write(byte[] msg) {
        if (staticSynthesizerHolder != null) staticSynthesizerHolder.write(msg);
    }

    private void connectedDeviceToSynth(MidiDeviceInfo info) {
        mMidiManager.openDevice(info, device -> {
            for (PortInfo p : info.getPorts())
                if (p.getType() == PortInfo.TYPE_INPUT) {
                    MidiConnection midiConnection =
                            device.connectPorts(mSynthesizerInputPort, p.getPortNumber());
                    openConnections.put(p, midiConnection);
                }
        }, null);
    }

    public static final byte STATUS_COMMAND_MASK = (byte) 0xF0;
    public static final byte STATUS_CHANNEL_MASK = (byte) 0x0F;

    String formatBytesToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            sb.append(String.format("%02X ", b));
        return sb.toString();
    }

    private MidiReceiver[] mMidiReceivers = new MidiReceiver[]{new MidiReceiver() {
        @Override
        public void onSend(byte[] data, int offset, int count, long timestamp)
                throws IOException {
            byte[] msg = Arrays.copyOfRange(data, offset, offset + count);
            mMidiSynthesizer.write(msg);

            byte command = (byte) (msg[0] & STATUS_COMMAND_MASK);
            if (command == MidiConstants.CONTROL_CHANGE)
                Log.i(TAG, "Special command: " + formatBytesToString(msg));
        }
    }};

    @Override
    public MidiReceiver[] onGetInputPortReceivers() {
        return mMidiReceivers;
    }

    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) { }

    @Override
    public void onDestroy() {
        mMidiSynthesizer.stop();
        super.onDestroy();
    }

}