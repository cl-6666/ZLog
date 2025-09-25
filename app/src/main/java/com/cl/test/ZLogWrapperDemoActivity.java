package com.cl.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ZLogWrapper 功能演示 Activity
 * 展示 ZLogWrapper 的各种日志功能
 */
public class ZLogWrapperDemoActivity extends AppCompatActivity {

    private TextView logOutput;
    private ScrollView scrollView;
    private StringBuilder logBuffer = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zlog_wrapper_demo);

        initViews();
        setupClickListeners();
        
        // 应用启动日志
        ZLogWrapper.i("DEMO", "ZLogWrapper 演示页面启动");
    }

    private void initViews() {
        logOutput = findViewById(R.id.log_output);
        scrollView = findViewById(R.id.scroll_view);
    }

    private void setupClickListeners() {
        // 基本日志演示
        findViewById(R.id.btn_basic_logs).setOnClickListener(v -> demoBasicLogs());
        
        // 异常日志演示
        findViewById(R.id.btn_exception_logs).setOnClickListener(v -> demoExceptionLogs());
        
        // JSON 日志演示
        findViewById(R.id.btn_json_logs).setOnClickListener(v -> demoJsonLogs());
        
        // 不同标签日志演示
        findViewById(R.id.btn_tag_logs).setOnClickListener(v -> demoTagLogs());
        
        // 清空日志
        findViewById(R.id.btn_clear_logs).setOnClickListener(v -> clearLogs());
    }

    /**
     * 演示基本日志功能
     */
    private void demoBasicLogs() {
        appendLog("=== ZLogWrapper 基本日志演示 ===");
        
        ZLogWrapper.v("VERBOSE", "这是 VERBOSE 级别日志");
        ZLogWrapper.d("DEBUG", "这是 DEBUG 级别日志");
        ZLogWrapper.i("INFO", "这是 INFO 级别日志");
        ZLogWrapper.w("WARNING", "这是 WARNING 级别日志");
        ZLogWrapper.e("ERROR", "这是 ERROR 级别日志");
        
        appendLog("基本日志已输出到 Logcat 和文件");
        showToast("基本日志演示完成");
    }

    /**
     * 演示异常日志功能
     */
    private void demoExceptionLogs() {
        appendLog("=== ZLogWrapper 异常日志演示 ===");
        
        try {
            // 故意制造一个除零异常
            int result = 10 / 0;
        } catch (Exception e) {
            ZLogWrapper.e("EXCEPTION", "发生了除零异常", e);
        }
        
        try {
            // 故意制造空指针异常
            String str = null;
            int length = str.length();
        } catch (Exception e) {
            ZLogWrapper.w("EXCEPTION", "发生了空指针异常", e);
        }
        
        // 仅异常信息
        try {
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            ZLogWrapper.w("EXCEPTION", e);
        }
        
        appendLog("异常日志已输出到 Logcat 和文件");
        showToast("异常日志演示完成");
    }

    /**
     * 演示 JSON 日志功能
     */
    private void demoJsonLogs() {
        appendLog("=== ZLogWrapper JSON 日志演示 ===");
        
        String jsonString = "{\"name\":\"张三\",\"age\":25,\"skills\":[\"Java\",\"Android\",\"Kotlin\"],\"address\":{\"city\":\"北京\",\"district\":\"朝阳区\"}}";
        ZLogWrapper.json("JSON", jsonString);
        
        String complexJson = "{\"users\":[{\"id\":1,\"name\":\"用户1\"},{\"id\":2,\"name\":\"用户2\"}],\"total\":2,\"success\":true}";
        ZLogWrapper.json("JSON", complexJson);
        
        appendLog("JSON 日志已格式化输出到 Logcat 和文件");
        showToast("JSON 日志演示完成");
    }

    /**
     * 演示不同标签日志功能
     */
    private void demoTagLogs() {
        appendLog("=== ZLogWrapper 标签日志演示 ===");
        
        // 网络相关日志
        ZLogWrapper.d("NETWORK", "开始网络请求");
        ZLogWrapper.i("NETWORK", "网络请求成功");
        ZLogWrapper.w("NETWORK", "网络请求超时");
        
        // 数据库相关日志
        ZLogWrapper.d("DATABASE", "开始数据库查询");
        ZLogWrapper.i("DATABASE", "查询到 10 条记录");
        ZLogWrapper.e("DATABASE", "数据库连接失败");
        
        // UI 相关日志
        ZLogWrapper.d("UI", "界面开始渲染");
        ZLogWrapper.i("UI", "界面渲染完成");
        ZLogWrapper.v("UI", "用户点击按钮");
        
        // 业务逻辑日志
        ZLogWrapper.i("BUSINESS", "用户登录成功");
        ZLogWrapper.w("BUSINESS", "用户权限不足");
        ZLogWrapper.e("BUSINESS", "业务处理失败");
        
        appendLog("不同标签日志已输出到 Logcat 和文件");
        showToast("标签日志演示完成");
    }

    /**
     * 清空日志显示
     */
    private void clearLogs() {
        logBuffer.setLength(0);
        logOutput.setText("");
        ZLogWrapper.i("DEMO", "日志显示已清空");
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