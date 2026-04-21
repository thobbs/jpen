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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jpen.PenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inspector {
  static final Logger L = LoggerFactory.getLogger(Inspector.class);

  final PenManager penManager;
  final PrintWriter writer;
  final InspectorThread inspectorThread;

  public Inspector(PenManager penManager, String loggerName, int periodInSec) {
    this.penManager = penManager;
    String fileName = evalFileName(loggerName);
    try {
      this.writer = new PrintWriter(new FileWriter(fileName), true);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    L.info("logging to file: {}", fileName);
    inspectorThread = new InspectorThread(this, periodInSec * 1000 / 2, 2);
  }

  void close() {
    writer.close();
  }

  private String evalFileName(String loggerName) {
    StringBuilder sb = new StringBuilder(loggerName);
    sb.append("-inspect-");
    sb.append(new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
    sb.append(".txt");
    return sb.toString();
  }
}
