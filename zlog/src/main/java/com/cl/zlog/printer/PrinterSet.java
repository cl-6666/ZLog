/*
 * Copyright 2016 cl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cl.zlog.printer;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Represents a group of Printers that should used to print logs in the same time, each printer
 * may probably print the log to different place.
 */
public class PrinterSet implements Printer, Flushable, Closeable {

  private Printer[] printers;

  /**
   * Constructor, pass printers in and will use all these printers to print the same logs.
   *
   * @param printers the printers used to print the same logs
   */
  public PrinterSet(Printer... printers) {
    this.printers = printers.clone();
  }

  @Override
  public void println(int logLevel, String tag, String msg) {
    for (Printer printer : printers) {
      printer.println(logLevel, tag, msg);
    }
  }

  @Override
  public void flush() throws IOException {
    IOException failure = null;
    for (Printer printer : printers) {
      if (printer instanceof Flushable) {
        try {
          ((Flushable) printer).flush();
        } catch (Exception e) {
          if (failure == null) {
            failure = new IOException("Flush printer failed", e);
          }
        }
      }
    }
    if (failure != null) {
      throw failure;
    }
  }

  @Override
  public void close() throws IOException {
    IOException failure = null;
    for (int i = printers.length - 1; i >= 0; i--) {
      Printer printer = printers[i];
      if (printer instanceof Closeable) {
        try {
          ((Closeable) printer).close();
        } catch (Exception e) {
          if (failure == null) {
            failure = new IOException("Close printer failed", e);
          }
        }
      }
    }
    if (failure != null) {
      throw failure;
    }
  }
}
