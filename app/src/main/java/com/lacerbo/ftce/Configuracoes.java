package com.lacerbo.ftce;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

class Configuracoes {

    private SharedPreferences preferences;

    Configuracoes(Context context) {
        this.preferences = context.getSharedPreferences("com.lacerbo.ftce", Context.MODE_PRIVATE);
    }

    void salvarConfiguracoes(String chave, Object valor){

        if (valor.getClass().getSimpleName().toLowerCase().equals("boolean")) {
            this.preferences.edit().putBoolean(chave, (Boolean) valor).commit();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("integer")) {
            this.preferences.edit().putInt(chave, (Integer) valor).commit();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("float")) {
            this.preferences.edit().putFloat(chave, (Float) valor).commit();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("long")) {
            this.preferences.edit().putLong(chave, (Long) valor).commit();
        } else {
            this.preferences.edit().putString(chave, (String) valor).commit();
        }

    }

    String pegarStringConfiguracoes(String chave){

        String retornoPadrao = "";

        switch (chave) {
            case "PI_strURLServidor":
                retornoPadrao = "ftce.lacerbo.com";  //URL do servidor.
                break;
            //case "PI_strIPServidor":
            //    retornoPadrao = "167.114.109.13";  //IP do servidor.
            //    break;
            case "PI_strVersaoApp":
                retornoPadrao = "106";  //Versão do aplicativo.
                break;
        }

        return this.preferences.getString(chave, retornoPadrao);
    }

    Integer pegarIntegerConfiguracoes(String chave){
        Integer retornoPadrao = 0;

        switch (chave) {
            case "PI_intPortaConexao":
                retornoPadrao = 11101;  //Porta de conexão do servidor.
                break;
            case "PI_intTempoTransmissao":
                retornoPadrao = 60;  //Tempo de transmissão da localização em segundos que o serviço Localizacao se conecta com o servidor.
                break;
            case "PI_intAtivarMonitor":
                retornoPadrao = 300;  //Quantidade de segundos que é considera como inativo o serviço Localizacao se não atualizar a PI_lonDataHoraMonitor.
                break;
        }

        return this.preferences.getInt(chave, retornoPadrao);

    }

    Boolean pegarBooleanConfiguracoes(String chave){
        Boolean retornoPadrao = false;

        switch (chave) {
            case "PI_booSomTiros":
                retornoPadrao = true;  //Usado para ativar/desativar o som da sirene nas mensagens ou aceso ao aplicatvo.
                break;
            case "PI_booSomSirene":
                retornoPadrao = true;  //Usado para ativar/desativar o som da sirene nas mensagens de pedido de Ajuda.
                break;
            case "PI_booVibrar":
                retornoPadrao = true;  //Usado para ativar/desativar a vobração nas mensagens.
                break;
            case "PI_booFalar":
                retornoPadrao = false;  //Usado para ativar/desativar a fala das mensagens. É ignorado nas mensagens de AJUDA ou PROPAGANDA.
                break;
            case "PI_booVolume":
                retornoPadrao = false;  //Usado para desativar reprodução Sirene.
                break;
        }

        return this.preferences.getBoolean(chave, retornoPadrao);
    }

    Float pegarFloatConfiguracoes(String chave){
        Float retornoPadrao = 0.0f;
        switch (chave) {
            case "PI_floDistancia":
                retornoPadrao = 20.0f;  //Menor distância em metros entre localizações.
                break;
        }
        return this.preferences.getFloat(chave, retornoPadrao);
    }

    Long pegarLongConfiguracoes(String chave){
        Long retornoPadrao = 0L;
        if (chave.equals("PI_lonDataHoraMonitor")) {
            retornoPadrao = 0L;  //Valor em milisegundos da última data e hora do serviço Monitor.
        }
        return this.preferences.getLong(chave, retornoPadrao);
    }

    Boolean ativaLocalizacaoConfiguracoes() {
        Boolean retorno = false;
        Long lonDataHoraMonitor = pegarLongConfiguracoes("PI_lonDataHoraMonitor");

        //Se o serviço Monitor está ativado a menos de 300 segundos (PI_intAtivarMonitor).
        if ( ( (Calendar.getInstance().getTimeInMillis()/1000) - (lonDataHoraMonitor/1000) > pegarIntegerConfiguracoes("PI_intAtivarMonitor"))) {
            retorno = true;
        }

        return retorno;
    }

}
