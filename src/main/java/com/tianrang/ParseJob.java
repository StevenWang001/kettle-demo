package com.tianrang;

import com.tianrang.bean.Job;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ParseJob {

    public static Job parse(InputStream inputStream) throws Exception {
        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, Job.class);
    }
}
