/* [{
Copyright 2007, 2008, 2009 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.demo.inspect;

import java.util.Date;
import java.util.Map;
import jpen.demo.StatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InspectorThread implements Runnable {
  static final Logger L = LoggerFactory.getLogger(InspectorThread.class);

  final Inspector inspector;
  final long period;
  final long times;

  InspectorThread(Inspector inspector, long period, long times) {
    if (period <= 0) throw new IllegalArgumentException("period must be greater than 0");
    this.inspector = inspector;
    this.period = period;
    this.times = times;
    Thread t = new Thread(this, "JPen-Inspector");
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();
  }

  // @Override
  public synchronized void run() {
    try {
      long totalTime = times * period / 1000;
      L.warn("JPen Demo will automatically shut down in {} seconds", totalTime);
      logInfo(0);
      for (int i = 1; i <= times; i++) {
        wait(period);
        logInfo(i);
      }
      inspector.close();
      L.info("shutting down...");
      System.exit(0);
    } catch (InterruptedException ex) {
      L.warn("interrupted!", ex);
    }
  }

  private void logInfo(int count) {
    L.info("collecting info {}/{}", count, times);
    inspector.writer.println(new StatusReport(inspector.penManager));
    inspector.writer.println(evalThreadsDump(count));
    L.info("info collected");
  }

  private String evalThreadsDump(int count) {
    StringBuilder sb = new StringBuilder();
    Map<Thread, StackTraceElement[]> threadToStackTraces = Thread.getAllStackTraces();
    sb.append("=== (" + count + ") Stack Traces " + new Date() + " ===\n");
    for (Map.Entry<Thread, StackTraceElement[]> threadToStackTracesE :
        threadToStackTraces.entrySet()) {
      Thread thread = threadToStackTracesE.getKey();
      sb.append(thread + ", state=" + thread.getState() + ", isAlive=" + thread.isAlive());
      sb.append("\n");
      for (StackTraceElement stackTraceElement : threadToStackTracesE.getValue()) {
        sb.append("\t");
        sb.append(stackTraceElement);
        sb.append("\n");
      }
    }
    sb.append("=== === ===");
    return sb.toString();
  }
}
