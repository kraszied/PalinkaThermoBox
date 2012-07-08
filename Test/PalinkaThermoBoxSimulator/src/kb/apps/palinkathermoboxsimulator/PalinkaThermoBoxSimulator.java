package kb.apps.palinkathermoboxsimulator;

public class PalinkaThermoBoxSimulator {

  public static void main(String[] args) {
    Thread waitThread = new Thread(new WaitThread());
    waitThread.start();
  }

}
