package com.marsss.database.categories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Filter {
    public static final LinkedList<String> filter = new LinkedList<>();

    private static void getFilter(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                filter.add(line);
            }
        }
    }
}
