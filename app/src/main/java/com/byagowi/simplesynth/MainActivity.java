package com.byagowi.simplesynth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MidiSynthesizerService.class));
        LinearLayout ll = (LinearLayout) findViewById(R.id.instruments);
        LocalBroadcastManager localBroadcast = LocalBroadcastManager.getInstance(getApplicationContext());

        for (int i = 0; i < 16; ++i) {
            TextView tv = new TextView(this);
            int channelId = i;
            tv.setText("Channel " + (channelId + 1) + ":");
            ll.addView(tv);

            Spinner s = new Spinner(this);
            s.setAdapter(new ArrayAdapter<>(this, R.layout.instruments_select_item, INSTRUMENTS));

            if (channelId == 9) { // 10th channel is dedicated to music box
                s.setSelection(10);
                s.setEnabled(false);
            }

            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MidiSynthesizerService.INSTRUMENT_CHANGE_MSG);
                    intent.putExtra(MidiSynthesizerService.CHANNEL_ID_MSG_FIELD, channelId);
                    intent.putExtra(MidiSynthesizerService.SELECTED_INSTRUMENT_MSG_FIELD, position);
                    localBroadcast.sendBroadcast(intent);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            ll.addView(s);
        }
    }

    public String[] INSTRUMENTS = new String[]{"Acoustic Grand Piano", "Bright Acoustic Piano",
            "Electric Grand Piano", "Honky Tonk Piano", "Electric Piano 0", "Electric Piano 1",
            "Harpsichord", "Clavi", "Celesta", "Glockenspiel", "Music Box", "Vibraphone", "Marimba",
            "Xylophone", "Tubular Bells", "Dulcimer", "Drawbar Organ", "Percussive Organ",
            "Rock Organ", "Church Organ", "Reed Organ", "Accordion", "Harmonica", "Tango Accordion",
            "Acoustic Guitar Nylon", "Acoustic Guitar Steel", "Electric Guitar Jazz",
            "Electric Guitar Clean", "Electric Guitar Muted", "Overdriven Guitar",
            "Distortion Guitar", "Guitar Harmonics", "Acoustic Bass", "Electric Bass Finger",
            "Electric Bass Pick", "Fretless Bass", "Slap Bass 0", "Slap Bass 1", "Synth Bass 0",
            "Synth Bass 1", "Violin", "Viola", "Cello", "Contrabass", "Tremolo Strings",
            "Pizzicato Strings", "Orchestral Harp", "Timpani", "String Ensemble 0",
            "String Ensemble 1", "Synthstrings 0", "Synthstrings 1", "Choir Aahs", "Voice Oohs",
            "Synth Voice", "Orchestra Hit", "Trumpet", "Trombone", "Tuba", "Muted Trumpet",
            "French Horn", "Brass Section", "Synthbrass 0", "Synthbrass 1", "Soprano", "Alto Sax",
            "Tenor Sax", "Baritone Sax", "Oboe", "English Horn", "Bassoon", "Clarinet", "Piccolo",
            "Flute", "Recorder", "Pan Flute", "Blown Bottle", "Shakuhachi", "Whistle", "Ocarina",
            "Lead 0 Square", "Lead 1 Sawtooth", "Lead 2 Calliope", "Lead 3 Chiff", "Lead 4 Charang",
            "Lead 5 Voice", "Lead 6 Fifths", "Lead 7 Bass Lead", "Pad 0 New Age", "Pad 1 Warm",
            "Pad 2 Polysynth", "Pad 3 Choir", "Pad 4 Bowed", "Pad 5 Metallic", "Pad 6 Halo",
            "Pad 7 Sweep", "Fx 0 Rain", "Fx 1 Soundtrack", "Fx 2 Crystal", "Fx 3 Atmosphere",
            "Fx 4 Brightness", "Fx 5 Goblins", "Fx 6 Echoes", "Fx 7 Sci Fi", "Sit R", "Banjo",
            "Shamisen", "Koto", "Kalimba", "Bag Pipe", "Fiddle", "Shanai", "Tinkle Bell", "Agogo",
            "Steel Drums", "Woodblock", "Taiko Drum", "Melodic Tom", "Synth Drum", "Reverse Cymbal",
            "Guitar Fret Noise", "Breath Noise", "Seashore", "Bird Tweet", "Telephone Ring",
            "Helicopter", "Applause", "Gunshot"};
}
