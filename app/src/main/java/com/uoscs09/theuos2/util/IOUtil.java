package com.uoscs09.theuos2.util;

import android.content.Context;
import android.support.annotation.Nullable;

import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.common.AsyncLoader.OnTaskFinishedListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.concurrent.Callable;

public class IOUtil {
    public static final String FILE_TIMETABLE = "timetable_file";
    public static final String FILE_COLOR_TABLE = "color_table_file";
    public static final String FILE_REST = "rest_file";
    public static final String FILE_LIBRARY_SEAT = "file_library_seat";

    /**
     * 주어진 이름의 파일을 읽어온다.
     *
     * @return 파일이 존재하고, 성공적으로 읽어왔을 경우 : 해당 객체 <br>
     * 파일이 없거나 예외가 발생할 경우 : null
     * @throws IOException
     * @throws StreamCorruptedException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T readFromFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        T object;
        try {
            fis = context.openFileInput(fileName);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            object = (T) ois.readObject();
        } finally {
            closeStream(ois);
            closeStream(bis);
            closeStream(fis);
        }
        return object;
    }

    @Nullable
    public static <T> T readFromFileSuppressed(Context context, String fileName) {
        try {
            return readFromFile(context, fileName);

        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveToFileSuppressed(Context context, String fileName, Object obj) {
        try {
            return saveToFile(context, fileName, obj);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @return 성공 여부
     * @throws IOException
     */
    public static boolean saveToFile(Context context, String fileName, Object obj) throws IOException {
        boolean state = false;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            state = true;
        } finally {
            closeStream(oos);
            closeStream(bos);
            closeStream(fos);
        }
        return state;
    }

    private static void closeStream(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 파일을 비 동기적으로 읽는다. <br>
     * 리스너의 result에 exception 발생 여부가 전달되고, <br>
     * data에 성공 할 경우 원하는 data, 실패 했을 경우 exception이 전달된다.
     */
    public static <T> void readFromFileAsync(final Context context, final String fileName, OnTaskFinishedListener<T> l) {
        AsyncLoader.excute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return readFromFile(context, fileName);
            }
        }, l);
    }

    /**
     * 파일을 비 동기적으로 저장한다. <br>
     * 리스너의 result에 exception 발생 여부가 전달되고, <br>
     * data에 성공 할 경우 성공 여부, 실패 했을 경우 exception이 전달된다.
     */
    public static void saveToFileAsync(final Context context, final String fileName, final Object obj, OnTaskFinishedListener<Boolean> l) {
        AsyncLoader.excute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return saveToFile(context, fileName, obj);
            }
        }, l);
    }

    public static void saveObjectToFileAsync(final Context context, final String fileName, final Object obj) {
        AsyncLoader.excute(new Runnable() {
            @Override
            public void run() {
                try {
                    saveToFile(context, fileName, obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 파일 또는 폴더를 삭제한다. 폴더의 경우 재귀적으로 탐색하여 내부의 파일까지 모두 삭제한다.
     */
    public static void clearApplicationFile(File dir) {
        if (dir == null || !dir.isDirectory())
            return;
        File[] children = dir.listFiles();
        try {
            for (File file : children) {
                if (file.isDirectory())
                    clearApplicationFile(file);
                else
                    file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] toByteArray(Serializable obj) throws IOException {
        ObjectOutputStream objectOuput = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            objectOuput = new ObjectOutputStream(output);
            objectOuput.writeObject(obj);
            return output.toByteArray();
        } finally {
            closeStream(output);
            closeStream(objectOuput);
        }
    }

    public static <T extends Serializable> T fromByteArray(byte[] array)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                array);

        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            @SuppressWarnings("unchecked")
            T obj = (T) objectInputStream.readObject();
            return obj;
        } finally {
            closeStream(objectInputStream);
            closeStream(byteArrayInputStream);
        }
    }

}
