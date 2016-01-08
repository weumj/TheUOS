package com.uoscs09.theuos2.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class IOUtil {
    private static final String TAG = "IOUtil";
    public static final String FILE_TIMETABLE = "timetable_file_v1.2";
    public static final String FILE_REST = "rest_file";
    public static final String FILE_LIBRARY_SEAT = "file_library_seat";

    private IOUtil(){}

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

    public static boolean writeObjectToFileSuppressed(Context context, String fileName, Object obj) {
        try {
            writeObjectToFile(context, fileName, obj);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 내부 저장소에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    public static void writeObjectToFile(Context context, String fileName, Object obj) throws IOException {
        if (obj == null) {
            Log.e(TAG, "Cannot write null object.");
            return;
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        } finally {
            closeStream(oos);
            closeStream(bos);
            closeStream(fos);
        }

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
     * 외부 저장소 (ex : storage)에 주어진 이름으로 파일을 저장한다.
     *
     * @param obj 저장할 객체
     * @throws IOException
     */
    public static void writeObjectToExternalFile(String fileName, Object obj) throws IOException {
        if (obj == null) {
            Log.e(TAG, "Cannot write null object.");
            return;
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fileName);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        } finally {
            closeStream(oos);
            closeStream(bos);
            closeStream(fos);
        }
    }

    /*
    /**
     * 파일을 비 동기적으로 읽는다. <br>
     * 리스너의 result에 exception 발생 여부가 전달되고, <br>
     * data에 성공 할 경우 원하는 data, 실패 했을 경우 exception이 전달된다.

    public static <T> void readFromFileAsync(final Context context, final String fileName, OnTaskFinishedListener<T> l) {
        AsyncUtil.execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return readFromFile(context, fileName);
            }
        }, l);
    }
    */

    /*
       /**
     * 파일을 비 동기적으로 저장한다. <br>
     * 리스너의 result에 exception 발생 여부가 전달되고, <br>
     * data에 성공 할 경우 성공 여부, 실패 했을 경우 exception이 전달된다.

    public static void writeObjectToFileAsync(Context context, final String fileName, final Object obj, OnTaskFinishedListener<Boolean> l) {
        final Context appContext = context.getApplicationContext();
        AsyncUtil.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                writeObjectToFile(appContext, fileName, obj);
                return true;
            }
        }, l);
    }
    */


    public static void writeObjectToFileAsync(Context context, final String fileName, final Object obj) {
        final Context appContext = context.getApplicationContext();
        AsyncUtil.execute(() -> {
            try {
                writeObjectToFile(appContext, fileName, obj);
            } catch (IOException e) {
                e.printStackTrace();
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

        if (children == null)
            return;

        try {
            for (File file : children) {
                if (file.isDirectory())
                    clearApplicationFile(file);
                else {
                    if (!file.delete())
                        Log.e("IOUtil", "Deleting file [" + file + "] has failed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final class Builder<T> implements Request.Builder<T> {
        private final String fileName;
        private Context context;

        public Builder(String fileName) {
            this.fileName = fileName;
        }

        public Builder<T> setContext(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        @Override
        public Request<T> build() {
            return new FileOpenRequest<>(context, fileName);
        }
    }

    private static class FileOpenRequest<T> extends AbstractRequest<T> {
        private final String fileName;
        private final Context context;

        public FileOpenRequest(@Nullable Context context, String fileName) {
            this.fileName = fileName;
            this.context = context != null ? context.getApplicationContext() : null;
        }

        @Override
        public T get() throws Exception {
            if (context != null)
                return readFromFile(context, fileName);
            else
                return null;
        }
    }


    public static <T> Processor<T, T> newFileWriteProcessor(@Nullable Context context, String fileName) {
        return new FileWriteProcessor<>(context, fileName);
    }

    static class FileWriteProcessor<T> implements Processor<T, T> {
        private final String fileName;
        private final Context context;

        public FileWriteProcessor(@Nullable Context context, String fileName) {
            this.fileName = fileName;
            this.context = context != null ? context.getApplicationContext() : null;
        }

        @Override
        public T process(T t) throws Exception {
            if (context == null)
                writeObjectToExternalFile(fileName, t);
            else
                writeObjectToFile(context, fileName, t);
            return t;
        }
    }

    /*
    public static byte[] toByteArray(Serializable obj) throws IOException {
        ObjectOutputStream objectOutput = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            objectOutput = new ObjectOutputStream(output);
            objectOutput.writeObject(obj);
            return output.toByteArray();
        } finally {
            closeStream(output);
            closeStream(objectOutput);
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

*/
}
