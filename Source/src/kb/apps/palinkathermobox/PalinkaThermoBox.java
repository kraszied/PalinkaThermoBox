package kb.apps.palinkathermobox;

import java.util.UUID;

import kb.apps.palinkathermobox.R;
import kb.apps.palinkathermobox.BluetoothService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PalinkaThermoBox extends Activity implements Events {

  // local bluetooth adapter
  private BluetoothAdapter adapter = null;
  private BluetoothDevice device = null;
  private BluetoothService service = null;
  private boolean adapterPrevEnabled = false;
  private static final int REQUEST_ENABLE_BT = 0;
  private static final int EXTRA_STATE_DEFAULT_VALUE = -1;
  
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
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    adapterPrevEnabled = adapter.isEnabled(); 
    if (adapterPrevEnabled) {
      String btInfo = adapter.getAddress() + " : " + adapter.getName();
      Toast.makeText(this, btInfo, Toast.LENGTH_LONG).show();
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
      Toast.makeText(this, "service not null", Toast.LENGTH_LONG).show();
      //service.stop();
      service.start();
      if (service.isRunning()) {
        Toast.makeText(this, "service running", Toast.LENGTH_LONG).show();
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
    Toast.makeText(this, "service null", Toast.LENGTH_LONG).show();
    if (service != null) {
      service.stop();
      service = null;
    }
    device = adapter.getRemoteDevice("00:1F:E1:F2:95:1F");
    service = new BluetoothService(adapter, device, PALINCAR_SERVICE, this);
  }

  @Override
  public void onDataReceived(byte[] data) {
    // TODO Auto-generated method stub
    handler.sendEmptyMessage(0);
  }

  @Override
  public void onErrorMessage(String errorMsg) {
    Message msg = handler.obtainMessage(12);
    Bundle bundle = new Bundle();
    bundle.putString("ErrorMessage", errorMsg);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }
  
  @Override
  public void onUserMessage(String userMsg) {
    // TODO Auto-generated method stub
    
  }

  private Handler handler = new Handler() {
    public void  handleMessage(Message msg) {
      switch(msg.what) {
        case 0: {
          Toast.makeText(getApplicationContext(), "someting arrived", Toast.LENGTH_LONG).show();
          break;
        }
        case 12: {
          Toast.makeText(getApplicationContext(), msg.getData().getString("ErrorMessage"), Toast.LENGTH_LONG).show();
          break;
        }
      }
    }
  };
  
}
