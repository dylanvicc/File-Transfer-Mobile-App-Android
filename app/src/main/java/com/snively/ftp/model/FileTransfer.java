package com.snively.ftp.model;

import java.io.InputStream;

public class FileTransfer {

    private final String name;
    private final InputStream stream;

    public FileTransfer(String name, InputStream stream) {
        this.name = name;
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getName() {
        return name;
    }
}
