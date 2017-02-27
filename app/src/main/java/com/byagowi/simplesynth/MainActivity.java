package com.byagowi.simplesynth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MidiSynthesizerService.class));
        LinearLayout parentLayout = (LinearLayout) findViewById(R.id.instruments);

        ArrayAdapter<String> instrumentsListAdapter =
                new ArrayAdapter<>(this, R.layout.instruments_select_item, INSTRUMENTS);

        for (int i = 0; i < 16; ++i) {
            int channelId = i;

            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            parentLayout.addView(ll);

            TextView cMajor = new TextView(this);
            cMajor.setText("C");
            cMajor.setOnClickListener(v -> playChord(channelId, 48, 52, 55));
            cMajor.setPadding(0, 0, 5, 0);
            cMajor.setTextSize(25);
            ll.addView(cMajor);

            TextView gMajor = new TextView(this);
            gMajor.setText("G");
            gMajor.setOnClickListener(v -> playChord(channelId, 55, 59, 62));
            gMajor.setPadding(5, 0, 10, 0);
            gMajor.setTextSize(25);
            ll.addView(gMajor);

            TextView channelText = new TextView(this);
            channelText.setText("Channel " + (channelId + 1) + ":");
            ll.addView(channelText);

            if (channelId == 9) {
                channelText.setText("Channel 10, dedicated to effects");
                channelText.setEnabled(false);
            } else {
                channelText.setTextSize(10);

                Spinner s = new Spinner(this);
                s.setAdapter(instrumentsListAdapter);
                s.setOnItemSelectedListener(new ProgramChangeClickListener(channelId));

                ll.addView(s);
            }
        }
    }

    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;

    private void playChord(int channelId, int... notes) {
        Log.d(TAG, "chord play issued");
        new Thread(() -> {
            try {
                byte[] msg = new byte[3];
                for (int note : notes) {
                    msg[0] = (byte) (NOTE_ON + channelId); // NOTE_ON
                    msg[1] = (byte) note;
                    msg[2] = (byte) 127;
                    MidiSynthesizerService.write(msg);
                    Thread.sleep(200);
                    msg[0] = (byte) (NOTE_OFF + channelId); // NOTE_OFF
                    msg[1] = (byte) note;
                    msg[2] = (byte) 0;
                    MidiSynthesizerService.write(msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static final int PROGRAM_CHANGE = 0xC0;

    private static class ProgramChangeClickListener implements AdapterView.OnItemSelectedListener {
        private final int channelId;

        ProgramChangeClickListener(int channelId) { this.channelId = channelId; }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "program change issued");

            byte[] msg = new byte[2];
            msg[0] = (byte) (PROGRAM_CHANGE + channelId);
            msg[1] = (byte) (position - 1);
            MidiSynthesizerService.write(msg);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    public static final String[] INSTRUMENTS = new String[]{"Acoustic Grand Piano",
            "Bright Acoustic Piano", "Electric Grand Piano", "Honky Tonk Piano", "Electric Piano 0",
            "Electric Piano 1", "Harpsichord", "Clavi", "Celesta", "Glockenspiel", "Music Box",
            "Vibraphone", "Marimba", "Xylophone", "Tubular Bells", "Dulcimer", "Drawbar Organ",
            "Percussive Organ", "Rock Organ", "Church Organ", "Reed Organ", "Accordion",
            "Harmonica", "Tango Accordion", "Acoustic Guitar Nylon", "Acoustic Guitar Steel",
            "Electric Guitar Jazz", "Electric Guitar Clean", "Electric Guitar Muted",
            "Overdriven Guitar", "Distortion Guitar", "Guitar Harmonics", "Acoustic Bass",
            "Electric Bass Finger", "Electric Bass Pick", "Fretless Bass", "Slap Bass 0",
            "Slap Bass 1", "Synth Bass 0", "Synth Bass 1", "Violin", "Viola", "Cello", "Contrabass",
            "Tremolo Strings", "Pizzicato Strings", "Orchestral Harp", "Timpani",
            "String Ensemble 0", "String Ensemble 1", "Synthstrings 0", "Synthstrings 1",
            "Choir Aahs", "Voice Oohs", "Synth Voice", "Orchestra Hit", "Trumpet", "Trombone",
            "Tuba", "Muted Trumpet", "French Horn", "Brass Section", "Synthbrass 0", "Synthbrass 1",
            "Soprano", "Alto Sax", "Tenor Sax", "Baritone Sax", "Oboe", "English Horn", "Bassoon",
            "Clarinet", "Piccolo", "Flute", "Recorder", "Pan Flute", "Blown Bottle", "Shakuhachi",
            "Whistle", "Ocarina", "Lead 0 Square", "Lead 1 Sawtooth", "Lead 2 Calliope",
            "Lead 3 Chiff", "Lead 4 Charang", "Lead 5 Voice", "Lead 6 Fifths", "Lead 7 Bass Lead",
            "Pad 0 New Age", "Pad 1 Warm", "Pad 2 Polysynth", "Pad 3 Choir", "Pad 4 Bowed",
            "Pad 5 Metallic", "Pad 6 Halo", "Pad 7 Sweep", "Fx 0 Rain", "Fx 1 Soundtrack",
            "Fx 2 Crystal", "Fx 3 Atmosphere", "Fx 4 Brightness", "Fx 5 Goblins", "Fx 6 Echoes",
            "Fx 7 Sci Fi", "Sit R", "Banjo", "Shamisen", "Koto", "Kalimba", "Bag Pipe", "Fiddle",
            "Shanai", "Tinkle Bell", "Agogo", "Steel Drums", "Woodblock", "Taiko Drum",
            "Melodic Tom", "Synth Drum", "Reverse Cymbal", "Guitar Fret Noise", "Breath Noise",
            "Seashore", "Bird Tweet", "Telephone Ring", "Helicopter", "Applause", "Gunshot"};

}
