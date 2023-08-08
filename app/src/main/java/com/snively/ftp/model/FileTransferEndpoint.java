package com.snively.ftp.model;

public class FileTransferEndpoint {

    private final String address;
    private final String username;
    private final String password;
    private final String directory;

    public FileTransferEndpoint(String address, String username, String password, String directory) {
        this.address = address;
        this.username = username;
        this.password = password;
        this.directory = directory;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDirectory() {
        return directory;
    }
}
