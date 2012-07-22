package kb.apps.palinkathermobox;

public interface ApplicationEvents {
  public void onCommand(String command, String value);
  public void onErrorMessage(String errorMsg);
  public void onStatusChanged(int status);
  public void onTemperatureChanged(double value);
}
