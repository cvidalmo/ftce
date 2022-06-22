package com.lacerbo.ftce;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.io.File.*;

public class Inicio extends Activity {

    private Boolean booFalhaInstalacao = false;

    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 12345;
    private static final String TAG = Inicio.class.getSimpleName();
    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private Boolean booProgressBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        if (! isClassAtiva()) {
            Configuracoes classConfiguracoes = new Configuracoes(getApplicationContext());
            classConfiguracoes.salvarConfiguracoes("PI_lonDataHoraMonitor", 0L);
            classConfiguracoes.salvarConfiguracoes("PI_strVersaoApp", "104");
        }

        if (Build.VERSION.SDK_INT >= 23) {
            List<String> lisStrPermissao = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.RECORD_AUDIO);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (lisStrPermissao.isEmpty()) {
                chamaSite();
            } else {
                String[] strPermissao = new String[lisStrPermissao.size()];
                strPermissao = lisStrPermissao.toArray(strPermissao);
                ActivityCompat.requestPermissions(this, strPermissao, 0);
            }

        } else {
            chamaSite();
        }

    }

    private boolean isClassAtiva() {
        String strClass = "com.lacerbo.ftce.Localizacao";
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (strClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void chamaSite() {

        EnviarDados mEnviaDados = new EnviarDados();
        mEnviaDados.getInstance(getApplicationContext(), "C", "ACESSO", "");

        Configuracoes classConfiguracoes = new Configuracoes(getApplicationContext());

        if (classConfiguracoes.pegarBooleanConfiguracoes("PI_booVibrar")) {
            Vibrator mVibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] mPattern = {0, 75, 300, 75, 300, 75, 1000, 75, 300, 75, 300, 75, 1000, 75, 300, 75, 300, 75, 300};  //O zero é para começar imediatamente, os pares sao: vibrar e pausa.
            mVibrar.vibrate(mPattern, -1);
        }

        if (classConfiguracoes.pegarBooleanConfiguracoes("PI_booSomTiros")) {
            MediaPlayer mpSomTiros;
            //mpSomTiros = new MediaPlayer;
            mpSomTiros = MediaPlayer.create(getApplicationContext(), R.raw.silenciador);
            mpSomTiros.setVolume(1.0f, 1.0f);
            mpSomTiros.start();
        }

        Telefone classTelefone = new Telefone();
        classTelefone.getInstance(getApplicationContext());

        String strServidor = "https://" + classConfiguracoes.pegarStringConfiguracoes("PI_strURLServidor") + "/dados.php?";
        String strImeiSIM1 = classTelefone.getImeiSIM1();
        String strValor1 = strImeiSIM1.substring(0, 8) + classTelefone.getHora();
        String strValor2 = "3" + strImeiSIM1.substring(8) + classTelefone.getHora();
        strImeiSIM1 = "dados=" + geraChaveAceSite(strValor2) + "-" + geraChaveAceSite(strValor1);

        /*
        webView = (WebView) findViewById(R.id.webView1);

        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);
        // Other webview options
        webView.getSettings().setLoadWithOverviewMode(true);

        //Other webview settings
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //webView.setScrollbarFadingEnabled(false);
        //webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setSupportZoom(false);
        webView.loadUrl(strServidor + strImeiSIM1);
        startWebView();
        */

        webView = findViewById(R.id.webView1);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webView.setWebViewClient(new Client());
        webView.setWebChromeClient(new ChromeClient());
//        if (Build.VERSION.SDK_INT >= 19) {
//            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        } else {
//            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }

        webView.loadUrl(strServidor + strImeiSIM1);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public class ChromeClient extends WebChromeClient {

        // For Android 5.0 +
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    //Log.e(TAG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Seleção de Imagem");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;

        }

        /*
        DICA: Esse método só é chamado nas versões do Android superior a 3.0 e inferior a 5.0 (menor que LolliPop.
              Mas na compilação, no build.grade (Module: app), em buildTypes, em release{ proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro' }
              diretório: E:\Projetos\Celular - Mobile\ftce\build\intermediates\proguard-files
              Deve-se acrescentar o código abaixo:

              -keepclassmembers class * extends android.webkit.WebChromeClient {
                   public void openFileChooser(...);
              }

              SE NA HORA DE GERAR O APK, EM Build, FOR EXECUTADO O Clean Project, DEVE-SE COLOCAR NOVAMENTE O CÓDIGO NO ARQUIVO,
              POIS ESSA OPÇÃO APAGA O ARQUIVO: proguard-android-optimize.txt

        */ // openFileChooser for Android 3.0+ and 5.0-
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType)  {

            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");

            // Create the storage directory if it does not exist
            if (! imageStorageDir.exists()){
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + separator + "FTCE_" + System.currentTimeMillis() + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final List<Intent> cameraIntents = new ArrayList<>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for(ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent i = new Intent(captureIntent);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                i.setPackage(packageName);
                i.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                cameraIntents.add(i);

            }

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            Intent chooserIntent = Intent.createChooser(i,"Seleção de Imagem");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

        }

        void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {
            openFileChooser(uploadMsg);
        }

        /** Added code to clarify chooser. **/

        //The webPage has 2 filechoosers and will send a console message informing what action to perform, taking a photo or updating the file
        public boolean onConsoleMessage(ConsoleMessage cm) {
            onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
            return true;
        }
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            //Log.d("androidruntime", "Per cònsola: " + message);
            //Toast.makeText(getBaseContext(), message+":message", Toast.LENGTH_LONG).show();
            //if(message.endsWith("foto")){ boolFileChooser= true; }
            //else if(message.endsWith("pujada")){ boolFileChooser= false; }
        }
        /* Added code to clarify chooser. */
    }

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if (webView != null) {
            if(webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }

    /*
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)

        return super.onKeyDown(keyCode, event);
    }*/


    public class Client extends WebViewClient {
        ProgressDialog progressDialog;

        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // If url contains mailto link then open Mail Intent
            if (url.contains("mailto:")) {

                // Could be cleverer and use a regex
                //Open links in new browser
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                // Here we can open new activity

                return true;

            } else {

                // Stay within this webview and load url
                view.loadUrl(url);
                return true;
            }
        }

        //Show loader on url load
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // Then show progress  Dialog
            // in standard case YourActivity.this
            if (progressDialog == null && booProgressBar) {
                booProgressBar = false;
                progressDialog = new ProgressDialog(Inicio.this);
                progressDialog.setMessage("Carregando...");
                progressDialog.show();
            }
        }

        // Called when all page resources loaded
        public void onPageFinished(WebView view, String url) {
            try {
                // Close progressDialog
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } else {  //if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri result = null;

            try {
                if (resultCode == RESULT_OK) {
                    // retrieve from the private variable if the intent is null
                    result = data == null ? mCapturedImageURI : data.getData();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "activity :" + e,
                        Toast.LENGTH_LONG).show();
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }

    }

    private String geraChaveAceSite(String strChave) {
        int resto;
        long cociente = Long.parseLong(strChave);
        StringBuilder retorno = new StringBuilder();
        String sequencia = "AaBb0CcDdE1eFfGg2HhIiJ3jKkLl4MmNn5OoPpQ6qRrSs7TtUuV8vWwXx9YyZz";
        while (cociente > 62) {
            resto = (int) (cociente % 62);
            retorno.append(sequencia.substring(resto, resto + 1));
            cociente = (cociente / 62);
        }
        resto = (int)cociente;
        retorno.append(sequencia.substring(resto, resto + 1));

        return retorno.toString();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean booChamaSite = true;
        try {
            if (requestCode == 0) {
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (i == -1) {
                            booChamaSite = false;
                            break;
                        }
                    }
                } else {
                    booChamaSite = false;
                }
            } else {
                booChamaSite = false;
            }
        } catch (Exception e) {
            booChamaSite = false;
        }

        if (booChamaSite) {
            chamaSite();
        } else {
            booFalhaInstalacao = true;
            Toast mensag = Toast.makeText(this, "Falha nas permissões. Reinstale permitindo todos os acessos.", Toast.LENGTH_LONG);
            mensag.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (booFalhaInstalacao) {
            Uri packageUri = Uri.parse("package:com.lacerbo.ftce");
            Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            startActivity(uninstallIntent);
        }
    }

}

