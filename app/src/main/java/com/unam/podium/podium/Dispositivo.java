package com.unam.podium.podium;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ikayo on 17/07/15.
 */
public class Dispositivo extends AppCompatActivity implements OnItemClickListener {
    private ArrayList<BluetoothDevice> devices;
    private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
    private SimpleAdapter adapter;
    private Map<String, String> map = null;
    private ListView listView;
    private String DEVICE_NAME = "name";
    private String DEVICE_ADDRESS = "address";
    public static final int RESULT_CODE = 31;
    public final static String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
    public final static String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dis_layout);

        listView = (ListView)findViewById(R.id.listView);
        devices= (ArrayList<BluetoothDevice>) MainActivity.mDevices;
        //devices= (ArrayList<BluetoothDevice>) NuevaCarrera.mDevices;


        for(BluetoothDevice ad:devices){
            map= new HashMap<String,String>();
            map.put(DEVICE_NAME,ad.getName());
            map.put(DEVICE_ADDRESS,ad.getAddress());
            listItems.add(map);
        }
        adapter=new SimpleAdapter(getApplicationContext(),listItems,R.layout.items_layout,
                new String[]{"name","address"},new int[]{R.id.deviceName,R.id.deviceAddr});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HashMap<String,String> hm = (HashMap<String,String>) listItems.get(i);
        String name = "";
        String addr = "";
        name=hm.get(DEVICE_NAME);
        addr=hm.get(DEVICE_ADDRESS);
       // System.out.println("\n\n\n\n\n Agrego esto en dispositivos:"+addr+"\n\n\n\n\n\n");
        SharedPreferences sharedPreferences = getSharedPreferences("MyPodium", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Addr",addr);
        editor.putString("Name",name);
        editor.commit();
/*
        Intent intent = new Intent(this,Prueba.class);
        intent.putExtra(EXTRA_DEVICE_NAME,name);
        intent.putExtra(EXTRA_DEVICE_ADDRESS,addr);
        startActivity(intent);
        finish();
*/
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        listView.removeAllViewsInLayout();
    }
}
