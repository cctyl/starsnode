package io.github.cctyl.starnode.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.utils.ConfigManager;

public class SettingsActivity extends AppCompatActivity {

    private EditText etWebSocketUrl;
    private EditText etToken;
    private EditText etDeviceName;
    private Button btnSave;
    private Button btnReset;
    
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        configManager = new ConfigManager(this);
        
        initViews();
        setupToolbar();
        loadCurrentConfig();
        setupClickListeners();
    }

    private void initViews() {
        etWebSocketUrl = findViewById(R.id.etWebSocketUrl);
        etToken = findViewById(R.id.etToken);
        etDeviceName = findViewById(R.id.etDeviceName);
        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }

    private void loadCurrentConfig() {
        etWebSocketUrl.setText(configManager.getWebSocketUrl());
        etToken.setText(configManager.getToken());
        etDeviceName.setText(configManager.getDeviceName());
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveConfig());
        btnReset.setOnClickListener(v -> resetToDefaults());
    }

    private void saveConfig() {
        String wsUrl = etWebSocketUrl.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String deviceName = etDeviceName.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(wsUrl)) {
            etWebSocketUrl.setError("请输入WebSocket地址");
            etWebSocketUrl.requestFocus();
            return;
        }

        if (!isValidWebSocketUrl(wsUrl)) {
            etWebSocketUrl.setError("请输入有效的WebSocket地址（以ws://或wss://开头）");
            etWebSocketUrl.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(token)) {
            etToken.setError("请输入Token");
            etToken.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(deviceName)) {
            etDeviceName.setError("请输入设备名称");
            etDeviceName.requestFocus();
            return;
        }

        // 保存配置
        configManager.saveWebSocketUrl(wsUrl);
        configManager.saveToken(token);
        configManager.saveDeviceName(deviceName);

        Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show();
        
        // 返回主界面
        setResult(RESULT_OK);
        finish();
    }

    private void resetToDefaults() {
        etWebSocketUrl.setText(ConfigManager.DEFAULT_WEBSOCKET_URL);
        etToken.setText(ConfigManager.DEFAULT_TOKEN);
        etDeviceName.setText(ConfigManager.DEFAULT_DEVICE_NAME);
        
        Toast.makeText(this, "已重置为默认值", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidWebSocketUrl(String url) {
        return url.toLowerCase().startsWith("ws://") || url.toLowerCase().startsWith("wss://");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}