package com.uoscs09.theuos2.async;

import com.uoscs09.theuos2.util.IOUtil;

public class FileWriteProcessor<T> implements Processor<T, String> {
    protected final String fileName;

    public FileWriteProcessor(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String process(T t) throws Exception {
        IOUtil.writeObjectToExternalFile(fileName, t);
        return fileName;
    }
}
