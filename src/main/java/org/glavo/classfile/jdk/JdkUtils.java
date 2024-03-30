package org.glavo.classfile.jdk;

public class JdkUtils {

    public static final int LATEST_CLASSFILE_MAJOR_VERSION;
    public static final int LATEST_CLASSFILE_MINOR_VERSION;

    static {
        String classVersion = System.getProperty("java.class.version");
        int idx = classVersion.indexOf('.');
        if (idx > 0) {
            LATEST_CLASSFILE_MAJOR_VERSION = Integer.parseInt(classVersion.substring(0, idx));
            LATEST_CLASSFILE_MINOR_VERSION = Integer.parseInt(classVersion.substring(idx + 1));
        } else {
            LATEST_CLASSFILE_MAJOR_VERSION = Integer.parseInt(classVersion);
            LATEST_CLASSFILE_MINOR_VERSION = 0;
        }
    }

    private JdkUtils() {
    }
}
