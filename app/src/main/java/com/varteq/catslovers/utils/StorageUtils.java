package com.varteq.catslovers.utils;

import com.varteq.catslovers.AppController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StorageUtils {

    public static File getAppExternalDataDirectoryFile() {
        File dataDirectoryFile = AppController.getInstance().getExternalFilesDir(null);
        dataDirectoryFile.mkdirs();

        return dataDirectoryFile;
    }

    public static File getImagePickerDirectoryFile() {
        File dataDirectoryFile = new File(getAppExternalDataDirectoryFile(), "/ImagePicker");
        dataDirectoryFile.mkdirs();

        return dataDirectoryFile;
    }

    public static void writeStringToFile(String string, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeObjectToFile(Object object, File file) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObjectFromFile(File file) {
        ObjectInputStream ois = null;
        T object = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            object = (T) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return object;
    }

    public static void clearDirectory(File directory) {
        if (directory.isDirectory())
            for (File file : directory.listFiles())
                if (!file.isDirectory())
                    file.delete();
    }
}
