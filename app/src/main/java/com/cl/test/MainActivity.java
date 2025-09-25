package com.cl.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cl.zlog.Logger;
import com.cl.zlog.ZLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MainActivity - ZLog 功能演示
 * 展示各种日志功能，比原版更好用的特性
 */
public class MainActivity extends AppCompatActivity {

    private TextView logOutput;
    private ScrollView scrollView;
    private StringBuilder logBuffer = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        
        // 应用启动日志
        ZLog.i("MainActivity 启动完成");
    }

    private void initViews() {
        logOutput = findViewById(R.id.log_output);
        scrollView = findViewById(R.id.scroll_view);
    }

    private void setupClickListeners() {
        // 基本日志演示
        findViewById(R.id.btn_basic_logs).setOnClickListener(v -> demoBasicLogs());
        
        // 格式化日志演示
        findViewById(R.id.btn_formatted_logs).setOnClickListener(v -> demoFormattedLogs());
        
        // 对象日志演示
        findViewById(R.id.btn_object_logs).setOnClickListener(v -> demoObjectLogs());
        
        // JSON/XML 日志演示
        findViewById(R.id.btn_json_xml_logs).setOnClickListener(v -> demoJsonXmlLogs());
        
        // 异常日志演示
        findViewById(R.id.btn_exception_logs).setOnClickListener(v -> demoExceptionLogs());
        
        // 自定义标签日志演示
        findViewById(R.id.btn_custom_tag_logs).setOnClickListener(v -> demoCustomTagLogs());
        
        // 线程和堆栈信息演示
        findViewById(R.id.btn_thread_stack_logs).setOnClickListener(v -> demoThreadStackLogs());
        
        // 清空日志
        findViewById(R.id.btn_clear_logs).setOnClickListener(v -> clearLogs());
        
        // 文件日志演示
        findViewById(R.id.btn_file_log_demo).setOnClickListener(v -> {
            Intent intent = new Intent(this, LogFileActivity.class);
            startActivity(intent);
        });

        // 自定义格式化器演示
        findViewById(R.id.btn_custom_formatter_demo).setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomFormatterActivity.class);
            startActivity(intent);
        });

        // ZLogWrapper 演示
        findViewById(R.id.btn_zlog_wrapper_demo).setOnClickListener(v -> {
            Intent intent = new Intent(this, ZLogWrapperDemoActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 演示基本日志功能
     */
    private void demoBasicLogs() {
        appendLog("=== 基本日志演示 ===");
        
        ZLog.v("这是 VERBOSE 级别日志");
        ZLog.d("这是 DEBUG 级别日志");
        ZLog.i("这是 INFO 级别日志");
        ZLog.w("这是 WARNING 级别日志");
        ZLog.e("这是 ERROR 级别日志");
        
        appendLog("基本日志已输出到 Logcat");
        showToast("基本日志演示完成");
    }

    /**
     * 演示格式化日志功能
     */
    private void demoFormattedLogs() {
        appendLog("=== 格式化日志演示 ===");
        
        String name = "张三";
        int age = 25;
        double score = 98.5;
        
        ZLog.d("用户信息: 姓名=%s, 年龄=%d, 分数=%.1f", name, age, score);
        ZLog.i("当前时间: %tF %tT", System.currentTimeMillis(), System.currentTimeMillis());
        ZLog.w("内存使用: %.2f MB", Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0);
        
        appendLog("格式化日志已输出到 Logcat");
        showToast("格式化日志演示完成");
    }

    /**
     * 演示对象日志功能
     */
    private void demoObjectLogs() {
        appendLog("=== 对象日志演示 ===");
        
        // 自定义对象
        ZLogApplication.User user = new ZLogApplication.User("李四", 30, "lisi@example.com");
        ZLog.d("用户对象: %s", user);
        
        // 数组
        int[] numbers = {1, 2, 3, 4, 5};
        ZLog.d("数组: %s", numbers);
        
        // List
        List<String> fruits = new ArrayList<>();
        fruits.add("苹果");
        fruits.add("香蕉");
        fruits.add("橙子");
        ZLog.d("水果列表: %s", fruits);
        
        // Map
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", "王五");
        userInfo.put("age", 28);
        userInfo.put("married", true);
        ZLog.d("用户信息Map: %s", userInfo);
        
        // Intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("key", "value");
        ZLog.d("Intent对象: %s", intent);
        
        appendLog("对象日志已输出到 Logcat");
        showToast("对象日志演示完成");
    }

    /**
     * 演示 JSON/XML 日志功能
     */
    private void demoJsonXmlLogs() {
        appendLog("=== JSON/XML 日志演示 ===");
        
        // JSON 字符串
        String jsonString = "{\"name\":\"赵六\",\"age\":35,\"skills\":[\"Java\",\"Android\",\"Kotlin\"],\"address\":{\"city\":\"北京\",\"district\":\"朝阳区\"}}";
        ZLog.json(jsonString);
        
        // XML 字符串
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><user><name>钱七</name><age>40</age><city>上海</city></user>";
        ZLog.xml(xmlString);
        
        appendLog("JSON/XML 日志已格式化输出到 Logcat");
        showToast("JSON/XML 日志演示完成");
    }

    /**
     * 演示异常日志功能
     */
    private void demoExceptionLogs() {
        appendLog("=== 异常日志演示 ===");
        
        try {
            // 故意制造一个异常
            int result = 10 / 0;
        } catch (Exception e) {
            ZLog.e("发生了除零异常", e);
        }
        
        try {
            // 故意制造空指针异常
            String str = null;
            int length = str.length();
        } catch (Exception e) {
            ZLog.e("发生了空指针异常: %s", e, e.getMessage());
        }
        
        appendLog("异常日志已输出到 Logcat");
        showToast("异常日志演示完成");
    }

    /**
     * 演示自定义标签日志功能
     */
    private void demoCustomTagLogs() {
        appendLog("=== 自定义标签日志演示 ===");
        
        // 使用自定义标签
        Logger networkLogger = ZLog.tag("NETWORK").build();
        networkLogger.d("开始网络请求");
        networkLogger.i("网络请求成功");
        
        Logger databaseLogger = ZLog.tag("DATABASE").build();
        databaseLogger.d("开始数据库查询");
        databaseLogger.w("数据库连接超时");
        
        Logger uiLogger = ZLog.tag("UI").build();
        uiLogger.d("界面渲染开始");
        uiLogger.i("界面渲染完成");
        
        appendLog("自定义标签日志已输出到 Logcat");
        showToast("自定义标签日志演示完成");
    }

    /**
     * 演示线程和堆栈信息日志功能
     */
    private void demoThreadStackLogs() {
        appendLog("=== 线程和堆栈信息演示 ===");
        
        // 主线程日志
        Logger threadLogger = ZLog.enableThreadInfo().enableBorder().build();
        threadLogger.d("主线程日志信息");
        
        // 子线程日志
        new Thread(() -> {
            Logger subThreadLogger = ZLog.tag("SUB-THREAD").enableThreadInfo().enableStackTrace(3).build();
            subThreadLogger.i("子线程日志信息");
            subThreadLogger.d("包含堆栈跟踪的日志");
        }).start();
        
        appendLog("线程和堆栈信息日志已输出到 Logcat");
        showToast("线程和堆栈信息演示完成");
    }

    /**
     * 清空日志显示
     */
    private void clearLogs() {
        logBuffer.setLength(0);
        logOutput.setText("");
        ZLog.i("日志显示已清空");
    }

    /**
     * 添加日志到界面显示
     */
    private void appendLog(String message) {
        logBuffer.append(message).append("\n");
        logOutput.setText(logBuffer.toString());
        
        // 滚动到底部
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * 显示 Toast 消息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}