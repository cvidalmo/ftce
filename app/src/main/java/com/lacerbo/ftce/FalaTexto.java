package com.lacerbo.ftce;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

class FalaTexto implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    FalaTexto(Context context){
        tts = new TextToSpeech(context.getApplicationContext(), FalaTexto.this);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            tts.setLanguage(new Locale("pt", "br"));
            tts.setSpeechRate(1.4f);
        }
    }

    void LerTexto(String strTexto, boolean... booArgumentos){

        boolean booInterrompeAnterior = booArgumentos.length <= 0 || booArgumentos[0];

        String stLerTexto = strTexto + ". ";
        stLerTexto = stLerTexto.replace("\n", ". ");
        stLerTexto = stLerTexto.replace("\f", ". ");
        stLerTexto = stLerTexto.replace("\r", ". ");
        stLerTexto = stLerTexto.replace("\t", ". ");
        stLerTexto = stLerTexto.replace(". ", ":");
        //stLerTexto = stLerTexto.replace("(", ":");
        stLerTexto = stLerTexto.replace(")", ":");
        //stLerTexto = stLerTexto.replace("\"", ":");
        stLerTexto = stLerTexto.replace(": ", ":");
        stLerTexto = stLerTexto.replace("::", ": :");
        stLerTexto = stLerTexto.replace(", ", ". ");

        stLerTexto = stLerTexto.replace("lida?", "liídaa?:");
        stLerTexto = stLerTexto.replace("ção?", "çã-uo?:");
        stLerTexto = stLerTexto.replace("mada?", "má-da?:");

        stLerTexto = stLerTexto.toLowerCase();
        String[] saTexto = stLerTexto.split(":");

        if ((booInterrompeAnterior) && (tts.isSpeaking())) {
            tts.stop();
        }

        HashMap<String, String> hash = new HashMap<>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));

        String strTextoFala = saTexto[0];
        tts.speak(strTextoFala, TextToSpeech.QUEUE_ADD, hash);
        tts.playSilence(250, TextToSpeech.QUEUE_ADD, null);

        for (int inPos = 1; inPos < saTexto.length; inPos++) {
            strTextoFala = saTexto[inPos];
            tts.speak(strTextoFala, TextToSpeech.QUEUE_ADD, hash);
            tts.playSilence(250, TextToSpeech.QUEUE_ADD, null);
        }

    }

    void destroy(){
        tts.shutdown();
    }
    void stop() {tts.stop(); }
    boolean isSpeaking(){
        return tts.isSpeaking();
    }

}