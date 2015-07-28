package com.unam.podium.podium;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 3000;
    private Dialog mDialog;
    public final static String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
    public final static String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
    public static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private Button bt;
    public static final String DEFAULT = "N/A";

    private ArrayList<BluetoothDevice> devices;
    private Map<String, String> map = null;
    private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
    private String DEVICE_NAME = "name";
    private String DEVICE_ADDRESS = "address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //El dispositivo soporta BLE?
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,getString(R.string.noble),Toast.LENGTH_LONG).show();
            finish();
        }
        final BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=bm.getAdapter();
        //mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        //El dispositivo soporta blueetooth
        if(mBluetoothAdapter==null){
            Toast.makeText(this,getString(R.string.noble),Toast.LENGTH_LONG).show();
        }

        //El dispositivo esta conectado?
        if(!mBluetoothAdapter.isEnabled()){
            Intent disponible = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(disponible,REQUEST_ENABLE_BT);
        }

        bt=(Button)findViewById(R.id.btn);
        //Boton para conectar con el dispositivo vinculado
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = getSharedPreferences("MyPodium", Context.MODE_PRIVATE);
                String addr = sharedPreferences.getString("Addr", DEFAULT);
                String name = sharedPreferences.getString("Name", DEFAULT);

                if (addr.equals(DEFAULT) || addr.equals("address"))
                //|| name.equals(DEFAULT) || name.equals("name") ||
                        Toast.makeText(MainActivity.this, "No has vinculado tu podium", Toast.LENGTH_LONG).show();
                else {
                    Intent intent = new Intent(MainActivity.this, NuevaCarrera.class);
                    intent.putExtra(EXTRA_DEVICE_ADDRESS, addr);
                    intent.putExtra(EXTRA_DEVICE_NAME, name);
                    startActivity(intent);

                }
 /*               Intent intent = new Intent(getApplicationContext(),
                        Prueba.class);  //Una vez terminado el tiempo se pasa a la clase Device
                startActivity(intent);   //Lanza la actividad  deviceListIntent
*/
   /*             Intent intent = new Intent(this,Prueba.class);
                intent.putExtra(EXTRA_DEVICE_NAME,name);
                intent.putExtra(EXTRA_DEVICE_ADDRESS,addr);
                startActivity(intent);

*/
/*
                scanDispositivos();
                dialogo(MainActivity.this, R.layout.dialog_proceso);

                Timer mTimer = new Timer(); // El tiempo es hasta que se termina de ejecutar el getApplicationContext()
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),
                                Dispositivo.class);  //Una vez terminado el tiempo se pasa a la clase Device
                        startActivity(intent);   //Lanza la actividad  deviceListIntent
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                    }
                }, SCAN_PERIOD);

                */
            }
        });

    }


    @Override
        public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.vincular:
                vincularPodium();
                break;
        }
        return true;
    }

    private void vincularPodium(){
//        Toast.makeText(MainActivity.this,"Vincular Dipositivo",Toast.LENGTH_SHORT).show();
        scanDispositivos();
        dialogo(MainActivity.this, R.layout.dialog_proceso);
        Timer mTimer = new Timer(); // El tiempo es hasta que se termina de ejecutar el getApplicationContext()
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),
                        Dispositivo.class);  //Una vez terminado el tiempo se pasa a la clase Device
                startActivity(intent);   //Lanza la actividad  deviceListIntent
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        }, SCAN_PERIOD);


    }

    // Metodo para buscar dispositivos en tiempo SCAN_PERIOD
 private void scanDispositivos() {

     new Thread(new Runnable() {
            @Override
                public void run() {
                    mBluetoothAdapter.startLeScan(llamadaLeScan);
                        try {
                            Thread.sleep(SCAN_PERIOD);
                        } catch (Exception e) {}
                        mBluetoothAdapter.stopLeScan(llamadaLeScan);

            }
        }).start();
    }


    // Atributo que corre un hilo y verifica si el dispositivo BLE esta en la lista de BluetoothDevice si no lo agrega
    private BluetoothAdapter.LeScanCallback llamadaLeScan= new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bluetoothDevice!=null){
                        if(mDevices.indexOf(bluetoothDevice)==-1){
                            mDevices.add(bluetoothDevice);
                        }

                    }
                }
            });
        }
    };

    public void dialogo(Context con,int i){
        DialogInterface.OnKeyListener diokl = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if(i==keyEvent.KEYCODE_HOME || i==keyEvent.KEYCODE_SEARCH){
                    return true;
                }
                return false;
            }
        };
        mDialog = new AlertDialog.Builder(con).create();
        mDialog.setOnKeyListener(diokl);
        mDialog.show();
        mDialog.setContentView(i);
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
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
        mDialog = null;
    }



}
