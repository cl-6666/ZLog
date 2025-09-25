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
     * 初始化 ZLog 日志框架
     * 提供两种初始化方式：
     * 1. 传统的详细配置方式（推荐用于生产环境）
     * 2. 简化的 ZLogWrapper 方式（推荐用于快速开发）
     */
    private void initZLog() {
        // 方式一：传统的详细配置方式（保留原有功能）
        initZLogTraditional();
        
        // 方式二：使用 ZLogWrapper 进行简化初始化（新增功能）
        // 注释掉下面这行可以只使用传统方式
        // initZLogWithWrapper();
        
        // 演示日志输出
        ZLog.d("APPLICATION", "应用程序启动完成 - 使用传统配置方式");
    }

    /**
     * 传统的详细配置方式初始化 ZLog
     * 提供完整的自定义配置能力
     */
    private void initZLogTraditional() {
        // 创建日志配置
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)                     // 设置日志级别
                .tag("ZLog-Demo")                           // 设置全局标签
                .enableThreadInfo()                         // 启用线程信息
                .enableStackTrace(2)                        // 启用堆栈跟踪，深度为2
                .enableBorder()                             // 启用边框
                .jsonFormatter(new com.cl.zlog.formatter.message.json.DefaultJsonFormatter())
                .xmlFormatter(new com.cl.zlog.formatter.message.xml.DefaultXmlFormatter())
                .throwableFormatter(new com.cl.zlog.formatter.message.throwable.DefaultThrowableFormatter())
                .threadFormatter(new DefaultThreadFormatter())
                .borderFormatter(new DefaultBorderFormatter())
                .addObjectFormatter(User.class, new UserObjectFormatter())
                .addInterceptor(new BlacklistTagsFilterInterceptor("SENSITIVE"))
                .build();

        // 创建打印器
        AndroidPrinter androidPrinter = new AndroidPrinter(true);
        ConsolePrinter consolePrinter = new ConsolePrinter();
        
        // 文件打印器配置
        FilePrinter filePrinter = new FilePrinter.Builder(getLogDir())
                .fileNameGenerator(new DateFileNameGenerator())
                .backupStrategy(new NeverBackupStrategy())
                .cleanStrategy(new FileLastModifiedCleanStrategy(7 * 24 * 60 * 60 * 1000L)) // 7天
                .flattener(new com.cl.zlog.flattener.DefaultFlattener())
                .build();

        // 初始化 ZLog
        ZLog.init(config, androidPrinter, consolePrinter, filePrinter);
    }

    /**
     * 使用 ZLogWrapper 进行简化初始化
     * 适合快速开发和简单场景
     */
    private void initZLogWithWrapper() {
        // 使用 ZLogWrapper 进行初始化
        // 传入自定义日志文件路径，如果为 null 则使用默认路径
        ZLogWrapper.init(this, getLogDir());
        
        // 演示 ZLogWrapper 的使用
        ZLogWrapper.d("APPLICATION", "应用程序启动完成 - 使用 ZLogWrapper");
        ZLogWrapper.i("APPLICATION", "使用 ZLogWrapper 进行日志管理");
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