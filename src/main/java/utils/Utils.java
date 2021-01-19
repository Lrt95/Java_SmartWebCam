package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class Utils {

    /**
     * Convert file to bytes buffer
     * @param path The file path
     * @return The Bytes Buffer
     * @throws IOException If the file not exists or if it can't read
     */
    public static byte[] readFileToBytes(String path) throws IOException {
        return Files.readAllBytes(new File(path).toPath());
    }

    /**
     * Read a file
     * @param path The file path
     * @return A ArrayList<String> with each lines
     * @throws FileNotFoundException If the file not exists
     */
    public static ArrayList<String> readFile(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner scanner = new Scanner(file);
        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return lines;
    }

    /**
     * Find the bigger element of the array
     * @param array The array
     * @return The maximum
     */
    public static int maxIndex(float[] array) {
        int best = 0;
        for (int i = 1; i < array.length; ++i) {
            if (array[i] > array[best]) {
                best = i;
            }
        }
        return best;
    }

    /**
     * Round the value to the specific precision
     * @param value The value to round
     * @param precision The precision (>= 0)
     * @return If the precision is negative
     */
    public static double round(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("precision need to be positive!");
        }
        double rounder = Math.pow(10, precision);
        return Math.round(value * rounder) / rounder;
    }
}
