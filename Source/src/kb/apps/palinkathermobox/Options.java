package kb.apps.palinkathermobox;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * options activity object
 * implements the option setting window
 * */
public class Options extends Activity implements OnSeekBarChangeListener {
  
  public final static String ACTION_VALUE_CHANGED = "kb.apps.palinkathermobox.Options.actions.VALUE_CHANGED";
  public final static String EXTRA_SOURCE = "kb.apps.palinkathermobox.Options.EXTRA_SOURCE";
  public final static String EXTRA_VALUE = "kb.apps.palinkathermobox.Options.EXTRA_VALUE";
  public final static int SRC_BRIGHTNESS = R.id.sbBrightness;
  public final static int SRC_PELTIER_POWER = R.id.sbPeltierPower;
  public final static int SRC_UNKNOWN = -1;
  public final static int BRIGHTNESS_CHANGED = 10;
  public final static int PELTIER_POWER_CAHNGED = 11;
  
  private TextView brightnessValue;
  private SeekBar brightness;
  private TextView peltierPowerValue;
  private SeekBar peltierPower;
  
  private final static String PREFERENCE_NAME = TemperatureControl.PREFERENCE_NAME;
  private final static String BRIGHTNESS_KEY = "BrightnessValue";
  private final static String PELTIER_POWER_KEY = "PeltierPowerValue";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_options);
    
    brightnessValue = (TextView) findViewById(R.id.tvBrightnessValue);
    brightnessValue.setText("");
    peltierPowerValue = (TextView) findViewById(R.id.tvPeltierPowerValue);
    peltierPowerValue.setText("");
    
    brightness = (SeekBar) findViewById(R.id.sbBrightness);
    peltierPower = (SeekBar) findViewById(R.id.sbPeltierPower);
        
    loadPreferences(this);

    brightness.setOnSeekBarChangeListener(this);
    peltierPower.setOnSeekBarChangeListener(this);
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    savePrefrences(this);
  }
  
  @Override
  public void onStartTrackingTouch(SeekBar arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {    
    Intent intent = new Intent();
    intent.setAction(ACTION_VALUE_CHANGED);
    intent.putExtra(EXTRA_SOURCE, seekBar.getId());
    intent.putExtra(EXTRA_VALUE, seekBar.getProgress());
    sendBroadcast(intent);
  }
  
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress,
      boolean fromUser)
  {
    // TODO Auto-generated method stub
  }
  
  private void loadPreferences(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    brightness.setProgress(Integer.valueOf(sharedPreferences.getInt(BRIGHTNESS_KEY, 50)));
    peltierPower.setProgress(Integer.valueOf(sharedPreferences.getInt(PELTIER_POWER_KEY, 25)));
 }
 
 private void savePrefrences(Context context) {
   SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
   SharedPreferences.Editor editor = sharedPreferences.edit();
   editor.putInt(BRIGHTNESS_KEY, brightness.getProgress());
   editor.putInt(PELTIER_POWER_KEY, peltierPower.getProgress());
   editor.commit();
 }
 
}
