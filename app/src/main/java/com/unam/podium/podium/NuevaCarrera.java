package com.unam.podium.podium;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class NuevaCarrera extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private AudioManager audio;
    private MediaPlayer mMediaPlayer;
    private String mDeviceAddress="";
    private String mDeviceName="";
    private TextView temperatura = null;
    private TextView pulso = null;
    private String cadenaTermometro="";
    private String cadenaPulso="";
    private String cadenaBotonDerecho="";
    private String cadenaBotonIzquierdo="";
    private String cadenaSepara="";
    private String cadenaSepara2="";
    private String cadenaBateria="";
    private String cadenaStatusBateria="";
    private int    enteroCarga=0;
    private int    cargador= 0;
    int btnDerPresionado = 0;
    int btnIzqPresionado = 0;

    private RBLService mBluetoothLeService;
    private Map<UUID,BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

    private final ServiceConnection servicio = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("\n\n\n\n\n onSERVICECONNECTED\n\n\n\n\n\n");

            mBluetoothLeService = ((RBLService.LocalBinder) iBinder)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Toast.makeText(NuevaCarrera.this,"No se puede inicializar BLE",Toast.LENGTH_LONG).show();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService =null;
            System.out.println("\n\n\n\n\n onSERVICEDISCONECTED\n\n\n\n\n\n");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // System.out.println("\n\n\n\n\n BROADCASTRECEIVER!!!\n\n\n\n\n\n");
            try {
                final String action = intent.getAction();

                if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
                } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                        .equals(action)) {
                    getGattService(mBluetoothLeService.getSupportedGattService());
                } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                    displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
                }
            }catch(Exception e){e.printStackTrace();}
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(" Podium");
        setContentView(R.layout.activity_nueva_carrera);
        temperatura = (TextView)findViewById(R.id.temperatura);
        pulso = (TextView)findViewById(R.id.pulso);
        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(Dispositivo.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Dispositivo.EXTRA_DEVICE_NAME);

        Intent gattServiceIntent = new Intent(this, RBLService.class);
        bindService(gattServiceIntent, servicio, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT && resultCode== Activity.RESULT_CANCELED){
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onKeyDown(int keycode,KeyEvent event){
        if (keycode == KeyEvent.KEYCODE_BACK){
            //    stopService (Oncreate.gattServiceIntent);
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
        }catch (Exception e){}
    }

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristic = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
        map.put(characteristic.getUuid(), characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        try {
            intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
            intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
            intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
            intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);
        }catch(Exception e){  System.out.println("\n\n\n\n\n\n\n\n ERROR:" + e.getMessage() + "\n\n\n\n\n\n\n\n");}
        finally {
            return intentFilter;
        }

    }


    private void displayData(byte[] byteArray) {
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (byteArray != null) {
            String data="";
            int []datosEnteros= new int[byteArray.length];
            for (int i=0; i<byteArray.length;i++) {
                datosEnteros[i] = byteArray[i];
                data +=datosEnteros[i];
            }

            cadenaBotonIzquierdo = data.substring(12,13);
            cadenaBotonDerecho= data.substring(13,14);
            cadenaTermometro=data.substring(14,16);

            cadenaSepara=data.substring(16);

//            cadenaPulso=data.substring(16);

            if(cadenaSepara.length()==7){
                cadenaPulso=data.substring(16,19);
                cadenaBateria=data.substring(19, 22);
                cadenaStatusBateria=data.substring(22);
            }
            else if(cadenaSepara.length()==6) {
                cadenaSepara2=cadenaSepara.substring(2,5);
                enteroCarga = Integer.parseInt(cadenaSepara2);
                if(enteroCarga == 100){

                    cadenaPulso = data.substring(16,18);
                    cadenaBateria=data.substring(18,21);
                    cadenaStatusBateria = data.substring(21);
                }
                else {
                    cadenaPulso = data.substring(16,19);
                    cadenaBateria=data.substring(19,21);
                    cadenaStatusBateria=data.substring(21);

                }

//                int num = Integer.parseInt(str);
            }
            else{
                cadenaPulso = data.substring(16,18);
                cadenaBateria=data.substring(18,20);
                cadenaStatusBateria=data.substring(20);

            }
            cargador = Integer.parseInt(cadenaStatusBateria);
            if(cargador == 0)
                cadenaStatusBateria="Desconectada";
            else if(cargador == 1)
                cadenaStatusBateria="Cargando";
            else
                cadenaStatusBateria="Leyendo";
/////////////////////////////////////////////////////////// Subir Volumen
            btnDerPresionado = Integer.parseInt(cadenaBotonDerecho);
            if(btnDerPresionado== 1 ) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }

///////////////////////////////////////////////////////////// Bajar Volumen
            btnIzqPresionado = Integer.parseInt(cadenaBotonIzquierdo);
            if(btnIzqPresionado == 1 ) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
            pulso.setText(cadenaPulso);
            temperatura.setText(cadenaTermometro);

        }
    }



}
