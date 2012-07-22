package kb.apps.palinkathermobox;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import kb.apps.palinkathermobox.BluetoothThread;

/**
 * implements the bluetooth service
 * */
public class BluetoothService implements ServiceEvents {

  private BluetoothAdapter adapter;       /** bluetooth adapter object */
  private BluetoothDevice device;         /** bluetooth device object (connected to) */
  private BluetoothThread thread;         /** bluetooth service thread object */
  private final ApplicationEvents event;  /** event handler object */
  private UUID uuid;                      /** service id */
  private String command;                 /** received command */
    
  /**
   * constructor
   * @param adapter the bluetooth adapter in the used device
   * @param device the remote device (server) which intend to connect to
   * @param btUuid service id
   * @param event event handler object
   * */
  public BluetoothService(BluetoothAdapter adapter, BluetoothDevice device, UUID btUuid, ApplicationEvents event) {
    this.adapter = adapter;
    this.device = device;
    this.event = event;
    uuid = btUuid;
    command = "";
  }
  
  /**
   * starts the service
   * */
  public synchronized void start(boolean wait) {
    if (thread != null) {
      thread.cancel();
      thread = null;      
    }
    thread = new BluetoothThread(adapter, device, uuid, this, wait);
    thread.start();
  }
  
  /**
   * stops the service
   * */
  public synchronized void stop() {
    if (thread != null) {
      thread.cancel();
      thread = null;
    }
  }
  
  /**
   * returns true if service is running otherwise false
   * */
  public synchronized boolean isRunning() {
    boolean ret = false;
    
    if (thread != null) {
      ret = thread.isAlive();
    }
    return ret;
  }
  
  /**
   * writes buffer to output stream
   * @param buffer buffer to write
   * */
  public void write(byte[] buffer) {
    if (thread != null) {
      thread.write(buffer);
    }
  }
  
  /**
   * writes integer value to output stream
   * @param out integer value
   * */
  public void write(int out) {
    if (thread != null) {
      thread.write(out);
    }
  }

  @Override
  public void onDataReceived(byte[] data) {    
    StringBuilder cmd = new StringBuilder();
    StringBuilder value = new StringBuilder();   
    int cmdLength = -1;    
    
    for (int i = 0; i < data.length; ++i) {
      command += (char) data[i];
    }
    cmdLength = getCommand(command, cmd, value); 
    if (cmdLength != -1) {
      if (event != null) {
        event.onCommand(cmd.toString(), value.toString());
      }
      command = command.replaceAll(command.substring(0, cmdLength), "");
    }
  }

  @Override
  public void onErrorMessage(String errorMsg) {
    if (event != null) {
      event.onErrorMessage(errorMsg);
    }
  }
  
  /**
   * check full command receive do not
   * returns command length if command is available otherwise -1
   * @param data raw command string
   * @param cmd received command
   * @param value received value 
   * */
  private int getCommand(String data, StringBuilder cmd, StringBuilder value) {
    int index = data.indexOf("\r\n");
    
    onErrorMessage(data); // TODO remove debug
    
    if (index != -1) {
      int cmdEnd = data.lastIndexOf("_");
      if (cmdEnd != -1) {
        cmd.append(data.substring(0, cmdEnd));
        value.append(data.substring((cmdEnd + 1), (data.length() - 1)));
      }
      index += 2;
    }
    return index;
  }
  
  /**
   * set actual status
   * */
  private void setStatus(int status) {
    if (event != null) {
      event.onStatusChanged(status);
    }
  }

  @Override
  public void onStatusChanged(int status) {
    setStatus(status);
  }
    
}
