package com.gmail.sync667.gougouserver.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public abstract class Config {

    private Config config;
    protected static Path path;
    final static Charset ENCODING = StandardCharsets.UTF_8;

    public Config(Path path) {
        Config.path = path;
    }

    public Config getConfig() {
        return config;
    }
}
