/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.provider.wintab;

import java.awt.AWTEvent;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jpen.PLevel;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.internal.BuildInfo;
import jpen.internal.Range;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.VirtualScreenBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WintabProvider extends AbstractPenProvider {
  private static final Logger L = LoggerFactory.getLogger(WintabProvider.class);

  private static final NativeLibraryLoader LIB_LOADER =
      new NativeLibraryLoader(
          new String[] {""},
          new String[] {"64"},
          Integer.valueOf(
              BuildInfo.getProperties().getString("jpen.provider.wintab.nativeVersion")));

  static void loadLibrary() {
    LIB_LOADER.load();
  }

  /**
   * When this system property is set to true then the provider stops sending events if no AWT
   * events are received after one second.
   */
  public static final String WAIT_AWT_ACTIVITY_SYSTEM_PROPERTY =
      "jpen.provider.wintab.waitAwtActivity";

  private static final boolean WAIT_AWT_ACTIVITY =
      Boolean.valueOf(System.getProperty(WAIT_AWT_ACTIVITY_SYSTEM_PROPERTY));

  static {
    if (WAIT_AWT_ACTIVITY) L.info("WAIT_AWT_ACTIVITY set to true");
  }

  public static final String PERIOD_SYSTEM_PROPERTY = "jpen.provider.wintab.period";
  public static final int PERIOD;

  static {
    String periodString = System.getProperty(PERIOD_SYSTEM_PROPERTY, null);
    int periodValue = 10;
    if (periodString != null)
      try {
        periodValue = Integer.valueOf(periodString);
        if (periodValue <= 0) {
          L.error("ignored illegal PERIOD value {}, period value must be >= 0", periodValue);
          periodValue = 10;
        } else L.info("PERIOD set to {}", periodValue);
      } catch (NumberFormatException ex) {
      }
    PERIOD = periodValue;
  }

  public final WintabAccess wintabAccess;
  private final Map<Integer, WintabDevice> cursorToDevice = new HashMap<Integer, WintabDevice>();
  private final Range[] levelRanges = new Range[PLevel.Type.VALUES.size()];
  final VirtualScreenBounds screenBounds = VirtualScreenBounds.getInstance();
  private final Thread thread;
  private volatile boolean paused = true;

  // by default the tablet device moves the system pointer (cursor)
  private boolean systemCursorEnabled = true;

  public static class Constructor extends AbstractPenProvider.AbstractConstructor {
    public String getName() {
      return "Wintab";
    }

    public boolean constructable(PenManager penManager) {
      String osName = System.getProperty("os.name");
      boolean constructable = osName.toLowerCase().contains("windows");
      L.debug("Wintab constructable check: os.name='{}' => {}", osName, constructable);
      return constructable;
    }

    @Override
    public PenProvider constructProvider() throws Throwable {
      L.info("constructing Wintab provider");
      loadLibrary();
      L.debug("native library loaded, creating WintabAccess");
      WintabAccess wintabAccess = new WintabAccess();
      L.debug("WintabAccess created, instantiating WintabProvider");
      return new WintabProvider(this, wintabAccess);
    }

    @Override
    public int getNativeVersion() {
      return LIB_LOADER.nativeVersion;
    }

    @Override
    public int getNativeBuild() {
      loadLibrary();
      return WintabAccess.getNativeBuild();
    }

    @Override
    public int getExpectedNativeBuild() {
      return Integer.valueOf(
          BuildInfo.getProperties().getString("jpen.provider.wintab.nativeBuild"));
    }
  }

  class MyThread extends Thread implements AWTEventListener {

    private long scheduleTime;
    private long awtEventTime;
    private boolean waitingAwtEvent;
    private int inputEventModifiers;

    {
      setName("jpen-WintabProvider");
      setDaemon(true);
      setPriority(Thread.MAX_PRIORITY);
      if (WAIT_AWT_ACTIVITY) Toolkit.getDefaultToolkit().addAWTEventListener(this, ~0);
    }

    public void run() {
      try {
        KeyboardFocusManager keyboardFocusManager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        long processingTime;
        long correctPeriod;
        boolean waited = true;
        while (true) {
          processingTime = waited ? System.currentTimeMillis() : scheduleTime;
          schedule();
          processingTime = scheduleTime - processingTime;
          correctPeriod = PERIOD - processingTime;
          waited = false;
          synchronized (this) {
            if (correctPeriod > 0) {
              wait(correctPeriod);
              waited = true;
            }
            if (WAIT_AWT_ACTIVITY) {
              waitingAwtEvent =
                  scheduleTime - awtEventTime > 1000
                      && (inputEventModifiers == 0
                          || keyboardFocusManager.getActiveWindow() == null);
              if (waitingAwtEvent) {
                wait(500);
                waited = true;
              }
            }
            while (paused) {
              L.debug("going to wait...");
              wait();
              L.debug("notified");
              waited = true;
            }
          }
        }
      } catch (InterruptedException ex) {
        throw new AssertionError(ex);
      }
    }

    private void schedule() {
      processQueuedEvents();
      scheduleTime = System.currentTimeMillis();
    }

    // @Override
    public synchronized void eventDispatched(AWTEvent ev) {
      InputEvent inputEvent = ev instanceof InputEvent ? (InputEvent) ev : null;
      synchronized (this) {
        awtEventTime = System.currentTimeMillis();
        if (inputEvent != null) inputEventModifiers = inputEvent.getModifiersEx();
        if (!paused && waitingAwtEvent) notify();
      }
    }
  }

  private WintabProvider(Constructor constructor, WintabAccess wintabAccess) {
    super(constructor);
    this.wintabAccess = wintabAccess;
    L.info(
        "WintabProvider initializing: deviceName='{}', packetRate={}, hardwareCapabilities=0x{}, systemCursorEnabled={}",
        wintabAccess.getDeviceName(),
        wintabAccess.getPacketRate(),
        Integer.toHexString(wintabAccess.getDeviceHardwareCapabilities()),
        wintabAccess.getSystemCursorEnabled());

    for (int i = PLevel.Type.VALUES.size(); --i >= 0; ) {
      PLevel.Type levelType = PLevel.Type.VALUES.get(i);
      Range range = wintabAccess.getLevelRange(levelType);
      levelRanges[levelType.ordinal()] = range;
      L.info("WintabProvider level range: {} = {}", levelType, range);
    }

    thread = new MyThread();
    thread.start();
    L.debug("Wintab thread started: {}", thread.getName());
    L.debug("wintabAccess={}", wintabAccess);
  }

  Range getLevelRange(PLevel.Type type) {
    return levelRanges[type.ordinal()];
  }

  private void processQueuedEvents() {
    L.trace("start");
    while (wintabAccess.nextPacket() && !paused) {
      WintabDevice device = getDevice(wintabAccess.getCursor());
      L.trace("device: {}", device.getName());
      device.scheduleEvents();
    }
    L.trace("end");
  }

  private WintabDevice getDevice(int cursor) {
    WintabDevice wintabDevice = cursorToDevice.get(cursor);
    if (wintabDevice == null) {
      L.info(
          "discovered new Wintab cursor: index={}, name='{}', cursorType={}, rawType={}, physicalId={}, buttonCount={}, capabilityMask=0x{}",
          cursor,
          WintabAccess.getCursorName(cursor),
          WintabAccess.getCursorType(cursor),
          WintabAccess.getRawCursorType(cursor),
          WintabAccess.getPhysicalId(cursor),
          WintabAccess.getButtonCount(cursor),
          Integer.toHexString(WintabAccess.getCapabilityMask(cursor)));
      String[] buttonNames = WintabAccess.getButtonNames(cursor);
      if (buttonNames != null && buttonNames.length > 0)
        L.debug("cursor {} button names: {}", cursor, Arrays.toString(buttonNames));
      cursorToDevice.put(cursor, wintabDevice = new WintabDevice(this, cursor));
      devices.clear();
      devices.addAll(cursorToDevice.values());
      L.info(
          "Wintab device added (total devices={}): {}", cursorToDevice.size(), wintabDevice.getName());
      getPenManager().firePenDeviceAdded(getConstructor(), wintabDevice);
    }
    return wintabDevice;
  }

  // @Override
  public void penManagerPaused(boolean paused) {
    setPaused(paused);
  }

  synchronized void setPaused(boolean paused) {
    L.trace("executing setPaused()");
    if (paused == this.paused) return;
    this.paused = paused;
    if (!paused) {
      L.debug("false paused value");
      screenBounds.reset();
      synchronized (thread) {
        L.debug("going to notify all...");
        thread.notifyAll();
        L.debug("done notifying ");
      }
      wintabAccess.enable(true);
    }
  }

  @Override
  public boolean getUseRelativeLocationFilter() {
    return systemCursorEnabled;
  }

  /**
   * @param systemCursorEnabled If {@code false} then tablet movement on Wintab devices doesn't
   *     cause movement on the system mouse pointer. {@code true} then tablet movement on Wintab
   *     devices cause movement on the system mouse pointer, this is the default value.
   */
  public synchronized void setSystemCursorEnabled(boolean systemCursorEnabled) {
    if (this.systemCursorEnabled == systemCursorEnabled) return;
    this.systemCursorEnabled = systemCursorEnabled;
    wintabAccess.setSystemCursorEnabled(systemCursorEnabled);
  }

  public synchronized boolean getSystemCursorEnabled() {
    return systemCursorEnabled;
  }
}
