/*
 * Copyright 2015 cl
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

package com.cl.zlog;

import com.cl.zlog.formatter.border.BorderFormatter;
import com.cl.zlog.formatter.message.json.JsonFormatter;
import com.cl.zlog.formatter.message.object.ObjectFormatter;
import com.cl.zlog.formatter.message.throwable.ThrowableFormatter;
import com.cl.zlog.formatter.message.xml.XmlFormatter;
import com.cl.zlog.formatter.stacktrace.StackTraceFormatter;
import com.cl.zlog.formatter.thread.ThreadFormatter;
import com.cl.zlog.interceptor.Interceptor;
import com.cl.zlog.internal.SystemCompat;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cl.zlog.LogLevel.ALL;
import static com.cl.zlog.LogLevel.DEBUG;
import static com.cl.zlog.LogLevel.ERROR;
import static com.cl.zlog.LogLevel.INFO;
import static com.cl.zlog.LogLevel.NONE;
import static com.cl.zlog.LogLevel.VERBOSE;
import static com.cl.zlog.LogLevel.WARN;
import static org.junit.Assert.assertTrue;

public class ZLogTest {

  private static final String MESSAGE = "message";

  private static final String DEFAULT_TAG = "ZLOG";

  private static final String CUSTOM_TAG = "custom_tag";

  private static final String CUSTOM_PRINTER_MSG_PREFIX = "message from custom printer - ";

  private List<LogItem> logsContainer = new ArrayList<>();

  @Before
  public void setup() {
    ZLogUtil.beforeTest();
    ZLog.init(new LogConfiguration.Builder().logLevel(ALL).tag(DEFAULT_TAG).build(),
        new ContainerPrinter(logsContainer));
  }

  @Test
  public void testSimpleLogging() {
    ZLog.i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, MESSAGE);
  }

  @Test
  public void testLogLevel() {
    ZLog.i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, MESSAGE);

    // Test WARN
    Logger logger = ZLog.logLevel(WARN).build();
    logsContainer.clear();
    logger.i(MESSAGE);
    AssertUtil.assertNoLog(logsContainer);
    logsContainer.clear();
    logger.w(MESSAGE);
    assertLog(WARN, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.e(MESSAGE);
    assertLog(ERROR, DEFAULT_TAG, MESSAGE);

    // Test NONE
    logger = ZLog.logLevel(NONE).build();
    logsContainer.clear();
    logger.log(-1, MESSAGE);
    logger.log(0, MESSAGE);
    logger.log(1, MESSAGE);
    logger.v(MESSAGE);
    logger.d(MESSAGE);
    logger.i(MESSAGE);
    logger.w(MESSAGE);
    logger.e(MESSAGE);
    logger.log(7, MESSAGE);
    logger.log(8, MESSAGE);
    logger.log(9, MESSAGE);
    AssertUtil.assertNoLog(logsContainer);


    // Test ALL
    logger = ZLog.logLevel(ALL).build();
    logsContainer.clear();
    logger.log(-1, MESSAGE);
    assertLog(-1, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.log(0, MESSAGE);
    assertLog(0, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.log(1, MESSAGE);
    assertLog(1, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.v(MESSAGE);
    assertLog(VERBOSE, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.d(MESSAGE);
    assertLog(DEBUG, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.w(MESSAGE);
    assertLog(WARN, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.e(MESSAGE);
    assertLog(ERROR, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.log(7, MESSAGE);
    assertLog(7, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.log(8, MESSAGE);
    assertLog(8, DEFAULT_TAG, MESSAGE);
    logsContainer.clear();
    logger.log(9, MESSAGE);
    assertLog(9, DEFAULT_TAG, MESSAGE);
  }

  @Test
  public void testTag() {
    ZLog.i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, MESSAGE);

    logsContainer.clear();
    ZLog.tag(CUSTOM_TAG).i(MESSAGE);
    assertLog(INFO, CUSTOM_TAG, MESSAGE);
  }

  @Test
  public void testThread() {
    ZLog.enableThreadInfo().i("Message with thread info");
    boolean result = (logsContainer.size() == 1
        && logsContainer.get(0).msg.contains("Thread: "));
    assertTrue("No thread info found", result);

    logsContainer.clear();
    ZLog.disableThreadInfo().i("Message without thread info");
    result = (logsContainer.size() == 1
        && !logsContainer.get(0).msg.contains("Thread: "));
    assertTrue("Thread info found", result);
  }

  @Test
  public void testStackTrace() {
    ZLog.enableStackTrace(1).i("Message with stack trace, depth 1");
    boolean result = (logsContainer.size() == 1
        && logsContainer.get(0).msg.contains("\t─ "));
    assertTrue("No stack trace found", result);

    logsContainer.clear();
    ZLog.enableStackTrace(2).i("Message with stack trace, depth 2");
    result = (logsContainer.size() == 1
        && logsContainer.get(0).msg.contains("\t├ "));
    assertTrue("No stack trace found", result);

    logsContainer.clear();
    ZLog.disableStackTrace().i("Message without stack trace");
    result = (logsContainer.size() == 1
        && !logsContainer.get(0).msg.contains("\t├ "));
    assertTrue("Stack trace found", result);
  }

  @Test
  public void testBorder() {
    ZLog.enableBorder().i("Message with a border");
    boolean result = (logsContainer.size() == 1
        && logsContainer.get(0).msg.startsWith("╔═══")
        && logsContainer.get(0).msg.endsWith("════"));
    assertTrue("No bordered log found", result);

    logsContainer.clear();
    ZLog.disableBorder().i("Message without a border");
    result = (logsContainer.size() == 1
        && !logsContainer.get(0).msg.startsWith("╔═══")
        && !logsContainer.get(0).msg.endsWith("════"));
    assertTrue("Bordered log found", result);
  }

  @Test
  public void testObject() {
    Date date = new Date();
    ZLog.addObjectFormatter(Date.class, new ObjectFormatter<Date>() {
      @Override
      public String format(Date date) {
        return Long.toString(date.getTime());
      }
    }).i(date);
    boolean result = (logsContainer.size() == 1
        && logsContainer.get(0).msg.equals(Long.toString(date.getTime())));
    assertTrue("Formatted object log not found", result);
  }

  @Test
  public void testModifyingInterceptor() {
    ZLog.addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.tag = CUSTOM_TAG;
        return log;
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.msg = log.msg + "[i1]";
        return log;
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.msg = log.msg + "[i2]";
        return log;
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.level = DEBUG;
        return log;
      }
    }).i(MESSAGE);
    assertLog(DEBUG, CUSTOM_TAG, MESSAGE + "[i1][i2]");
  }

  @Test
  public void testReplacingInterceptor() {
    ZLog.addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        return new LogItem(LogLevel.VERBOSE, "tag1", "msg1");
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        return new LogItem(LogLevel.DEBUG, "tag2", "msg2");
      }
    }).i(MESSAGE);
    assertLog(DEBUG, "tag2", "msg2");
  }

  @Test
  public void testBlockingInterceptor() {
    ZLog.addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.msg = "i1";
        return log;
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        // Block the log.
        return null;
      }
    }).addInterceptor(new Interceptor() {
      @Override
      public LogItem intercept(LogItem log) {
        log.msg = "i2";
        return log;
      }
    }).i(MESSAGE);
    AssertUtil.assertNoLog(logsContainer);
  }

  @Test
  public void testCustomJsonFormatter() {
    ZLog.jsonFormatter(
        new JsonFormatter() {

          @Override
          public String format(String data) {
            return "This is a json string: " + data;
          }
        })
        .json("{name=xlog}");
    assertLog(DEBUG, DEFAULT_TAG, "This is a json string: {name=xlog}");
  }

  @Test
  public void testCustomXmlFormatter() {
    ZLog.xmlFormatter(
        new XmlFormatter() {

          @Override
          public String format(String data) {
            return "This is a xml string: " + data;
          }
        })
        .xml("<note name=\"xlog\">");
    assertLog(DEBUG, DEFAULT_TAG, "This is a xml string: <note name=\"xlog\">");
  }

  @Test
  public void testCustomThrowableFormatter() {
    final String formattedThrowable = "This is a throwable";
    ZLog.throwableFormatter(
        new ThrowableFormatter() {
          @Override
          public String format(Throwable data) {
            return formattedThrowable;
          }
        })
        .i(MESSAGE, new Throwable());
    assertLog(INFO, DEFAULT_TAG, MESSAGE + "\n" + formattedThrowable);
  }

  @Test
  public void testCustomThreadFormatter() {
    final String formattedThread = "This is the thread info";
    ZLog.threadFormatter(
        new ThreadFormatter() {
          @Override
          public String format(Thread data) {
            return formattedThread;
          }
        })
        .enableThreadInfo()
        .i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, formattedThread + "\n" + MESSAGE);
  }

  @Test
  public void testCustomStackTraceFormatter() {
    final String formattedStackTrace = "This is the stack trace";
    ZLog.stackTraceFormatter(
        new StackTraceFormatter() {
          @Override
          public String format(StackTraceElement[] data) {
            return formattedStackTrace;
          }
        })
        .enableStackTrace(1)
        .i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, formattedStackTrace + "\n" + MESSAGE);
  }

  @Test
  public void testCustomBorderFormatter() {
    ZLog.enableThreadInfo().threadFormatter(new ThreadFormatter() {
      @Override
      public String format(Thread data) {
        return "T1";
      }
    }).enableBorder().borderFormatter(new BorderFormatter() {
      @Override
      public String format(String[] segments) {
        return addCustomBorder(segments);
      }
    }).i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, addCustomBorder(new String[]{"T1", MESSAGE}));
  }

  private String addCustomBorder(String[] segments) {
    if (segments == null || segments.length == 0) {
      return "";
    }

    String[] nonNullSegments = new String[segments.length];
    int nonNullCount = 0;
    for (String segment : segments) {
      if (segment != null) {
        nonNullSegments[nonNullCount++] = segment;
      }
    }
    if (nonNullCount == 0) {
      return "";
    }
    StringBuilder msgBuilder = new StringBuilder();
    msgBuilder.append("<<").append(SystemCompat.lineSeparator);
    for (int i = 0; i < nonNullCount; i++) {
      msgBuilder.append(nonNullSegments[i]);
      if (i != nonNullCount - 1) {
        msgBuilder.append(SystemCompat.lineSeparator).append("--")
            .append(SystemCompat.lineSeparator);
      } else {
        msgBuilder.append(SystemCompat.lineSeparator).append(">>");
      }
    }
    return msgBuilder.toString();
  }

  @Test
  public void testCustomPrinter() {
    ZLog.printers(
        new ContainerPrinter(logsContainer) {

          @Override
          protected LogItem onPrint(LogItem logItem) {
            logItem.msg = CUSTOM_PRINTER_MSG_PREFIX + logItem.msg;
            return logItem;
          }
        })
        .i(MESSAGE);
    assertLog(INFO, DEFAULT_TAG, CUSTOM_PRINTER_MSG_PREFIX + MESSAGE);
  }

  private void assertLog(int logLevel, String tag, String msg) {
    AssertUtil.assertHasLog(logsContainer, new LogItem(logLevel, tag, msg));
  }
}