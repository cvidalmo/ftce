package com.lacerbo.ftce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Vidal on 15/03/2015.
 ***************************************************************/
public class Volume extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int prevVolume;
        int volume;
        if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {
            //volume = (int)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
            //prevVolume = (int)intent.getExtras().get("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE");

            Configuracoes classConfiguracoes = new Configuracoes(context);
            classConfiguracoes.salvarConfiguracoes("PI_booVolume", true);

            /*if(volume < prevVolume) {
              //Botão que diminuiu
            } else if(volume > prevVolume){
              //Botão que aumentou
            }*/

        }
    }
}
