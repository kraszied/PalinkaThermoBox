package kb.apps.palinkathermobox;

import kb.apps.palinkathermobox.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PalinkaThermoBox extends Activity {

  // local bluetooth adapter
  private BluetoothAdapter m_BluetoothAdapter = null;
  private static final int REQUEST_ENABLE_BT = 0;
  private static final int EXTRA_STATE_DEFAULT_VALUE = -1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // get local bluetooth adapter
    m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    if (m_BluetoothAdapter.isEnabled()) {
      String btInfo = m_BluetoothAdapter.getAddress() + " : " + m_BluetoothAdapter.getName();
      Toast.makeText(this, btInfo, Toast.LENGTH_LONG).show();
    } else {
      // try to enable the bluetooth adapter
      startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
      registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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
        finish();        
      }
    }
  };
  
  @Override
  protected void onDestroy()
  {
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch(requestCode)
    {
      case REQUEST_ENABLE_BT:
      {
        if (resultCode != RESULT_OK) {
          Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_LONG).show();
          setResult(RESULT_CANCELED);
          finish();      
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
  
}
