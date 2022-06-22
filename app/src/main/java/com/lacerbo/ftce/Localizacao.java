package com.lacerbo.ftce;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.view.Display;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vidal on 22/10/2015.
 ***************************************/
public class Localizacao extends Service {

    private Location mLocationAnterior = null;
    private Location mLocationThread = null;
    private MyGpsListener myGpsListener;
    private LocationManager mLocationManager;
    //private float MIN_DISTANCE = 20;
    //private long MIN_TIME = 60 * 1000;  //Segundos.

    private Configuracoes classConfiguracoes;
    private Boolean booGPSProvider = false;
    private Boolean booNetworkProvider = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 21) { // Build.VERSION_CODES.LOLLIPOP) {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "gufo");
            wakeLock.acquire();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        myGpsListener = new MyGpsListener();
        Boolean booPermissaoGPS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        classConfiguracoes = new Configuracoes(getApplicationContext());
        if (booPermissaoGPS) ativaMonitor();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ativaMonitor() {

        final Handler mHandler = new Handler();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                EnviarDados enviarDados;
                enviarDados = new EnviarDados();

                float MIN_DISTANCE = classConfiguracoes.pegarFloatConfiguracoes("PI_floDistancia");
                long MIN_TIME = classConfiguracoes.pegarIntegerConfiguracoes("PI_intTempoTransmissao");
                Long lonDataHoraMonitor = classConfiguracoes.pegarLongConfiguracoes("PI_lonDataHoraMonitor");

                if ((Calendar.getInstance().getTimeInMillis()/1000) - (lonDataHoraMonitor/1000) < MIN_TIME) {
                    try {
                        enviarDados.getInstance(getApplicationContext(), "C", "Comando", "PING");  //PING.
                    } catch (Exception ignored) {}
                } else {

                    classConfiguracoes.salvarConfiguracoes("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());

                    try {
                        booGPSProvider = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ignored) {
                    }
                    /*try {
                        booNetworkProvider = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception ignored) {
                    }*/

                    Location location = null;

                    if (booGPSProvider) {
                        try {
                            myGpsListener.onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myGpsListener);
                            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        } catch (Exception ignored) {}
                    /*} else if (booNetworkProvider) {
                        myGpsListener.onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myGpsListener);
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);*/
                    } else {
                        try {
                            enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GD");  //GPS DESATIVADO.
                        } catch (Exception ignored) {}
                    }

                    if (location != null) {
                        if ((mLocationAnterior == null) || (location.distanceTo(mLocationAnterior) > MIN_DISTANCE)) {
                            String strLatitude, strLongitude, strPrecisao, strVelocidade, strRota;
                            Float floLatitude = 0.0f;
                            Float floLongitude = 0.0f;
                            Float floVelocidade = 0.0f;
                            Float floPrecisao = 0.0f;
                            Float floRota = 0.0f;

                            try {
                                floLatitude = (float) location.getLatitude();
                                floLongitude = (float) location.getLongitude();
                                floVelocidade = location.getSpeed();
                                floPrecisao = location.getAccuracy();
                                floRota = location.getBearing();

                            } catch (Exception e) {
                                try {
                                    enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GED");  //GPS ERRO AO PEGAR DADOS.
                                    mLocationManager.removeUpdates(myGpsListener);
                                } catch (Exception ignored) {}
                            }

                            if (floLatitude < 0.0f) {
                                strLatitude = String.valueOf(((int) (floLatitude * -1000000)) + 1000000000);
                            } else {
                                strLatitude = "00" + String.valueOf(((int) (floLatitude * 1000000)) + 1000000000);
                                strLatitude = "0" + strLatitude.substring(strLatitude.length() - 9);
                            }

                            if (floLongitude < 0.0f) {
                                strLongitude = String.valueOf(((int) (floLongitude * -1000000)) + 1000000000);
                            } else {
                                strLongitude = "00" + String.valueOf(((int) (floLongitude * 1000000)) + 1000000000);
                                strLongitude = "0" + strLongitude.substring(strLongitude.length() - 9);
                            }

                            strPrecisao = "00000" + String.valueOf((int) (floPrecisao * 1));
                            strPrecisao = strPrecisao.substring(strPrecisao.length() - 4);

                            strVelocidade = "0000" + String.valueOf((int) (floVelocidade * 1));
                            strVelocidade = strVelocidade.substring(strVelocidade.length() - 3);

                            strRota = "0000" + String.valueOf((int) (floRota * 1));
                            strRota = strRota.substring(strRota.length() - 3);

                            String strEndereco = ".";

                            if (!floLatitude.equals(0.0f)) {
                                String result = null;

                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addressList != null && addressList.size() > 0) {
                                        Address address = addressList.get(0);
                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                            sb.append(address.getAddressLine(i)).append(", ");
                                        }
                                        //sb.append(address.getLocality()).append("\n");
                                        //sb.append(address.getPostalCode()).append("\n");
                                        sb.append(address.getCountryName()).append(".");
                                        result = sb.toString();
                                    }
                                } catch (Exception e) {
                                    try {
                                        enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GEG");  //GPS ERRO AO PEGAR GEOINFORMACOES.
                                        mLocationManager.removeUpdates(myGpsListener);
                                    } catch (Exception ignored) {}
                                }

                                if (result != null) {
                                    strEndereco = result;
                                }
                            }
                            if (mLocationAnterior == null) {
                                mLocationAnterior = location;
                            }
                            mLocationAnterior.set(location);
                            String strDados = strLatitude + strLongitude + strPrecisao + strVelocidade + strRota + "," + strEndereco;
                            try {
                                enviarDados.getInstance(getApplicationContext(), "T", "Andando", strDados);
                            } catch (Exception ignored) {}
                        } else {
                            try {
                                if (isScreenOn()) {
                                    enviarDados.getInstance(getApplicationContext(), "C", "Comando", "PDA");  //PING DISPLAY ATIVO.
                                } else {
                                    enviarDados.getInstance(getApplicationContext(), "C", "Comando", "PDM");  //PING GPS DISTANCIA MINIMA.
                                }
                            } catch (Exception ignored) {}
                        }
                        try {
                            mLocationManager.removeUpdates(myGpsListener);
                        } catch (Exception ignored) {}
                    }
                }
                //Reinicia a Thread.
                mHandler.postDelayed(this, 20 * 1000);
            }
        };

        mHandler.postDelayed(mRunnable, 20 * 1000);
    }

    private class MyGpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private Boolean isScreenOn () {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

}

