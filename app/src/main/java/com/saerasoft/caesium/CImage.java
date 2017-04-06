package com.saerasoft.caesium;


class CImage {
    private int id;
    private String fullPath;
    private String name;
    private long modifiedTimestamp;
    private long size;
    private String bucketName;

    CImage(int id,
           String name,
           String fullPath,
           long size,
           long modifiedTimestamp,
           String bucketName) {

        this.id = id;
        this.fullPath = fullPath;
        this.name = name;
        this.modifiedTimestamp = modifiedTimestamp;
        this.size = size;
        this.bucketName = bucketName;
    }

    /* -- Getters and Setters -- */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
