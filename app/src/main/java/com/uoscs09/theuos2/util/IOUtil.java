package com.uoscs09.theuos2.util;

import android.Manifest;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;

import mj.android.utils.common.IOUtils;

import static com.uoscs09.theuos2.util.AppUtil.context;

public class IOUtil {
    private static final String TAG = "IOUtil";
    public static final String FILE_TIMETABLE = "timetable_file_v1.13";
    public static final String FILE_REST = "rest_file";
    public static final String FILE_LIBRARY_SEAT = "file_library_seat";

    private IOUtil() {
    }

    /**
     * 주어진 이름의 파일을 내부 저장소에서 읽어온다.
     *
     * @return 파일이 존재하고, 성공적으로 읽어왔을 경우 : 해당 객체 <br>
     * 파일이 없거나 예외가 발생할 경우 : null
     * @throws IOException
     * @throws StreamCorruptedException
     * @throws ClassNotFoundException
     */
    public static <T> T readFromInternalFile(String fileName) throws IOException, ClassNotFoundException {
        return IOUtils.readInternalFile(context(), fileName);
    }

    public static String readStringFromInternalFile(String fileName) throws IOException {
        return IOUtils.readInternalStringFile(context(), fileName);
    }


    /**
     * 주어진 이름의 파일을 읽어온다.
     *
     * @return 파일이 존재하고, 성공적으로 읽어왔을 경우 : 해당 객체 <br>
     * 파일이 없거나 예외가 발생할 경우 : null
     * @throws IOException
     * @throws StreamCorruptedException
     * @throws ClassNotFoundException
     */
    public static <T> T readFromExternalFile(File file) throws IOException, ClassNotFoundException {
        return IOUtils.readExternalFile(file);
    }

    public static String readStringFromExternalFile(File file) throws IOException {
        return IOUtils.readExternalStringFile(file);
    }

    /**
     * 외부 저장소 (ex : storage)에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    @SuppressWarnings("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static void writeObjectToExternalFile(String fileName, Object obj) throws IOException {
        if (obj instanceof String) IOUtils.writeExternalFile(fileName, (String) obj);
        else IOUtils.writeExternalFile(fileName, obj);
    }

    /**
     * 내부 저장소에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    public static void writeObjectToInternalFile(String fileName, Object obj) throws IOException {
        if (obj instanceof String) IOUtils.writeInternalFile(context(), fileName, (String) obj);
        else IOUtils.writeInternalFile(context(), fileName, obj);
    }


    @Nullable
    public static <T> T readInternalFileSilent(String fileName) {
        try {
            return readFromInternalFile(fileName);

        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeObjectToInternalFileSilent(String fileName, Object obj) {
        try {
            writeObjectToInternalFile(fileName, obj);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteInternalFile(String fileName) {
        try {
            return context().deleteFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void closeStream(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 파일 또는 폴더를 삭제한다. 폴더의 경우 재귀적으로 탐색하여 내부의 파일까지 모두 삭제한다.
     */
    public static void clearApplicationFile(File dir) {
        if (dir == null || !dir.isDirectory())
            return;
        File[] children = dir.listFiles();

        if (children == null)
            return;

        try {
            for (File file : children) {
                if (file.isDirectory())
                    clearApplicationFile(file);
                else {
                    if (!file.delete())
                        Log.e(TAG, "Deleting file [" + file + "] has failed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static <T> FileWriteFunc<T> newExternalFileWriteFunc(String fileName) {
        return new FileWriteFunc<>(fileName, true);
    }

    public static <T> FileWriteFunc<T> newInternalFileWriteFunc(String fileName) {
        return new FileWriteFunc<>(fileName, false);
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static FileWriteFunc<String> newStringExternalFileWriteFunc(String fileName) {
        return new StringFileWriteFunc(fileName, true);
    }

    public static FileWriteFunc<String> newStringInternalFileWriteFunc(String fileName) {
        return new StringFileWriteFunc(fileName, false);
    }

    /*
    * 처리 후 반환값은 Input Object 이다.
    * */
    static class FileWriteFunc<T>  {
        final String fileName;
        private final boolean isExternal;

        FileWriteFunc(String fileName, boolean isExternal) {
            this.fileName = fileName;
            this.isExternal = isExternal;
        }

        public T func(T t) throws IOException {
            if (isExternal)
                //noinspection ResourceType
                writeObjectToExternalFile(fileName, t);
            else
                writeObjectToInternalFile(fileName, t);
            return t;
        }
    }

    /*
    * 처리 후 반환값은 fileName 이다.
    * */
    private static class StringFileWriteFunc extends FileWriteFunc<String> {
        StringFileWriteFunc(String fileName, boolean isExternal) {
            super(fileName, isExternal);
        }

        @Override
        public String func(String t) throws IOException {
            super.func(t);
            return fileName;
        }
    }
}
