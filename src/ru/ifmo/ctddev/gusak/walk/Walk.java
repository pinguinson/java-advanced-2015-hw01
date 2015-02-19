package ru.ifmo.ctddev.gusak.walk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by pinguinson on 17.02.2015.
 */
public class Walk {

    public static void processInputFile(Path inputFile, Path outputFile, Charset charset) {
        try (BufferedReader reader = Files.newBufferedReader(inputFile, charset);
             BufferedWriter writer = Files.newBufferedWriter(outputFile, charset)) {

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                Path currentFile = Paths.get(currentLine);
                int hash = 0;
                long startTime = System.currentTimeMillis();
                try (FileChannel channel = new FileInputStream(currentFile.toFile()).getChannel()) {
                    hash = hashFNV(channel);

                } catch (IOException e) {
                    //System.err.println("HashCalculatingException");
                }
                long endTime = System.currentTimeMillis();
                System.err.println(String.valueOf(endTime - startTime) + " ms");
                String hashString = String.format("%08x", hash);
                String result = hashString + " " + currentFile.toString()
                        + System.getProperty("line.separator");
                writer.write(result);
                writer.flush();
            }

        } catch (NoSuchFileException e) {
            System.err.println("Input file not found.");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Input file charset is not UTF-8.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //MappedByteBuffer
    private static int hashFNV(FileChannel channel) {
        final int OFFSET_BASIS = 0x811c9dc5;
        final int FNV_PRIME = 0x01000193;
        final int PAGE_SIZE = 1024 * 1024; //1 mb

        int hash = OFFSET_BASIS;


        ByteBuffer reader = ByteBuffer.allocate(PAGE_SIZE);
        int c = 0;
        try {
            while (-1 != (channel.read(reader))) {
                reader.flip();

                while (reader.hasRemaining()) {
                    c = reader.get() & 0xff;
                    hash *= FNV_PRIME;
                    hash ^= c;
                }

                reader.clear();
            }

        } catch (Exception e) {
            System.err.println("Error while calculating hash:" + e.getMessage());
            return 0;
        }

        return hash;
    }

    public static void main(String[] args) {
        Charset charset = Charset.forName("UTF-8");
        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        processInputFile(inputFile, outputFile, charset);

    }
}
