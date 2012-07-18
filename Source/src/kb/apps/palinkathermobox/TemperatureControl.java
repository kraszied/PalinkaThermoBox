package kb.apps.palinkathermobox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class TemperatureControl extends View {

	public final static int MINIMUM_TEMPERATURE = 8;
	public final static int MAXIMUM_TEMPERATURE = 24; //TODO : Confirm values
	public final static double PERCENTAGE_TO_ANGLE =  1.8;
	
	private Paint paint;
	private int pathPercentage; 
	private double blackControlDegrees;
	
	public TemperatureControl(Context context,AttributeSet atSet , int defstyle)
	{
		super(context,atSet,defstyle);
		paint = new Paint();
		pathPercentage = 18; // TODO : Fetch from a saved value
	}
	
	public TemperatureControl(Context context,AttributeSet atSet)
	{
		super(context,atSet);
		paint = new Paint();
		pathPercentage = 18; // TODO : Fetch from a saved value
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
		
		//Draw current temp
		
		paint.setARGB(240, 40, 40, 40);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(35);
		
		//Draw small black circle
		//depends on temeperatures
		double meterXDifference = 145 * Math.cos(blackControlDegrees);
		double meterYDifference = 145 * Math.sin(blackControlDegrees);
		float meterXPosition = (float) (this.getWidth()/2 + meterXDifference);
		float meterYPosition = (float) (this.getHeight()/2 + meterYDifference);
		canvas.drawCircle(meterXPosition, meterYPosition, 15, paint);
		
		//Text-Drawing here
		String currenttemp = String.valueOf(pathPercentage) + "°C"; //TODO : Remove after refresh
		canvas.drawText(currenttemp, this.getWidth()/2, this.getHeight()/2, paint);
		
		//Meter values drawing
		paint.setARGB(255, 240, 230, 20);
		paint.setTextSize(18);
		int meterTemperature = MINIMUM_TEMPERATURE;
		float textDegrees = 0;
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
	
	
	public void setCurrentTemperature()
	{
		//TODO : Get current temperature level from bluetooth
	}
	
}
