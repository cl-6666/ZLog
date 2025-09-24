package com.cl.test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cl.zlog.LogConfiguration;
import com.cl.zlog.LogLevel;
import com.cl.zlog.Logger;
import com.cl.zlog.ZLog;
import com.cl.zlog.formatter.border.BorderFormatter;
import com.cl.zlog.formatter.message.json.JsonFormatter;
import com.cl.zlog.formatter.message.object.ObjectFormatter;
import com.cl.zlog.formatter.stacktrace.StackTraceFormatter;
import com.cl.zlog.formatter.thread.ThreadFormatter;
import com.cl.zlog.formatter.message.xml.XmlFormatter;
import com.cl.zlog.printer.AndroidPrinter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 自定义格式化器演示Activity
 * 展示各种自定义格式化器的使用方法
 */
public class CustomFormatterActivity extends AppCompatActivity {

    private TextView logOutput;
    private Logger customLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_formatter);

        initViews();
        initCustomLogger();
        setupClickListeners();
    }

    private void initViews() {
        logOutput = findViewById(R.id.log_output);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    /**
     * 初始化自定义格式化器日志器
     */
    private void initCustomLogger() {
        customLogger = ZLog.tag("CUSTOM-FORMATTER")
                .jsonFormatter(new ColorfulJsonFormatter())
                .xmlFormatter(new ColorfulXmlFormatter())
                .borderFormatter(new FancyBorderFormatter())
                .threadFormatter(new DetailedThreadFormatter())
                .stackTraceFormatter(new CompactStackTraceFormatter())
                .addObjectFormatter(User.class, new UserFormatter())
                .addObjectFormatter(ApiResponse.class, new ApiResponseFormatter())
                .printers(new AndroidPrinter())
                .build();

        ZLog.i("自定义格式化器日志器初始化完成");
    }

    private void setupClickListeners() {
        // 彩色JSON格式化
        findViewById(R.id.btn_colorful_json).setOnClickListener(v -> demonstrateColorfulJson());
        
        // 彩色XML格式化
        findViewById(R.id.btn_colorful_xml).setOnClickListener(v -> demonstrateColorfulXml());
        
        // 花式边框格式化
        findViewById(R.id.btn_fancy_border).setOnClickListener(v -> demonstrateFancyBorder());
        
        // 详细线程信息格式化
        findViewById(R.id.btn_detailed_thread).setOnClickListener(v -> demonstrateDetailedThread());
        
        // 紧凑堆栈跟踪格式化
        findViewById(R.id.btn_compact_stacktrace).setOnClickListener(v -> demonstrateCompactStackTrace());
        
        // 自定义对象格式化
        findViewById(R.id.btn_custom_object).setOnClickListener(v -> demonstrateCustomObject());
        
        // 清空日志
        findViewById(R.id.btn_clear_logs).setOnClickListener(v -> clearLogs());
    }

    /**
     * 演示彩色JSON格式化
     */
    private void demonstrateColorfulJson() {
        String jsonData = "{\"userId\":12345,\"userName\":\"张三\",\"email\":\"zhangsan@example.com\",\"roles\":[\"admin\",\"user\"],\"profile\":{\"age\":28,\"city\":\"北京\",\"isActive\":true},\"lastLogin\":\"2024-01-15T10:30:00Z\"}";
        customLogger.json(jsonData);
        appendToOutput("✨ 彩色JSON格式化演示完成");
        showToast("彩色JSON格式化演示完成");
    }

    /**
     * 演示彩色XML格式化
     */
    private void demonstrateColorfulXml() {
        String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><user><id>12345</id><name>张三</name><email>zhangsan@example.com</email><profile><age>28</age><city>北京</city><isActive>true</isActive></profile><roles><role>admin</role><role>user</role></roles></user>";
        customLogger.xml(xmlData);
        appendToOutput("✨ 彩色XML格式化演示完成");
        showToast("彩色XML格式化演示完成");
    }

    /**
     * 演示花式边框格式化
     */
    private void demonstrateFancyBorder() {
        customLogger.i("这是一条带有花式边框的日志消息");
        customLogger.w("警告：这是一条警告消息，使用了自定义边框格式化器");
        customLogger.e("错误：这是一条错误消息，展示了花式边框的效果");
        appendToOutput("✨ 花式边框格式化演示完成");
        showToast("花式边框格式化演示完成");
    }

    /**
     * 演示详细线程信息格式化
     */
    private void demonstrateDetailedThread() {
        // 在主线程中记录日志
        customLogger.i("主线程日志消息");
        
        // 在后台线程中记录日志
        new Thread(() -> {
            customLogger.d("后台线程日志消息");
        }, "BackgroundWorker").start();
        
        // 在另一个后台线程中记录日志
        new Thread(() -> {
            customLogger.w("数据处理线程日志消息");
        }, "DataProcessor").start();
        
        appendToOutput("✨ 详细线程信息格式化演示完成");
        showToast("详细线程信息格式化演示完成");
    }

    /**
     * 演示紧凑堆栈跟踪格式化
     */
    private void demonstrateCompactStackTrace() {
        try {
            // 模拟嵌套异常
            methodA();
        } catch (Exception e) {
            customLogger.e("捕获到异常，使用紧凑堆栈跟踪格式化", e);
        }
        appendToOutput("✨ 紧凑堆栈跟踪格式化演示完成");
        showToast("紧凑堆栈跟踪格式化演示完成");
    }

    private void methodA() throws Exception {
        methodB();
    }

    private void methodB() throws Exception {
        methodC();
    }

    private void methodC() throws Exception {
        throw new RuntimeException("这是一个模拟异常，用于演示紧凑堆栈跟踪格式化");
    }

    /**
     * 演示自定义对象格式化
     */
    private void demonstrateCustomObject() {
        User user = new User(12345, "张三", "zhangsan@example.com", 28, "北京");
        customLogger.d("用户对象: %s", user);

        ApiResponse<String> response = new ApiResponse<>(200, "success", "操作成功", "数据内容");
        customLogger.i("API响应对象: %s", response);

        appendToOutput("✨ 自定义对象格式化演示完成");
        showToast("自定义对象格式化演示完成");
    }

    private void clearLogs() {
        logOutput.setText("");
        showToast("日志显示已清空");
    }

    private void appendToOutput(String message) {
        runOnUiThread(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String logMessage = String.format("[%s] %s\n", timestamp, message);
            logOutput.append(logMessage);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ==================== 自定义格式化器实现 ====================

    /**
     * 彩色JSON格式化器
     */
    private static class ColorfulJsonFormatter implements JsonFormatter {
        @Override
        public String format(String json) {
            return "🎨 JSON数据 (彩色格式化):\n" + 
                   "┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n" +
                   "│ " + json.replace(",", ",\n│ ").replace("{", "{\n│   ").replace("}", "\n│ }") + "\n" +
                   "└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────";
        }
    }

    /**
     * 彩色XML格式化器
     */
    private static class ColorfulXmlFormatter implements XmlFormatter {
        @Override
        public String format(String xml) {
            return "🎨 XML数据 (彩色格式化):\n" + 
                   "┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n" +
                   "│ " + xml.replace("><", ">\n│ <") + "\n" +
                   "└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────";
        }
    }

    /**
     * 花式边框格式化器
     */
    private static class FancyBorderFormatter implements BorderFormatter {
        @Override
        public String format(String[] segments) {
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
            for (int i = 0; i < segments.length; i++) {
                if (i == 0) {
                    sb.append("║ 🚀 ").append(segments[i]).append("\n");
                } else {
                    sb.append("║ 📝 ").append(segments[i]).append("\n");
                }
            }
            sb.append("╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            return sb.toString();
        }
    }

    /**
     * 详细线程信息格式化器
     */
    private static class DetailedThreadFormatter implements ThreadFormatter {
        @Override
        public String format(Thread thread) {
            return String.format("🧵 线程信息 [ID:%d | 名称:%s | 优先级:%d | 状态:%s | 组:%s]", 
                    thread.getId(), 
                    thread.getName(), 
                    thread.getPriority(),
                    thread.getState().name(),
                    thread.getThreadGroup() != null ? thread.getThreadGroup().getName() : "null");
        }
    }

    /**
     * 紧凑堆栈跟踪格式化器
     */
    private static class CompactStackTraceFormatter implements StackTraceFormatter {
        @Override
        public String format(StackTraceElement[] stackTrace) {
            if (stackTrace == null || stackTrace.length == 0) {
                return "📍 堆栈跟踪: 无";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("📍 堆栈跟踪 (紧凑格式):\n");
            
            for (int i = 0; i < Math.min(stackTrace.length, 5); i++) { // 只显示前5层
                StackTraceElement element = stackTrace[i];
                sb.append("   ").append(i + 1).append(". ")
                  .append(element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1))
                  .append(".")
                  .append(element.getMethodName())
                  .append("(")
                  .append(element.getFileName())
                  .append(":")
                  .append(element.getLineNumber())
                  .append(")");
                if (i < Math.min(stackTrace.length, 5) - 1) {
                    sb.append("\n");
                }
            }
            
            if (stackTrace.length > 5) {
                sb.append("\n   ... 还有 ").append(stackTrace.length - 5).append(" 层堆栈");
            }
            
            return sb.toString();
        }
    }

    /**
     * 用户对象格式化器
     */
    private static class UserFormatter implements ObjectFormatter<User> {
        @Override
        public String format(User user) {
            return String.format("👤 用户信息 [ID:%d | 姓名:%s | 邮箱:%s | 年龄:%d | 城市:%s]", 
                    user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getCity());
        }
    }

    /**
     * API响应对象格式化器
     */
    private static class ApiResponseFormatter implements ObjectFormatter<ApiResponse> {
        @Override
        public String format(ApiResponse response) {
            return String.format("🌐 API响应 [状态码:%d | 状态:%s | 消息:%s | 数据:%s]", 
                    response.getCode(), response.getStatus(), response.getMessage(), response.getData());
        }
    }

    // ==================== 数据模型类 ====================

    /**
     * 用户数据模型
     */
    public static class User {
        private int id;
        private String name;
        private String email;
        private int age;
        private String city;

        public User(int id, String name, String email, int age, String city) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
            this.city = city;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getAge() { return age; }
        public String getCity() { return city; }
    }

    /**
     * API响应数据模型
     */
    public static class ApiResponse<T> {
        private int code;
        private String status;
        private String message;
        private T data;

        public ApiResponse(int code, String status, String message, T data) {
            this.code = code;
            this.status = status;
            this.message = message;
            this.data = data;
        }

        // Getters
        public int getCode() { return code; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}