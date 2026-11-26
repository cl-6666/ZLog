package com.cl.test;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.cl.test.databinding.ActivityLogSoakBinding;
import com.cl.test.databinding.PanelLogSoakControlsBinding;
import com.cl.test.databinding.PanelLogSoakResultsBinding;

import java.io.File;

public class LogSoakActivity extends AppCompatActivity {
    private static final int[] RATES = {1, 5, 10};
    private static LogSoakRun currentRun;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            render();
            if (currentRun != null && !currentRun.isFinished()) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private ActivityLogSoakBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogSoakBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PanelLogSoakControlsBinding controls = binding.controls;
        ArrayAdapter<CharSequence> rates = ArrayAdapter.createFromResource(this,
                R.array.log_soak_rates, android.R.layout.simple_spinner_item);
        rates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        controls.rate.setAdapter(rates);
        if (currentRun != null) {
            for (int i = 0; i < RATES.length; i++) {
                if (RATES[i] == currentRun.rate()) {
                    controls.rate.setSelection(i);
                    break;
                }
            }
        }

        controls.startTest.setOnClickListener(v -> {
            if (currentRun != null && !currentRun.isFinished()) {
                return;
            }
            File externalRoot = getExternalFilesDir(null);
            if (externalRoot == null
                    || !Environment.MEDIA_MOUNTED.equals(
                            Environment.getExternalStorageState(externalRoot))) {
                controls.status.setText(R.string.log_soak_external_unavailable);
                return;
            }
            int rate = RATES[Math.max(0, controls.rate.getSelectedItemPosition())];
            currentRun = LogSoakRun.start(new File(getNoBackupFilesDir(), "log-soak"),
                    new File(externalRoot, "log-soak"), rate);
            refreshNow();
        });
        controls.stopTest.setOnClickListener(v -> {
            if (currentRun != null && !currentRun.isFinished()) {
                currentRun.stop(LogSoakRun.StopReason.USER);
                refreshNow();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshNow();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(refresh);
        if (!isChangingConfigurations() && currentRun != null && !currentRun.isFinished()) {
            currentRun.stop(LogSoakRun.StopReason.PAGE_CLOSED);
        }
        super.onStop();
    }

    private void refreshNow() {
        handler.removeCallbacks(refresh);
        refresh.run();
    }

    private void render() {
        LogSoakRun run = currentRun;
        boolean finished = run != null && run.isFinished();
        boolean active = run != null && !finished;
        binding.getRoot().setKeepScreenOn(active);
        PanelLogSoakControlsBinding controls = binding.controls;
        controls.startTest.setEnabled(!active);
        controls.rate.setEnabled(!active);
        controls.stopTest.setEnabled(active && run.stopReason() == null);
        if (run == null) {
            renderResults(0, LogSequenceVerifier.Report.EMPTY, LogSequenceVerifier.Report.EMPTY,
                    false, 0);
            return;
        }

        long submitted = run.submitted();
        LogSequenceVerifier.Report internal = run.internal.report;
        LogSequenceVerifier.Report external = run.external.report;
        boolean passed = finished && internal.isComplete(submitted) && external.isComplete(submitted);
        controls.status.setText(status(run, finished, passed));
        controls.elapsedTime.setText(DateUtils.formatElapsedTime(run.elapsedMillis() / 1000));
        renderResults(submitted, internal, external, finished, run.p99Micros());
        String paths = getString(R.string.log_soak_paths, run.internal.file.getAbsolutePath(),
                run.external.file.getAbsolutePath());
        if (!paths.contentEquals(binding.results.logPath.getText())) {
            binding.results.logPath.setText(paths);
            binding.results.logPath.setVisibility(View.VISIBLE);
        }
    }

    private void renderResults(long submitted, LogSequenceVerifier.Report internal,
                               LogSequenceVerifier.Report external, boolean finished, long p99Micros) {
        PanelLogSoakResultsBinding results = binding.results;
        String latency = p99Micros == 0 ? getString(R.string.log_soak_metric_unavailable)
                : getString(R.string.log_soak_latency_value, p99Micros);
        String internalLabel = getString(R.string.log_soak_internal);
        String externalLabel = getString(R.string.log_soak_external);
        results.submitted.setText(getString(R.string.log_soak_count, submitted));
        results.internalVerified.setText(getString(R.string.log_soak_verified_value,
                internalLabel, getString(R.string.log_soak_count, internal.unique)));
        results.externalVerified.setText(getString(R.string.log_soak_verified_value,
                externalLabel, getString(R.string.log_soak_count, external.unique)));
        results.metrics.setText(getString(R.string.log_soak_metrics,
                storageMetrics(internalLabel, submitted, internal, finished),
                storageMetrics(externalLabel, submitted, external, finished), latency));
    }

    private String storageMetrics(String label, long submitted, LogSequenceVerifier.Report report,
                                  boolean finished) {
        long pending = finished ? 0 : Math.max(0, submitted - report.highestSequence);
        return getString(R.string.log_soak_storage_metrics, label,
                report.missing(submitted, finished), pending, report.duplicates, report.outOfOrder,
                report.invalid, Formatter.formatShortFileSize(this, report.bytes));
    }

    private String status(LogSoakRun run, boolean finished, boolean passed) {
        LogSoakRun.StopReason stop = run.stopReason();
        if (finished) {
            String reason = run.failure() == null ? getString(stopLabel(stop))
                    : getString(R.string.log_soak_error_detail, run.failure());
            return getString(R.string.log_soak_result, reason,
                    getString(passed && run.failure() == null
                            ? R.string.log_soak_passed : R.string.log_soak_failed));
        }
        String label = getString(stop == null ? R.string.log_soak_running : R.string.log_soak_stopping);
        long waiting = run.waitingMillis();
        return waiting >= 1000 ? getString(R.string.log_soak_call_pending, label, waiting) : label;
    }

    private int stopLabel(LogSoakRun.StopReason reason) {
        if (reason == null) {
            return R.string.log_soak_error;
        }
        switch (reason) {
            case USER: return R.string.log_soak_stopped_by_user;
            case PAGE_CLOSED: return R.string.log_soak_page_closed;
            case STORAGE_LIMIT: return R.string.log_soak_storage_limit;
            case LOW_SPACE: return R.string.log_soak_low_space;
            default: return R.string.log_soak_error;
        }
    }
}
