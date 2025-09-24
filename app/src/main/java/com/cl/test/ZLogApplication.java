package com.cl.test;

import android.app.Application;
import android.os.Environment;

import com.cl.zlog.LogConfiguration;
import com.cl.zlog.LogLevel;
import com.cl.zlog.ZLog;
import com.cl.zlog.formatter.border.DefaultBorderFormatter;
import com.cl.zlog.formatter.message.object.ObjectFormatter;
import com.cl.zlog.formatter.stacktrace.DefaultStackTraceFormatter;
import com.cl.zlog.formatter.thread.DefaultThreadFormatter;
import com.cl.zlog.interceptor.BlacklistTagsFilterInterceptor;
import com.cl.zlog.printer.AndroidPrinter;
import com.cl.zlog.printer.ConsolePrinter;
import com.cl.zlog.printer.file.FilePrinter;
import com.cl.zlog.printer.file.backup.NeverBackupStrategy;
import com.cl.zlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.cl.zlog.printer.file.naming.DateFileNameGenerator;

import java.io.File;

/**
 * ZLog 应用程序初始化类
 * 演示如何配置和初始化 ZLog 日志框架
 */
public class ZLogApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initZLog();
    }

    /**
     * 初始化 ZLog 配置
     * 包含高级配置选项，比原版更好用的功能
     */
    private void initZLog() {
        // 创建高级日志配置
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)                                    // 设置日志级别
                .tag("ZLog-Demo")                                          // 设置全局标签
                .enableThreadInfo()                                        // 启用线程信息
                .enableStackTrace(2)                                       // 启用堆栈跟踪，深度为2
                .enableBorder()                                            // 启用边框美化
                .borderFormatter(new DefaultBorderFormatter())             // 边框格式化器
                .threadFormatter(new DefaultThreadFormatter())             // 线程格式化器
                .stackTraceFormatter(new DefaultStackTraceFormatter())     // 堆栈跟踪格式化器
                .addObjectFormatter(User.class, new UserObjectFormatter()) // 自定义对象格式化器
                .addInterceptor(new BlacklistTagsFilterInterceptor(        // 黑名单标签过滤器
                        "SENSITIVE", "PASSWORD", "TOKEN"))
                .build();

        // Android 控制台打印器
        AndroidPrinter androidPrinter = new AndroidPrinter(true);

        // 控制台打印器（用于调试）
        ConsolePrinter consolePrinter = new ConsolePrinter();

        // 文件打印器 - 保存日志到文件
        FilePrinter filePrinter = new FilePrinter
                .Builder(getLogDir())                                      // 日志文件目录
                .fileNameGenerator(new DateFileNameGenerator())            // 按日期生成文件名
                .backupStrategy(new NeverBackupStrategy())                 // 不备份策略
                .cleanStrategy(new FileLastModifiedCleanStrategy(          // 清理策略：7天
                        7 * 24 * 60 * 60 * 1000L))
                .build();

        // 初始化 ZLog
        ZLog.init(config, androidPrinter, consolePrinter, filePrinter);

        // 打印初始化成功信息
        ZLog.i("ZLog 初始化成功！");
        ZLog.d("日志文件保存路径: %s", getLogDir());
    }

    /**
     * 获取日志文件保存目录
     */
    private String getLogDir() {
        File logDir = new File(getExternalFilesDir(null), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return logDir.getAbsolutePath();
    }

    /**
     * 用户对象示例类
     */
    public static class User {
        private String name;
        private int age;
        private String email;

        public User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        // Getters
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
    }

    /**
     * 自定义用户对象格式化器
     */
    public static class UserObjectFormatter implements ObjectFormatter<User> {
        @Override
        public String format(User user) {
            return String.format("User{name='%s', age=%d, email='%s'}", 
                    user.getName(), user.getAge(), user.getEmail());
        }
    }
}