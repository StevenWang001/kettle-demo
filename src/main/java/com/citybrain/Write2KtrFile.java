package com.citybrain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Write2KtrFile {
    public static void write(String filename, String content) throws Exception {
        File file = new File(filename);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(content);
        writer.flush();
        writer.close();
    }
}
