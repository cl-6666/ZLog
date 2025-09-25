package com.cl.test;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cl.zlog.LogConfiguration;
import com.cl.zlog.LogLevel;
import com.cl.zlog.ZLog;
import com.cl.zlog.flattener.PatternFlattener;
import com.cl.zlog.printer.AndroidPrinter;
import com.cl.zlog.printer.file.FilePrinter;
import com.cl.zlog.printer.file.naming.FileNameGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ZLog 包装器类
 * 提供更简洁的日志接口，支持自定义文件名生成和格式化
 * 借鉴 XLogWrapper 的设计思路
 */
public class ZLogWrapper {
    private static final String TAG = "ZLog-Demo";
    private static final String FLATTENER = "{d yyyy-MM-dd HH:mm:ss.SSS} {l}/{t}: {m}";
    private static boolean mHasInit;

    /**
     * 初始化 ZLog
     *
     * @param application 应用程序上下文
     * @param folderPath  日志文件保存路径，为空则使用默认路径
     */
    public static void init(Application application, String folderPath) {
        // Android 打印器
        AndroidPrinter androidPrinter = new AndroidPrinter(true);
        
        // 日志配置
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(TAG)
                .disableStackTrace()
                .build();

        // 确定日志文件保存路径
        String path = TextUtils.isEmpty(folderPath) ? 
                (application.getExternalFilesDir(null) != null ? 
                        application.getExternalFilesDir(null).getPath() : null) : folderPath;
        path = TextUtils.isEmpty(path) ? application.getFilesDir().getPath() : path;

        // 文件打印器
        FilePrinter filePrinter = new FilePrinter.Builder(path)
                .fileNameGenerator(new MyFileNameGenerator(application))
                .flattener(new MyFlattener(FLATTENER))
                .build();

        // 初始化 ZLog
        ZLog.init(config, androidPrinter, filePrinter);
        mHasInit = true;
        
        // 输出初始化成功信息
        ZLogWrapper.i("INIT", "ZLogWrapper 初始化成功，日志路径: " + path);
    }

    /**
     * VERBOSE 级别日志
     */
    public static void v(String tag, String msg) {
        if (mHasInit) {
            ZLog.v("[" + tag + "] " + msg);
            return;
        }
        Log.v(tag, msg);
    }

    /**
     * INFO 级别日志
     */
    public static void i(String tag, String msg) {
        if (mHasInit) {
            ZLog.i("[" + tag + "] " + msg);
            return;
        }
        Log.i(tag, msg);
    }

    /**
     * DEBUG 级别日志
     */
    public static void d(String tag, String msg) {
        if (mHasInit) {
            ZLog.d("[" + tag + "] " + msg);
            return;
        }
        Log.d(tag, msg);
    }

    /**
     * WARNING 级别日志
     */
    public static void w(String tag, String msg) {
        if (mHasInit) {
            ZLog.w("[" + tag + "] " + msg);
            return;
        }
        Log.w(tag, msg);
    }

    /**
     * WARNING 级别日志（带异常）
     */
    public static void w(String tag, String msg, Throwable throwable) {
        if (mHasInit) {
            ZLog.w("[" + tag + "] " + msg, throwable);
            return;
        }
        Log.w(tag, msg, throwable);
    }

    /**
     * WARNING 级别日志（仅异常）
     */
    public static void w(String tag, Throwable throwable) {
        if (mHasInit) {
            ZLog.w("[" + tag + "]", throwable);
            return;
        }
        Log.w(tag, "", throwable);
    }

    /**
     * ERROR 级别日志
     */
    public static void e(String tag, String msg) {
        if (mHasInit) {
            ZLog.e("[" + tag + "] " + msg);
            return;
        }
        Log.e(tag, msg);
    }

    /**
     * ERROR 级别日志（带异常）
     */
    public static void e(String tag, String msg, Throwable throwable) {
        if (mHasInit) {
            ZLog.e("[" + tag + "] " + msg, throwable);
            return;
        }
        Log.e(tag, msg, throwable);
    }

    /**
     * JSON 格式日志
     */
    public static void json(String tag, String msg) {
        if (mHasInit) {
            ZLog.json("[" + tag + "] " + msg);
            return;
        }
        Log.d(tag, msg);
    }

    /**
     * 自定义文件名生成器
     * 生成格式：TAG_v版本号_日期.log
     */
    static class MyFileNameGenerator implements FileNameGenerator {
        private final Context mCtx;

        public MyFileNameGenerator(Context context) {
            this.mCtx = context;
        }

        private final ThreadLocal<SimpleDateFormat> mLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {
            @NonNull
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            }
        };

        @Override
        public boolean isFileNameChangeable() {
            return true;
        }

        @Override
        public String generateFileName(int logLevel, long timestamp) {
            SimpleDateFormat sdf = mLocalDateFormat.get();
            if (sdf != null) {
                sdf.setTimeZone(TimeZone.getDefault());
                String dateStr = sdf.format(new Date(timestamp));
                return TAG + "_v" + getVerName() + "_" + dateStr + ".log";
            }
            return TAG + "_" + timestamp + ".log";
        }

        /**
         * 获取应用版本名
         */
        private String getVerName() {
            String verName = "1.0.0";
            try {
                verName = mCtx.getPackageManager()
                        .getPackageInfo(mCtx.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return verName;
        }
    }

    /**
     * 自定义格式化器
     * 使用指定的模式格式化日志
     */
    static class MyFlattener extends PatternFlattener {
        public MyFlattener(String pattern) {
            super(pattern);
        }
    }
}