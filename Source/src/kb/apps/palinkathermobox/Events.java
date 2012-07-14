package kb.apps.palinkathermobox;

public interface Events {
  public void onDataReceived(byte[] data);
  public void onErrorMessage(String errorMsg);
  public void onUserMessage(String userMsg);
}
