package com.cl.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.regex.Pattern;

final class LogSequenceVerifier {
    static final int MAX_SEQUENCE = 10_000_000;
    private static final int MAX_LINE_LENGTH = 128;
    private static final Pattern TIMESTAMP =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} [+-]\\d{4}");

    private final String prefix;
    private final BitSet seen = new BitSet();
    private final byte[] buffer = new byte[8192];
    private final StringBuilder line = new StringBuilder(MAX_LINE_LENGTH);
    private long offset;
    private long producerId;
    private int highestSequence;
    private int unique;
    private int duplicates;
    private int outOfOrder;
    private int invalid;
    private boolean lineTooLong;

    LogSequenceVerifier(String runId) {
        prefix = runId + '\t';
    }

    Report scan(File file, boolean finished) throws IOException {
        if (file.exists()) {
            try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
                long remaining = input.length() - offset;
                if (remaining < 0) {
                    throw new IOException("日志文件被截断");
                }
                input.seek(offset);
                while (remaining > 0) {
                    int count = input.read(buffer, 0, (int) Math.min(remaining, buffer.length));
                    if (count < 0) {
                        throw new IOException("读取时日志文件被截断");
                    }
                    for (int i = 0; i < count; i++) {
                        acceptByte(buffer[i]);
                    }
                    offset += count;
                    remaining -= count;
                }
            }
        } else if (offset > 0) {
            throw new IOException("日志文件已不存在");
        }

        if (finished && line.length() > 0) {
            invalid++;
            line.setLength(0);
            lineTooLong = false;
        }
        return new Report(offset, highestSequence, unique, duplicates, outOfOrder, invalid);
    }

    private void acceptByte(byte value) {
        if (value == '\n') {
            if (lineTooLong) {
                invalid++;
            } else {
                verifyLine(line.toString());
            }
            line.setLength(0);
            lineTooLong = false;
        } else if (line.length() < MAX_LINE_LENGTH) {
            line.append((char) (value & 0xff));
        } else {
            lineTooLong = true;
        }
    }

    private void verifyLine(String record) {
        int runIdStart = 0;
        if (!record.startsWith(prefix)) {
            int timestampEnd = record.indexOf('\t');
            if (timestampEnd < 0 || !TIMESTAMP.matcher(record.substring(0, timestampEnd)).matches()
                    || !record.startsWith(prefix, timestampEnd + 1)) {
                invalid++;
                return;
            }
            runIdStart = timestampEnd + 1;
        }
        int threadStart = runIdStart + prefix.length();
        int separator = record.indexOf('\t', threadStart);
        if (separator < 0) {
            invalid++;
            return;
        }
        long thread;
        int sequence;
        try {
            thread = Long.parseLong(record.substring(threadStart, separator));
            sequence = Integer.parseInt(record.substring(separator + 1));
        } catch (NumberFormatException e) {
            invalid++;
            return;
        }
        if (thread <= 0 || sequence < 1 || sequence > MAX_SEQUENCE
                || (producerId != 0 && thread != producerId)) {
            invalid++;
            return;
        }
        producerId = thread;
        if (seen.get(sequence)) {
            duplicates++;
            return;
        }
        seen.set(sequence);
        unique++;
        if (sequence < highestSequence) {
            outOfOrder++;
        }
        highestSequence = Math.max(highestSequence, sequence);
    }

    static final class Report {
        static final Report EMPTY = new Report(0, 0, 0, 0, 0, 0);

        final long bytes;
        final int highestSequence;
        final int unique;
        final int duplicates;
        final int outOfOrder;
        final int invalid;

        private Report(long bytes, int highestSequence, int unique, int duplicates, int outOfOrder,
                       int invalid) {
            this.bytes = bytes;
            this.highestSequence = highestSequence;
            this.unique = unique;
            this.duplicates = duplicates;
            this.outOfOrder = outOfOrder;
            this.invalid = invalid;
        }

        long missing(long submitted, boolean finished) {
            return (finished ? Math.max(submitted, highestSequence) : highestSequence) - unique;
        }

        boolean isComplete(long submitted) {
            return submitted > 0 && unique == submitted && highestSequence == submitted
                    && duplicates == 0 && outOfOrder == 0 && invalid == 0;
        }
    }
}
