package com.musicverse.server;

import lombok.val;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilities for raw data transfer
 */
public class IOUtil {
    private static final File songDir;
    static {
        songDir = Path.of(".", "songs").toFile();
        songDir.mkdirs();
    }

    public static File getSongFile(int id) {
        return new File(songDir, id + ".mp3");
    }

    public static void fileToStream(File file, OutputStream output) throws IOException {
        val size = Files.size(file.toPath());
        if (size > 0xFFFFFFFFL) throw new IllegalArgumentException("File " + file + " too big!");
        writeInt((int)size, output);
        val fileIn = new BufferedInputStream(new FileInputStream(file));
        pipe(fileIn, output, size);
    }

    public static void streamToFile(InputStream input, File file) throws IOException {
        val size = Integer.toUnsignedLong(readInt(input));
        try (val fileOut = new BufferedOutputStream(new FileOutputStream(file))) {
            pipe(input, fileOut, size);
        }
    }

    public static void pipe(InputStream input, OutputStream output, long bytes) throws IOException {
        val buf = new byte[4096];
        var read = 0;
        while (bytes > 0 && (read = input.read(buf, 0, (int) Math.min(buf.length, bytes))) > 0) {
            bytes -= read;
            output.write(buf, 0, read);
        }
    }

    public static int readInt(InputStream input) throws IOException {
        var data = 0;
        var read = 0;
        for (int i = 0; i < 4; i++) {
            read = input.read();
            if (read == -1) break;
            data = (data << 8) | (read & 0xFF);
        }
        return data;
    }

    public static void writeInt(int number, OutputStream output) throws IOException {
        output.write((number >>> 24) & 0xFF);
        output.write((number >>> 16) & 0xFF);
        output.write((number >>> 8) & 0xFF);
        output.write(number & 0xFF);
    }
}
