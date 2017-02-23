package com.byagowi.simplesynth;

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

import org.billthefarmer.mididriver.MidiDriver;

import java.io.IOException;

public class MidiSynthesizerService extends MidiDeviceService {
    private static String TAG = MidiSynthesizerService.class.getName();

    private MidiDriver mMidiSynthesizer = new MidiDriver();
    private MidiManager mMidiManager;
    private MidiInputPort mSynthesizerInputPort;

    @Override
    public void onCreate() {
        super.onCreate();
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
                    connectedDeviceToSynth(info);
                }

                @Override
                public void onDeviceRemoved(MidiDeviceInfo info) { }
            }, new Handler(Looper.getMainLooper()));
        }, null);
    }

    private void connectedDeviceToSynth(MidiDeviceInfo info) {
        mMidiManager.openDevice(info, device -> {
            for (PortInfo p : info.getPorts())
                if (p.getType() == PortInfo.TYPE_INPUT)
                    device.connectPorts(mSynthesizerInputPort, p.getPortNumber());
        }, null);
    }

    private MidiReceiver[] mMidiReceivers = new MidiReceiver[]{new MidiReceiver() {
        @Override
        public void onSend(byte[] data, int offset, int count, long timestamp)
                throws IOException {
            Log.i(TAG, "midi event");
            mMidiSynthesizer.write(data);
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