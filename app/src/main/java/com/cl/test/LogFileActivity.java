package com.cl.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cl.zlog.LogConfiguration;
import com.cl.zlog.LogLevel;
import com.cl.zlog.Logger;
import com.cl.zlog.ZLog;
import com.cl.zlog.flattener.Flattener2;
import com.cl.zlog.formatter.border.BorderFormatter;
import com.cl.zlog.formatter.message.json.JsonFormatter;
import com.cl.zlog.formatter.message.object.ObjectFormatter;
import com.cl.zlog.formatter.message.xml.XmlFormatter;
import com.cl.zlog.printer.file.FilePrinter;
import com.cl.zlog.printer.file.backup.FileSizeBackupStrategy;
import com.cl.zlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.cl.zlog.printer.file.naming.ChangelessFileNameGenerator;
import com.cl.zlog.printer.file.naming.DateFileNameGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件日志演示Activity
 * 展示文件日志功能和自定义格式化器
 */
public class LogFileActivity extends AppCompatActivity {

    private TextView logFileContent;
    private Logger fileLogger;
    private String logFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file);

        initViews();
        initFileLogger();
        setupClickListeners();
    }

    private void initViews() {
        logFileContent = findViewById(R.id.log_file_content);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    /**
     * 初始化文件日志器
     */
    private void initFileLogger() {
        // 创建日志文件目录
        File logDir = new File(getExternalFilesDir(null), "demo_logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        // FilePrinter使用ChangelessFileNameGenerator("demo")会生成demo.log文件
        logFilePath = new File(logDir, "demo.log").getAbsolutePath();

        // 创建自定义配置
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag("FILE-DEMO")
                .enableThreadInfo()
                .enableStackTrace(1)
                .enableBorder()
                .jsonFormatter(new CustomJsonFormatter())
                .xmlFormatter(new CustomXmlFormatter())
                .borderFormatter(new CustomBorderFormatter())
                .build();

        // 创建文件打印器
        FilePrinter filePrinter = new FilePrinter
                .Builder(logDir.getAbsolutePath())
                .fileNameGenerator(new ChangelessFileNameGenerator("demo"))
                .backupStrategy(new FileSizeBackupStrategy(1024 * 10)) // 10KB备份
                .cleanStrategy(new FileLastModifiedCleanStrategy(24 * 60 * 60 * 1000L)) // 1天清理
                .flattener(new CustomFlattener())
                .build();

        // 创建文件日志器
        fileLogger = ZLog.tag("FILE-DEMO")
                .jsonFormatter(new CustomJsonFormatter())
                .xmlFormatter(new CustomXmlFormatter())
                .borderFormatter(new CustomBorderFormatter())
                .printers(filePrinter)
                .build();

        ZLog.i("文件日志器初始化完成，日志文件: %s", logFilePath);
    }

    private void setupClickListeners() {
        // 写入基本日志
        findViewById(R.id.btn_write_basic_log).setOnClickListener(v -> writeBasicLog());
        
        // 写入格式化日志
        findViewById(R.id.btn_write_formatted_log).setOnClickListener(v -> writeFormattedLog());
        
        // 写入JSON日志
        findViewById(R.id.btn_write_json_log).setOnClickListener(v -> writeJsonLog());
        
        // 写入XML日志
        findViewById(R.id.btn_write_xml_log).setOnClickListener(v -> writeXmlLog());
        
        // 写入异常日志
        findViewById(R.id.btn_write_exception_log).setOnClickListener(v -> writeExceptionLog());
        
        // 读取日志文件
        findViewById(R.id.btn_read_log_file).setOnClickListener(v -> readLogFile());
        
        // 清空日志文件
        findViewById(R.id.btn_clear_log_file).setOnClickListener(v -> clearLogFile());
    }

    /**
     * 写入基本日志
     */
    private void writeBasicLog() {
        fileLogger.v("这是 VERBOSE 级别的文件日志");
        fileLogger.d("这是 DEBUG 级别的文件日志");
        fileLogger.i("这是 INFO 级别的文件日志");
        fileLogger.w("这是 WARNING 级别的文件日志");
        fileLogger.e("这是 ERROR 级别的文件日志");
        
        showToast("基本日志已写入文件");
    }

    /**
     * 写入格式化日志
     */
    private void writeFormattedLog() {
        fileLogger.i("用户登录: 用户名=%s, 时间=%s", "admin", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        fileLogger.d("系统信息: Android版本=%d, 设备型号=%s", android.os.Build.VERSION.SDK_INT, android.os.Build.MODEL);
        fileLogger.w("内存警告: 可用内存=%.2f MB", Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0);
        
        showToast("格式化日志已写入文件");
    }

    /**
     * 写入JSON日志
     */
    private void writeJsonLog() {
        String jsonData = "{\"userId\":12345,\"userName\":\"张三\",\"action\":\"login\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"metadata\":{\"ip\":\"192.168.1.100\",\"userAgent\":\"Android App\"}}";
        fileLogger.json(jsonData);
        
        showToast("JSON日志已写入文件");
    }

    /**
     * 写入XML日志
     */
    private void writeXmlLog() {
        String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><request><method>POST</method><url>/api/user/login</url><headers><header name=\"Content-Type\">application/json</header></headers><body>{\"username\":\"admin\",\"password\":\"***\"}</body></request>";
        fileLogger.xml(xmlData);
        
        showToast("XML日志已写入文件");
    }

    /**
     * 写入异常日志
     */
    private void writeExceptionLog() {
        try {
            // 模拟网络异常
            throw new RuntimeException("网络连接超时");
        } catch (Exception e) {
            fileLogger.e("网络请求失败", e);
        }
        
        try {
            // 模拟数据解析异常
            throw new IllegalArgumentException("JSON解析失败: 格式不正确");
        } catch (Exception e) {
            fileLogger.e("数据处理异常: %s", e, e.getMessage());
        }
        
        showToast("异常日志已写入文件");
    }

    /**
     * 读取日志文件内容
     */
    private void readLogFile() {
        try {
            File logFile = new File(logFilePath);
            ZLog.d("尝试读取日志文件: %s", logFilePath);
            
            if (!logFile.exists()) {
                // 检查目录中是否有其他日志文件
                File logDir = logFile.getParentFile();
                ZLog.d("原文件不存在，检查目录: %s", logDir != null ? logDir.getAbsolutePath() : "null");
                
                if (logDir != null && logDir.exists()) {
                    File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
                    ZLog.d("目录中找到 %d 个.log文件", files != null ? files.length : 0);
                    
                    if (files != null && files.length > 0) {
                        // 使用找到的第一个日志文件
                        logFile = files[0];
                        logFilePath = logFile.getAbsolutePath();
                        ZLog.d("找到日志文件: %s", logFilePath);
                    } else {
                        // 列出目录中的所有文件进行调试
                        File[] allFiles = logDir.listFiles();
                        StringBuilder debugInfo = new StringBuilder();
                        debugInfo.append("日志文件不存在，请先写入一些日志\n");
                        debugInfo.append("目录: ").append(logDir.getAbsolutePath()).append("\n");
                        debugInfo.append("目录中的文件:\n");
                        if (allFiles != null) {
                            for (File f : allFiles) {
                                debugInfo.append("- ").append(f.getName()).append(" (").append(f.length()).append(" bytes)\n");
                            }
                        } else {
                            debugInfo.append("无法读取目录内容\n");
                        }
                        logFileContent.setText(debugInfo.toString());
                        return;
                    }
                } else {
                    logFileContent.setText("日志目录不存在，请先写入一些日志\n目录: " + (logDir != null ? logDir.getAbsolutePath() : "null"));
                    return;
                }
            }

            ZLog.d("开始读取文件: %s, 大小: %d bytes", logFile.getAbsolutePath(), logFile.length());

            StringBuilder content = new StringBuilder();
            content.append("日志文件: ").append(logFile.getName()).append("\n");
            content.append("文件大小: ").append(logFile.length()).append(" bytes\n");
            content.append("修改时间: ").append(new Date(logFile.lastModified())).append("\n");
            content.append("文件路径: ").append(logFile.getAbsolutePath()).append("\n\n");
            
            if (logFile.length() == 0) {
                content.append("文件为空，请先写入一些日志");
                logFileContent.setText(content.toString());
                showToast("文件为空");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            int lineCount = 0;
            
            content.append("=== 文件内容 ===\n");
            while ((line = reader.readLine()) != null && lineCount < 50) { // 只读取前50行
                content.append(String.format("[%d] %s\n", lineCount + 1, line));
                lineCount++;
            }
            reader.close();
            
            ZLog.d("读取完成，共 %d 行", lineCount);
            
            if (lineCount >= 50) {
                content.append("\n... (显示前50行，完整内容请查看文件)");
            } else if (lineCount == 0) {
                content.append("文件存在但内容为空，请先写入一些日志");
            }
            
            logFileContent.setText(content.toString());
            showToast("日志文件读取完成，共 " + lineCount + " 行");
            
        } catch (IOException e) {
            String errorMsg = "读取日志文件失败: " + e.getMessage();
            logFileContent.setText(errorMsg);
            ZLog.e("读取日志文件失败", e);
            showToast("读取失败: " + e.getMessage());
        }
    }

    /**
     * 清空日志文件
     */
    private void clearLogFile() {
        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            logFile.delete();
        }
        logFileContent.setText("日志文件已清空");
        showToast("日志文件已清空");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 自定义JSON格式化器
     */
    private static class CustomJsonFormatter implements JsonFormatter {
        @Override
        public String format(String json) {
            return "📄 JSON数据:\n" + json;
        }
    }

    /**
     * 自定义XML格式化器
     */
    private static class CustomXmlFormatter implements XmlFormatter {
        @Override
        public String format(String xml) {
            return "📋 XML数据:\n" + xml;
        }
    }

    /**
     * 自定义边框格式化器
     */
    private static class CustomBorderFormatter implements BorderFormatter {
        @Override
        public String format(String[] segments) {
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
            for (String segment : segments) {
                sb.append("║ ").append(segment).append("\n");
            }
            sb.append("╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            return sb.toString();
        }
    }

    /**
     * 自定义日志扁平化器
     */
    private static class CustomFlattener implements Flattener2 {
        @Override
        public CharSequence flatten(long timeMillis, int logLevel, String tag, String message) {
            String levelName = getLevelName(logLevel);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date(timeMillis));
            return String.format("[%s] %s/%s: %s", timestamp, levelName, tag, message);
        }

        private String getLevelName(int logLevel) {
            switch (logLevel) {
                case LogLevel.VERBOSE: return "V";
                case LogLevel.DEBUG: return "D";
                case LogLevel.INFO: return "I";
                case LogLevel.WARN: return "W";
                case LogLevel.ERROR: return "E";
                default: return "?";
            }
        }
    }
}