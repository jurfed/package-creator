package com.octavianonline.packagepreparer.model;

import java.util.Objects;

public class CriticalFile implements Comparable<CriticalFile>{

    private String path;
    private String name;
    private boolean check;

    public CriticalFile(String path, String name, boolean check) {
        this.path = path;
        this.name = name;
        this.check = check;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CriticalFile that = (CriticalFile) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(CriticalFile o) {
        return this.name.compareTo(o.getName());
    }
}
