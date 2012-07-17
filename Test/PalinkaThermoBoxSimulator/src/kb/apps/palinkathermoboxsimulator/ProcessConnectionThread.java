package kb.apps.palinkathermoboxsimulator;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.microedition.io.StreamConnection;

public class ProcessConnectionThread implements Runnable {

  private StreamConnection mConnection;  
  private OutputStream output;

  // Constant that indicate command from devices
  private static final int EXIT_CMD = -1;
  private static final int KEY_RIGHT = 1;
  private static final int KEY_LEFT = 2;

  public ProcessConnectionThread(StreamConnection connection) {
    mConnection = connection;
  }

  @Override
  public void run() {
    try {

      // prepare to receive data
      InputStream inputStream = mConnection.openInputStream();
      output = mConnection.openDataOutputStream();

      System.out.println("waiting for input");

      byte[] buffer = new byte[1024];
      while (true) {
        //int command = inputStream.read();

        //if (command == EXIT_CMD) {
          //System.out.println("finish process");
          //break;
        //}

        //processCommand(command);
        
        int bytes = inputStream.read(buffer);
        if (bytes > 0) {
          System.out.println(Arrays.toString(buffer));        
          String answer = "T_IN_20\r\n";
          output.write(answer.getBytes());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Process the command from client
   * 
   * @param command
   *          the command code
   */
  private void processCommand(int command) {
    try {
      Robot robot = new Robot();
      String answer = "";
      switch (command) {
      case KEY_RIGHT:
        robot.keyPress(KeyEvent.VK_RIGHT);
        System.out.println("Right");
        answer = "T_IN_20\r\n";
        break;
      case KEY_LEFT:
        robot.keyPress(KeyEvent.VK_LEFT);
        System.out.println("Left");
        answer = "M_LaPutaMadre\r\n";
        break;
      }
      
      output.write(answer.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
