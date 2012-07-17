package kb.apps.palinkathermobox;

public interface ServiceEvents {
  public void onDataReceived(byte[] data);
  public void onErrorMessage(String errorMsg);
}
