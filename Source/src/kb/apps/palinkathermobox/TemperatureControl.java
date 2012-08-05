package kb.apps.palinkathermobox;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TemperatureControl extends View {

	public final static int METER_RADIUS = 15; 
	public final static int METER_SLACK = 10;
	
	public final static int MINIMUM_TEMPERATURE = 8;
	public final static int MAXIMUM_TEMPERATURE = 24; //TODO : Confirm values
	public final static double PERCENTAGE_TO_ANGLE =  1.8;
	
	public static final String PREFERENCE_NAME = "PALINCAR_REFERENCE";    /** prefernece name */
	private static final String SAVED_PERCENTAGE_KEY = "PathPercentage";  /** stored temperature key */
  
  private static final double NO_INPUT_TEMP = -99;                /** there is no input temperature */
  private static final String NO_INPUT_TEMP_STRING = "-.-- °C";   /** until the device is not connected this value is shown */

	private Paint paint;
	private int pathPercentage; 
	private double blackControlDegrees;
	
	private float meterXPosition;
	private float meterYPosition;
	private boolean isMeterPressed;
	private ApplicationEvents event;
	private Context context;
	
  private volatile double temperatureLevel = -99;
	
	public TemperatureControl(Context context, AttributeSet atSet)
	{
		super(context,atSet);
		paint = new Paint();
		pathPercentage = 75;
		meterXPosition = 0;
		meterYPosition = 0;
		isMeterPressed = false;
		
    // load the stored temperature data
		this.context = context;
		loadPreferences(this.context);
	}
	
	//public void onDestroy() {
	  //savePrefrences(context);
	//}
	
	public void setEventHandler(ApplicationEvents event) {
	  this.event = event;
	}
	
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		paint.setAntiAlias(true);
		
		
		canvas.drawARGB(255, 20, 20, 20);
		
		//Main, big yellow circle.
		paint.setARGB(255, 240, 230, 20);
		canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, 160,paint);
		
		//inner black circle
		paint.setARGB(255, 0, 0, 0);
		canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, 130,paint);
		canvas.drawLine(this.getWidth()/2-160, this.getHeight()/2,this.getWidth()/2+160, this.getHeight()/2, paint);
		canvas.drawLine(this.getWidth()/2, this.getHeight()/2,this.getWidth()/2, this.getHeight()/2 + 160, paint);
		paint.setARGB(255, 240, 230, 20);
		canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, 126,paint);
		
		blackControlDegrees = (pathPercentage * PERCENTAGE_TO_ANGLE) * Math.PI/180;
		
		
		
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(45);
		
		//Draw small black circle
		//depends on temeperatures
		double meterXDifference = 145 * Math.cos(blackControlDegrees);
		double meterYDifference = 145 * Math.sin(blackControlDegrees);
		meterXPosition = (float) (this.getWidth()/2 + meterXDifference); 
		meterYPosition = (float) (this.getHeight()/2 + meterYDifference);
		if(isMeterPressed)
		{
			paint.setARGB(255, 190, 40, 40);
			
		}
		else
		{
			paint.setARGB(255, 40, 40, 40);
		}
		canvas.drawCircle(meterXPosition, meterYPosition, METER_RADIUS, paint);
		
		//Text-Drawing here
		paint.setARGB(255, 40, 40, 40);
		String temperature = String.format("%.01f °C", temperatureLevel);
		if (temperatureLevel == NO_INPUT_TEMP) {
		  temperature = NO_INPUT_TEMP_STRING;
		}
		canvas.drawText(temperature, this.getWidth()/2, (this.getHeight()/2) + ((paint.getTextSize() / 2) - 4), paint);
		
		//Meter values drawing
		paint.setARGB(255, 240, 230, 20);
		paint.setTextSize(18);
		int meterTemperature = MINIMUM_TEMPERATURE;
		float textDegrees = 0;
		String currenttemp;
		while(meterTemperature <= MAXIMUM_TEMPERATURE)
		{
			currenttemp = String.valueOf(meterTemperature);
			currenttemp += "°C";
			canvas.drawText(currenttemp, 
							(float) (this.getWidth()/2 + 185 * Math.cos(textDegrees * Math.PI/180)), 
							(float) ( (this.getHeight()/2 + 10) + 185 * Math.sin(textDegrees * Math.PI/180)), 
							paint);
			meterTemperature += 2;
			textDegrees += 22.5;
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		
		
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{		
			double hitRadius = Math.sqrt( Math.pow( (event.getX() - meterXPosition), 2) + Math.pow( (event.getY() - meterYPosition), 2) );
			if(hitRadius < METER_RADIUS + METER_SLACK)
			{
				isMeterPressed = true;
				invalidate();
				return true;
			}

		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE)
		{
			double angleDegrees = Math.atan2(event.getY() - this.getHeight()/2, event.getX() - this.getWidth()/2 );
			angleDegrees = Math.abs(angleDegrees) * 180/Math.PI;
			pathPercentage = (int) (angleDegrees * (1/PERCENTAGE_TO_ANGLE));
			invalidate();
			return true;
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			isMeterPressed = false;
			invalidate();
			//Here, call the BT service and send the new temperature!!
			if (this.event != null) {			  
			  this.event.onTemperatureChanged(this.getTemperatureLevel());
			  savePrefrences(context);
			}
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	
	public double getTemperatureLevel()
	{
		int range = MAXIMUM_TEMPERATURE - MINIMUM_TEMPERATURE;
		return (MINIMUM_TEMPERATURE + range * ( (double)pathPercentage/100 )); 
		
	}
	
	public void setCurrentTemperature(double temperature)
	{
	  temperatureLevel = temperature;
	  invalidate();
	}
	
	private void loadPreferences(Context context) {
	   SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
	   pathPercentage = Integer.valueOf(sharedPreferences.getInt(SAVED_PERCENTAGE_KEY, 75));	    
	}
	
	private void savePrefrences(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt(SAVED_PERCENTAGE_KEY, pathPercentage);
    editor.commit();
	}
}
