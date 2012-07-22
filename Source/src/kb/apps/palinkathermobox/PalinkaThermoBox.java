package kb.apps.palinkathermobox;

import java.text.DecimalFormat;
import java.util.UUID;

import kb.apps.palinkathermobox.R;
import kb.apps.palinkathermobox.BluetoothService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PalinkaThermoBox extends Activity implements ApplicationEvents {

  // local bluetooth adapter
  private BluetoothAdapter adapter = null;            /** bluetooth adapter object */
  private BluetoothDevice device = null;              /** bluetooth device object (connected to) */
  private BluetoothService service = null;            /** bluetooth service object */
  private boolean adapterPrevEnabled = false;         /** contains the previus status of the bluetooth adapter */
  private boolean waitBeforeConnect = false;          /** application should wait before connect or not */
  private Intent btAdapterTurnOnIntent = null;        /** save adapter turn on request prevent uninteded calling sub activity */
  private TextView logTextRight;                      /** custom title rigth text object */
  private TextView logTextLeft;                       /** custom title left text object */
  private TemperatureControl temperature = null;      /** temperature control object */
  private DecimalFormat formatTemperature;            /** temperature formatting template */         

  private static final int REQUEST_ENABLE_BT = 0;           /** enabling bluetooth adapter command */
  private static final int EXTRA_STATE_DEFAULT_VALUE = -1;  /** getting adapter state command */

  // event messages
  private static final int COMMAND_RECEIVED_MSG = 0;        /** command received sync. message */
  private static final int ERROR_MSG_RECEIVED_MSG = 1;      /** error message received sync. message */
  private static final int BT_SERVICE_STATUS_CHANGED = 2;   /** service status changed sync. message */
  
  // event message variables
  private static final String COMMAND_MSG_STRING = "cmnd";        /** command parameter of command received sync. message */
  private static final String COMMAND_MSG_VALUE_STRING = "value"; /** value parameter of command received sync. message */
  private static final String ERROR_MSG_STRING = "errorMsg";      /** parameter of error message received sync. message */
  private static final String BT_MSG_STATUS_STRING = "status";    /** parameter of service status changed sync. message */
  
  // device commands
  private static final String PC_COMMAND_POSTFIX = "\r\n";  /** command postfix characters */
  private static final String PC_COMMAND_T_IN = "T_IN";     /** inside temperature device command */
  private static final String PC_COMMAND_T_OUT = "T_OUT";   /** inside temperature device command */
  
  // palincar device constants
  private static final UUID PALINCAR_SERVICE = UUID
      .fromString("00001101-0000-1000-8000-00805F9B34FB");
  private static final String PALINCAR_MAC_ADDRESS = "00:1F:E1:F2:95:1F";

  //private static final String PALINCAR_MAC_ADDRESS = "00:07:80:9D:36:46";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set up the window layout
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.main);
    // set custom titlebar
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
    // get local bluetooth adapter
    adapter = BluetoothAdapter.getDefaultAdapter();

    if (adapter == null) {
      Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
      finish();
    }

    registerReceiver(bluetoothState, new IntentFilter(
        BluetoothAdapter.ACTION_STATE_CHANGED));

    logTextRight = (TextView) findViewById(R.id.titleRight);
    logTextRight.setText(R.string.bt_disconnected);
    logTextLeft = (TextView) findViewById(R.id.titleLeft);
    logTextLeft.setText(R.string.app_name);
    temperature = (TemperatureControl) findViewById(R.id.tempControl);
    temperature.setEventHandler(this);
    
    formatTemperature = new DecimalFormat("#.##");
  }

  @Override
  protected void onStart() {
    super.onStart();
    adapterPrevEnabled = adapter.isEnabled();
    if (adapterPrevEnabled) {
      createService();
      waitBeforeConnect = false;
    } else {
      // try to enable the bluetooth adapter
      if (btAdapterTurnOnIntent == null) {
        btAdapterTurnOnIntent = new Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(btAdapterTurnOnIntent, REQUEST_ENABLE_BT);  
        waitBeforeConnect = true;
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (service != null) {
      service.start(waitBeforeConnect);
    }
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
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (service != null) {
      service.stop();
      service = null;
    }
    if (!adapterPrevEnabled && adapter.isEnabled()) {
      // stop the bluetooth device
      Toast.makeText(this, R.string.bt_will_be_disabled, Toast.LENGTH_LONG)
          .show();
      adapter.disable();
      btAdapterTurnOnIntent = null;
    }
    btAdapterTurnOnIntent = null;
    formatTemperature = null;
  }

  /**
   * bluetooth adapter status change receiver
   */
  BroadcastReceiver bluetoothState = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
          EXTRA_STATE_DEFAULT_VALUE);
      switch (state) {
        case BluetoothAdapter.STATE_OFF: 
        {
          if (service != null) {
            service.stop();
          }
          Toast.makeText(context, R.string.bt_connection_interrupted,
              Toast.LENGTH_LONG).show();
          logTextRight.setText(R.string.bt_disconnected);
          waitBeforeConnect = true;
          // finish();
          break;
        }
        case BluetoothAdapter.STATE_TURNING_ON:
        {
          logTextRight.setText(R.string.bt_connecting);
          break;
        }
        case BluetoothAdapter.STATE_ON:
        {
          if (service != null) {
            service.start(waitBeforeConnect);
            waitBeforeConnect = false;
          }
          break;
        }
      }
    }
  };

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
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
      if (service != null) {
        service.write(1);
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      if (service != null) {
        service.write(2);
      }
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onCommand(String command, String value) {
    Message msg = handler.obtainMessage(COMMAND_RECEIVED_MSG);
    Bundle bundle = new Bundle();
    bundle.putString(COMMAND_MSG_STRING, command);
    bundle.putString(COMMAND_MSG_VALUE_STRING, value);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @Override
  public void onErrorMessage(String errorMsg) {
    Message msg = handler.obtainMessage(ERROR_MSG_RECEIVED_MSG);
    Bundle bundle = new Bundle();
    bundle.putString(ERROR_MSG_STRING, errorMsg);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @Override
  public void onStatusChanged(int status) {
    Message msg = handler.obtainMessage(BT_SERVICE_STATUS_CHANGED);
    Bundle bundle = new Bundle();
    bundle.putInt(BT_MSG_STATUS_STRING, status);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @Override
  public void onTemperatureChanged(double value) {
    // TODO Auto-generated method stub
    String cmd = PC_COMMAND_T_OUT + "_" + formatTemperature.format(value) + PC_COMMAND_POSTFIX;
    if (service != null) {
      service.write(cmd.getBytes());      
    }
  }
  
  /**
   * create the service object returns true if the creation was successful
   * otherwise false
   * */
  private void createService() {
    try {
      device = adapter.getRemoteDevice(PALINCAR_MAC_ADDRESS);
      service = new BluetoothService(adapter, device, PALINCAR_SERVICE, this);
    } catch (IllegalArgumentException e) {
      device = null;
      service = null;
      Toast.makeText(this, R.string.bt_device_not_found, Toast.LENGTH_LONG)
          .show();
      finish();
    }
  }

  private Handler handler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case COMMAND_RECEIVED_MSG: {
          // TODO add command handling
          commandProcessor(msg.getData().getString(COMMAND_MSG_STRING), 
              msg.getData().getString(COMMAND_MSG_VALUE_STRING));
          break;
        }
        case ERROR_MSG_RECEIVED_MSG: {
          // TODO add error msg handling
          break;
        }
        case BT_SERVICE_STATUS_CHANGED: {
          int msgId = msg.getData().getInt(BT_MSG_STATUS_STRING);
          logTextRight.setText(msgId);
          break;
        }
      }  
    }
  };
  
  /**
   * processes the incoming commands
   * @param cmd command
   * @param value command value
   * */
  private void commandProcessor(String cmd, String value) {
    // inside temperature
    if (cmd.equals(PC_COMMAND_T_IN)) {
      temperature.setCurrentTemperature(Double.valueOf(value));
    }
  }
}
