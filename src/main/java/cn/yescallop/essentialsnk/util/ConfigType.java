package cn.yescallop.essentialsnk.util;

import java.io.File;
import java.util.Objects;

public class ConfigType {
    private final File file;
    private final int type;

    public ConfigType(File file, int type) {
        this.file = file;
        this.type = type;
    }

    File getFile() {
        return file;
    }

    int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.hashCode(), type);
    }

    @Override
    public String toString() {
        return "ConfigType(file=" + file + ", type=" + type + ")";
    }
}
