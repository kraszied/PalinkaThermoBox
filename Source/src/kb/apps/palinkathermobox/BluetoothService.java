package kb.apps.palinkathermobox;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import kb.apps.palinkathermobox.BluetoothThread;

public class BluetoothService {

  private BluetoothAdapter adapter; /** bluetooth adapter object */
  private BluetoothDevice device;   /** bluetooth device object (connected to) */
  private BluetoothThread service;  /** bluetooth service thread object */
  private final Events event;       /** event handler object */
  private UUID uuid;                /** service id */
  
  /**
   * constructor
   * @param adapter the bluetooth adapter in the used device
   * @param device the remote device (server) which intend to connect to
   * @param btUuid service id
   * @param event event handler object
   * */
  public BluetoothService(BluetoothAdapter adapter, BluetoothDevice device, UUID btUuid, Events event) {
    this.adapter = adapter;
    this.device = device;
    this.event = event;
    uuid = btUuid;
  }
  
  /**
   * starts the service
   * */
  public synchronized void start() {
    if (service != null) {
      service.cancel();
      service = null;      
    }
    service = new BluetoothThread(adapter, device, uuid, this.event);
    service.start();
  }
  
  /**
   * stops the service
   * */
  public synchronized void stop() {
    if (service != null) {
      service.cancel();
      service = null;      
    }
  }
  
  /**
   * returns true if service is running otherwise false
   * */
  public synchronized boolean isRunning() {
    boolean ret = false;
    if (service != null) {
      ret = service.isAlive();
    }
    return ret;
  }
  
  /**
   * writes buffer to output stream
   * @param buffer buffer to write
   * */
  public void write(byte[] buffer) {
    service.write(buffer);
  }
  
  public void write(int out) {
    service.write(out);
  }
    
}
