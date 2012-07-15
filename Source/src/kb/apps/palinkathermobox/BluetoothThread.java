package kb.apps.palinkathermobox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * handles the bluetooth data transport
 * */
public class BluetoothThread extends Thread {

  private BluetoothAdapter adapter; /** bluetooth adapter object */
  private BluetoothDevice device;   /** bluetooth device object (connected to) */
  private BluetoothSocket socket;   /** bluetooth socket */
  private InputStream input;        /** input stream */
  private OutputStream output;      /** output stream */
  private final Events event;       /** event handler object */
  
  private static final String TAG = "BluetoothThread";
    
  /**
   * constructor
   * @param adapter bluetooth adapter
   * @param device the remote device (server) which intend to connect to
   * @param uuid service id
   * @param event event handler object
   * */
  public BluetoothThread(BluetoothAdapter adapter, BluetoothDevice device, UUID uuid, Events event) {
    this.adapter = adapter;
    this.device = device;
    this.event = event;
    
    BluetoothSocket tmpSocket = null;
    try {         
      //Process process = Runtime.getRuntime().exec("sdptool records " + device.getAddress());
      //process.waitFor();                              
      tmpSocket = this.device.createRfcommSocketToServiceRecord(uuid);
      socket = tmpSocket;      
    //} catch (IOException e) {
    } catch (Exception e) {
      onError("device.createRfcommSocketToServiceRecord() failed, e: " + e.getMessage());
    }
  }  

  /**
   * thread execution method
   * */
  @Override
  public void run() {
    // cancel discovery because it slows down the connection
    adapter.cancelDiscovery();
    try {
      socket.connect();
      input = socket.getInputStream();
      output = socket.getOutputStream();
    } catch (IOException e) {
      input = null;
      output = null;
      onError("service thread starting failed, e: " + e.getMessage());
      try {
        socket.close();
      } catch (IOException e2) {
        onError("socket.close() failed during connection failure, e: " + e2.getMessage());
      }
      return;
    }     

    byte[] buffer = new byte[1024];
    int bytes = 0;
    List<byte[]> data = new Vector<byte[]>();
    
    while(true) {
      try {
        bytes = input.read(buffer);
        // check stream is completely arrived
        if (bytes == -1) {          
          if (event != null) {            
            Object[] tmp = data.toArray();
            byte[] fullData = new byte[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
              fullData[i] = (Byte)tmp[i];
            }
            event.onDataReceived(fullData);
          }
          data.clear();
        } else {
          data.add(buffer);
        }
      } catch (IOException e) {
        onError("socket.close() failed during socket reading failure, e: " + e.getMessage());
        break;
      }    
    }
  }
    
  /**
   * writes buffer to output stream
   * @param buffer buffer to write
   * */
  public void write(byte[] buffer) {
    if (output != null && isAlive()) {
      try {
        output.write(buffer);
      } catch (IOException e) {
        onError("Exception during write, e: " + e.getMessage());
      }
    }
  }
  
  public void write(int out) {
    if (output != null && isAlive()) {
      try {
        output.write(out);
      } catch (IOException e) {
        onError("Exception during write, e: " + e.getMessage());
      }
    }
  }
  
  /**
   * cancels the thread
   * */
  public void cancel() {
    try {
      socket.close();
    } catch (IOException e) {
      onError("socket.close() failed, e: " + e.getMessage());
    }    
  }
  
  /**
   * call error message callback
   * @param errorMsg error message
   * */
  private void onError(String errorMsg) {
    event.onErrorMessage(TAG + ": " + errorMsg);
  }
}
