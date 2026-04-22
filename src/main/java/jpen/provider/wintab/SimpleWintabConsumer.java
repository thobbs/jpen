package jpen.provider.wintab;

import jpen.PLevel;
import jpen.internal.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWintabConsumer {

  private static final Logger L = LoggerFactory.getLogger(WintabAccess.class);

  final WintabAccess wintabAccess;
  final Range pressureRange;
  final PressureLoggerThread loggerThread;

  public SimpleWintabConsumer() {
    WintabProvider.loadLibrary();
    try {
      wintabAccess = new WintabAccess();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize WintabAccess", e);
    }

    pressureRange = wintabAccess.getLevelRange(PLevel.Type.PRESSURE);
    loggerThread = new PressureLoggerThread();

  }

  public float getCurrentPressure() {
    float rawValue = wintabAccess.getValue(WintabAccess.getLevelTypeValueIndex(PLevel.Type.PRESSURE));
    return pressureRange.getRangedValue(rawValue);
  }

  public void startLogger() {
    loggerThread.start();
  }

  class PressureLoggerThread extends Thread {
    {
      setName("jpen-PressureLoggerThread");
      setDaemon(true);
    }

    public void run() {
      try {
        while (true) {
          L.info("currentPressure: {}", getCurrentPressure());
          Thread.sleep(500);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
