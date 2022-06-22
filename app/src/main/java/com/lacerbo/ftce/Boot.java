package com.lacerbo.ftce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Vidal on 15/03/2015.
 ***************************************************************/
public class Boot extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        try {
            Intent intentMonitor = new Intent(context, Localizacao.class);
            intentMonitor.setPackage("com.lacerbo.ftce");
            context.startService(intentMonitor);

            Configuracoes classConfiguracoes = new Configuracoes(context);
            classConfiguracoes.salvarConfiguracoes("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());

            EnviarDados mEnviaDados = new EnviarDados();
            mEnviaDados.getInstance(context, "C", "BOOT", "");
        } catch (Exception e) {
            // Não faça NADA.
        }
    }
}
