package com.davtyan.mytaxi.view.activitys;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
 
import android.speech.tts.TextToSpeech;

import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
 
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
 
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
 
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.davtyan.mytaxi.R;

public class MainActivity extends Activity {
    private TextToSpeech textToSpeech;
    private EditText editText_text;
    private Button button;
    private RadioButton radio_en;
    private RadioButton radio_fr;
    RadioGroup radioGroup;

    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        editText_text = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        radio_en = (RadioButton) findViewById(R.id.radio_en);
        radio_fr = (RadioButton) findViewById(R.id.radio_fr);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                Log.e("TTS", "TextToSpeech.OnInitListener.onInit...");
                printOutSupportedLanguages();
                setTextToSpeechLanguage();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTextToSpeechLanguage();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void printOutSupportedLanguages() {
        // Supported Languages
        Set<Locale> supportedLanguages = textToSpeech.getAvailableLanguages();
        if (supportedLanguages != null) {
            for (Locale lang : supportedLanguages) {
                Log.e("TTS", "Supported Language: " + lang);
            }
        }
    }


    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut() {
        if (!ready) {
            Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        // Text to Speak
        String toSpeak = editText_text.getText().toString();
        Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show();
        // A random String (Unique ID).
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private Locale getUserSelectedLanguage() {
        int checkedRadioId = this.radioGroup.getCheckedRadioButtonId();
        if (checkedRadioId == R.id.radio_en) {
            return Locale.ENGLISH;
        } else if (checkedRadioId == R.id.radio_fr) {
            return Locale.FRANCE;
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTextToSpeechLanguage() {
        Locale language = this.getUserSelectedLanguage();
        if (language == null) {
            this.ready = false;
            Toast.makeText(this, "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.ready = true;
            Locale currentLanguage = textToSpeech.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }
}
 