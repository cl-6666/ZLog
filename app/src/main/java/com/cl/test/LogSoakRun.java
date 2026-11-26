package com.cl.test;

import com.cl.zlog.LogLevel;
import com.cl.zlog.Logger;
import com.cl.zlog.ZLog;
import com.cl.zlog.flattener.PatternFlattener;
import com.cl.zlog.printer.file.FilePrinter;
import com.cl.zlog.printer.file.backup.NeverBackupStrategy;
import com.cl.zlog.printer.file.naming.ChangelessFileNameGenerator;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

final class LogSoakRun {
    static final String RECORD_PATTERN = "{d yyyy-MM-dd HH:mm:ss.SSS Z}\t{m}";
    private static final String LOG_TAG = "LOG-SOAK";
    private static final String RECORD_FILE = "records.log";
    private static final long MAX_TEST_BYTES = 256L * 1024 * 1024;
    private static final long MIN_FREE_BYTES = 128L * 1024 * 1024;
    private static final long CHECK_INTERVAL_SECONDS = 2;

    enum StopReason { USER, PAGE_CLOSED, STORAGE_LIMIT, LOW_SPACE, ERROR }

    static final class LogFile {
        final File file;
        volatile LogSequenceVerifier.Report report = LogSequenceVerifier.Report.EMPTY;

        private final File root;
        private final String storage;
        private final LogSequenceVerifier verifier;
        private boolean verificationFailed;

        private LogFile(File root, String runId, String storage) {
            this.root = root;
            this.storage = storage;
            file = new File(new File(root, runId), RECORD_FILE);
            verifier = new LogSequenceVerifier(runId);
        }
    }

    final LogFile internal;
    final LogFile external;

    private final int rate;
    private final String runId = UUID.randomUUID().toString();
    private final long startedAt = System.nanoTime();
    private final CountDownLatch stopSignal = new CountDownLatch(1);
    private final CountDownLatch writerFinished = new CountDownLatch(1);
    private final AtomicReference<StopReason> stopReason = new AtomicReference<>();
    private final AtomicLongArray latencyBuckets = new AtomicLongArray(44);
    private final AtomicReference<String> failure = new AtomicReference<>();

    private volatile long submitted;
    private volatile long callStartedAt = -1;
    private volatile long endedAt = -1;
    private volatile boolean outputsReady;
    private volatile boolean verificationFinished;

    private LogSoakRun(File internalRoot, File externalRoot, int rate) {
        this.rate = rate;
        internal = new LogFile(internalRoot, runId, "内部存储");
        external = new LogFile(externalRoot, runId, "外部存储");
    }

    static LogSoakRun start(File internalRoot, File externalRoot, int rate) {
        LogSoakRun run = new LogSoakRun(internalRoot, externalRoot, rate);
        new Thread(run::produce, "LogSoak-Producer").start();
        new Thread(run::verify, "LogSoak-Verifier").start();
        return run;
    }

    void stop(StopReason reason) {
        if (stopReason.compareAndSet(null, reason)) {
            stopSignal.countDown();
        }
    }

    int rate() {
        return rate;
    }

    StopReason stopReason() {
        return stopReason.get();
    }

    boolean isFinished() {
        return writerFinished.getCount() == 0 && verificationFinished;
    }

    String failure() {
        return failure.get();
    }

    long elapsedMillis() {
        long end = endedAt;
        return end < 0 ? elapsedNowMillis() : end;
    }

    long waitingMillis() {
        long start = callStartedAt;
        return start < 0 ? 0 : elapsedNowMillis() - start;
    }

    long submitted() {
        return submitted;
    }

    private long elapsedNowMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    long p99Micros() {
        long calls = submitted;
        if (calls == 0) {
            return 0;
        }
        long target = (calls * 99 + 99) / 100;
        long counted = 0;
        for (int i = 0; i < latencyBuckets.length(); i++) {
            counted += latencyBuckets.get(i);
            if (counted >= target) {
                return 1L << i;
            }
        }
        return 0;
    }

    private void produce() {
        try {
            prepare(internal);
            prepare(external);
            if (stopReason() != null) {
                return;
            }
            try (FilePrinter internalPrinter = createPrinter(internal);
                 FilePrinter externalPrinter = createPrinter(external)) {
                Logger logger = ZLog.tag(LOG_TAG)
                        .logLevel(LogLevel.INFO)
                        .disableThreadInfo()
                        .disableStackTrace()
                        .disableBorder()
                        .printers(internalPrinter, externalPrinter)
                        .build();
                outputsReady = true;
                produceLogs(logger);
            }
        } catch (IOException | RuntimeException e) {
            fail(e);
        } catch (InterruptedException e) {
            fail(e);
            Thread.currentThread().interrupt();
        } finally {
            endedAt = elapsedNowMillis();
            writerFinished.countDown();
        }
    }

    private void prepare(LogFile log) throws IOException {
        if (stopReason() == null) {
            if (!log.root.isDirectory() && !log.root.mkdirs()) {
                throw new IOException(log.storage + "无法创建测试目录");
            }
            checkStorage(log);
        }
    }

    private FilePrinter createPrinter(LogFile log) throws IOException {
        try {
            return new FilePrinter.Builder(log.file.getParent())
                    .fileNameGenerator(new ChangelessFileNameGenerator(RECORD_FILE))
                    .backupStrategy(new NeverBackupStrategy())
                    .flattener((time, level, tag, message) -> message)
                    .build();
        } catch (RuntimeException e) {
            throw new IOException(log.storage + "无法初始化日志文件", e);
        }
    }

    private void produceLogs(Logger logger) throws InterruptedException {
        String prefix = runId + '\t' + Thread.currentThread().getId() + '\t';
        PatternFlattener flattener = new PatternFlattener(RECORD_PATTERN);
        long interval = TimeUnit.SECONDS.toNanos(1) / rate;
        for (int sequence = 1; stopReason() == null; sequence++) {
            if (sequence > LogSequenceVerifier.MAX_SEQUENCE) {
                stop(StopReason.STORAGE_LIMIT);
                return;
            }
            String message = flattener.flatten(System.currentTimeMillis(), LogLevel.INFO, LOG_TAG,
                    prefix + sequence).toString();
            callStartedAt = elapsedNowMillis();
            long before = System.nanoTime();
            long took;
            try {
                logger.i(message);
                took = System.nanoTime() - before;
            } finally {
                callStartedAt = -1;
            }
            long micros = Math.max(1, (took + 999) / 1000);
            int bucket = Math.min(latencyBuckets.length() - 1,
                    Long.SIZE - Long.numberOfLeadingZeros(micros - 1));
            latencyBuckets.incrementAndGet(bucket);
            submitted = sequence;
            long remaining = interval - (System.nanoTime() - before);
            if (remaining > 0 && stopSignal.await(remaining, TimeUnit.NANOSECONDS)) {
                return;
            }
        }
    }

    private void verify() {
        try {
            while (!writerFinished.await(CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS)) {
                verifyFile(internal, false);
                verifyFile(external, false);
            }
            verifyFile(internal, true);
            verifyFile(external, true);
        } catch (InterruptedException e) {
            fail(e);
            Thread.currentThread().interrupt();
        } finally {
            verificationFinished = true;
        }
    }

    private void verifyFile(LogFile log, boolean finished) {
        if (log.verificationFailed) {
            return;
        }
        try {
            log.report = log.verifier.scan(log.file, finished);
            if (!finished && outputsReady && stopReason() == null) {
                checkStorage(log);
            }
        } catch (IOException | RuntimeException e) {
            log.verificationFailed = true;
            fail(new IOException(log.storage + "检查失败（" + e.getMessage() + "）", e));
        }
    }

    private void checkStorage(LogFile log) throws IOException {
        if (!log.root.canWrite()) {
            throw new IOException(log.storage + "测试目录不可写");
        }
        if (log.root.getUsableSpace() < MIN_FREE_BYTES) {
            stop(StopReason.LOW_SPACE);
            return;
        }
        File[] sessions = log.root.listFiles();
        if (sessions == null) {
            throw new IOException(log.storage + "无法读取测试目录");
        }
        long bytes = 0;
        for (File session : sessions) {
            bytes += new File(session, RECORD_FILE).length();
            if (bytes >= MAX_TEST_BYTES) {
                stop(StopReason.STORAGE_LIMIT);
                return;
            }
        }
    }

    private void fail(Exception error) {
        failure.compareAndSet(null, error.getMessage() == null
                ? error.getClass().getSimpleName() : error.getMessage());
        stop(StopReason.ERROR);
    }
}
