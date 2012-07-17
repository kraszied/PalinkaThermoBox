package kb.apps.palinkathermobox;

import java.util.Set;
import java.util.UUID;

import kb.apps.palinkathermobox.R;
import kb.apps.palinkathermobox.BluetoothService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PalinkaThermoBox extends Activity implements ApplicationEvents {

  // local bluetooth adapter
  private BluetoothAdapter adapter = null;
  private BluetoothDevice device = null;
  private BluetoothService service = null;
  private boolean adapterPrevEnabled = false;
  // TODO remove debug objects
  private EditText logText;
  private EditText command;
  private Button commandButton;
  
  private static final int REQUEST_ENABLE_BT = 0;
  private static final int EXTRA_STATE_DEFAULT_VALUE = -1;
  
  // event messages
  private static final int COMMAND_RECEIVED_MSG = 0;
  private static final int ERROR_MSG_RECEIVED_MSG = 1;
  // event message variables
  private static final String COMMAND_MSG_STRING = "cmnd";
  private static final String COMMAND_MSG_VALUE_STRING = "value";
  private static final String ERROR_MSG_STRING = "errorMsg";
  
  private static final UUID PALINCAR_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // get local bluetooth adapter
    adapter = BluetoothAdapter.getDefaultAdapter();
    
    if (adapter == null) {
      Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
      finish();
    }
    
    // TODO remove debug
    logText = (EditText) findViewById(R.id.log);
    command = (EditText) findViewById(R.id.command);    
    commandButton = (Button) findViewById(R.id.sendCommand);
    
    commandButton.setOnClickListener(new View.OnClickListener() {
      
      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == commandButton) {          
          service.write(command.getText().toString().getBytes());
        }
      }
    });
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    adapterPrevEnabled = adapter.isEnabled(); 
    if (adapterPrevEnabled) {
      String btInfo = adapter.getAddress() + " : " + adapter.getName();
      logText.append(btInfo + "\n"); // TODO remove debug logging
      createService();
    } else {
      // try to enable the bluetooth adapter
      startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
      registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    //Toast.makeText(this, "begin onResume()", Toast.LENGTH_LONG).show();
    if (service != null && !service.isRunning()) {
      logText.append("service not null\n"); // TODO remove debug logging
      //service.stop();
      service.start();
      if (service.isRunning()) {
        logText.append("service running\n"); // TODO remove debug logging
      }
    }
    //Toast.makeText(this, "end onResume()", Toast.LENGTH_LONG).show();
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    if (service != null) {
      service.stop();
    }
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    if (service != null) {
      service.stop();
    }
    if (!adapterPrevEnabled) {
      // stop the bluetooth device
      Toast.makeText(this, R.string.bt_will_be_disabled, Toast.LENGTH_LONG).show();
      adapter.disable();
    }
  }
  
  // bluetooth adapter status change receiver
  BroadcastReceiver bluetoothState = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, EXTRA_STATE_DEFAULT_VALUE);
      if (state == BluetoothAdapter.STATE_OFF) {
        Toast.makeText(context, R.string.bt_not_enabled, Toast.LENGTH_LONG).show();
        // TODO: connection status should be changed here
        //finish();        
      }
    }
  };
  
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch(requestCode) {
      case REQUEST_ENABLE_BT: {
        if (resultCode != RESULT_OK) {
          Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_LONG).show();
          setResult(RESULT_CANCELED);
          finish();      
        } else {
          createService();
        }
        break;
      }
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      //String sVolumeUp = "Up\r\n";
      if (service != null) {
        //service.write(sVolumeUp.getBytes());
        service.write(1);
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
      //String sVolumeDown = "Down\r\n";
      if (service != null) {
        //service.write(sVolumeDown.getBytes());
        service.write(2);
      }
      return true;
    }    
    return super.onKeyDown(keyCode, event);
  }
    
  private void createService() {    
 // TODO remove debug device
    logText.append("service null\n");
    if (service != null) {
      service.stop();
      service = null;
    }
    
    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
    if (pairedDevices.size() > 0) {
      for (BluetoothDevice device : pairedDevices) {
        logText.append(device.getName() + "\n");
          //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
      }
    }    
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    
    device = adapter.getRemoteDevice("00:1F:E1:F2:95:1F");
    
    service = new BluetoothService(adapter, device, PALINCAR_SERVICE, this);  // TODO keep only this line
  }

  private Handler handler = new Handler() {
    public void  handleMessage(Message msg) {
      switch(msg.what) {
        case COMMAND_RECEIVED_MSG: {
          logText.append("cmd: " + msg.getData().getString(COMMAND_MSG_STRING) + ", value: " + msg.getData().getString(COMMAND_MSG_VALUE_STRING) + "\n");
          break;
        }
        case ERROR_MSG_RECEIVED_MSG: {
          logText.append(msg.getData().getString(ERROR_MSG_STRING) + "\n");
          break;
        }
      }
    }
  };

  @Override
  public void onCommand(String command, String value) {
    // TODO Auto-generated method stub
    Message msg = handler.obtainMessage(COMMAND_RECEIVED_MSG);
    Bundle bundle = new Bundle();
    bundle.putString(COMMAND_MSG_STRING, command);
    bundle.putString(COMMAND_MSG_VALUE_STRING, value);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @Override
  public void onErrorMessage(String errorMsg) {
    // TODO Auto-generated method stub
    Message msg = handler.obtainMessage(ERROR_MSG_RECEIVED_MSG);
    Bundle bundle = new Bundle();
    bundle.putString(ERROR_MSG_STRING, errorMsg);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }
  
}
