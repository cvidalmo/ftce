package com.lacerbo.ftce;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

class Telefone {

    private TelephonyManager telephonyManager;
    private Integer nivelBateria = 0;
    private Boolean isPlugado = false;
    private String strData = "";
    private String strHora = "";
    private String imeiSIM1 = "";
    private String operatorSIM1 = "";
    private String modeloFab = "";

    Integer getNivelBateria() {
        return nivelBateria;
    }

    String getData() {
        return strData;
    }

    String getHora() {
        return strHora;
    }

    Boolean getIsPlugado() {
        return isPlugado;
    }

    String getImeiSIM1() {
        return imeiSIM1;
    }

    String getOperatorSIM1() {
        return operatorSIM1;
    }

    String getModeloFab() {
        return modeloFab;
    }

    void getInstance(Context context){

        IntentFilter ifBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifBateria);
        nivelBateria = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;

        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : 0;
        isPlugado = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        strData = currentDateAndTime.substring(0,6);
        strHora = currentDateAndTime.substring(7,13);

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version =  Build.VERSION.SDK; //System.getProperty("os.Build");

        if (model.startsWith(manufacturer)) {
            modeloFab = "M:" +model + ", A:" + version;
        } else {
            modeloFab = "F:" + manufacturer + ", M:" + model + ", A:" + version;
        }

        telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

        imeiSIM1 = telephonyManager.getDeviceId();

        try {
            imeiSIM1 = getDeviceIdBySlot("getDeviceIdGemini", 0);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                imeiSIM1 = getDeviceIdBySlot("getDeviceId", 0);
            } catch (GeminiMethodNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        imeiSIM1 = "000000000000000" + imeiSIM1.trim();
        imeiSIM1 = imeiSIM1.substring(imeiSIM1.length()-15);

        operatorSIM1 = telephonyManager.getSimOperatorName();

        try {
            operatorSIM1 = getNumberPhoneBySlot("getSimOperatorNameGemini", 0);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                operatorSIM1 = getNumberPhoneBySlot("getSimOperatorName", 0);
            } catch (GeminiMethodNotFoundException e1) {
                e1.printStackTrace();
            }
        }


        if (operatorSIM1 != null) {
            operatorSIM1 = operatorSIM1.toUpperCase();
        }

    }

    private String getDeviceIdBySlot(String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        String imei = null;

        try{

            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephonyManager, obParameter);

            if(ob_phone != null){
                imei = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imei;
    }

    private String getNumberPhoneBySlot(String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        String phoneNumber = null;

        try{

            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimNumber = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimNumber.invoke(telephonyManager, obParameter);

            if(ob_phone != null){
                phoneNumber = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return phoneNumber;
    }

    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

}
