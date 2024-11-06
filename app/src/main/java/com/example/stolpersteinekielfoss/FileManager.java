package com.example.stolpersteinekielfoss;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Copyright Mathias Uebel, Kiel Germany
 *
 * @see <a href="https://github.com/Klaus-Thaler/Stolpersteine_Kiel_Foss">Klaus Thaler on GitHub</a>
 *
 * @author Mathias Uebel
 */
public class FileManager {
    public static Long CacheFileLastModified(Context context, File file) {
        return file.lastModified();
    }
    public static boolean CacheFileExist(Context context, String filename){
        return context.getFileStreamPath(filename).exists();
    }
    public static Boolean externalStorageRW(){
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else if (extStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        } else {
            return false;
        }

    }
    static void saveDataInDownload(String subDir, String fileName, String text){
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //if you want to create a sub-dir
        root = new File(root, subDir);
        root.mkdir();
        // select the name for your file
        root = new File(root, fileName);
        try {
            FileOutputStream fout = new FileOutputStream(root);
            fout.write(text.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "IOE_saveDataInDownload: " + e.getMessage());
            boolean bool = false;
            try {
                // try to create the file
                bool = root.createNewFile();
            } catch (IOException ex) {
                Log.e(TAG, "IOE_saveDataInDownload: " + e.getMessage());
            }
            if (bool){
                // call the method again
                saveDataInDownload(subDir, fileName, text);
            }else {
                throw new IllegalStateException("Failed to create file");
            }
        } catch (IOException e) {
            Log.e(TAG, "IOE_saveDataInDownload: " + e.getMessage());
        }
    }
    public static void saveCacheFile(Context context, String filename, String dataString) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(dataString.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "IOE_saveCacheFile: " + e.getMessage());
        }
    }
    public static File loadCacheFile (Context context, String filename) {
        File file = null;
        try {
            file = context.getFileStreamPath(filename);
        } catch (Exception e) {
            Log.e(TAG, "IOE_loadCacheFile: " + e.getMessage());
        }
        return file;
    }
    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
            return jsonString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
    public static String readCacheFile(Context context, String filename) {
        String data = null;
        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(filename);
            InputStream stream = new BufferedInputStream(fileInputStream);
            data = convertStreamToString(stream);
            fileInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "IOE_readCacheFile: " + e.getMessage());
        }
        return data;
    }
    public static String readAssetFile(Context context, String fileName) {
        String returnString = "";
        AssetManager assets;
        try {
            assets = context.getAssets();
            final InputStream input = assets.open(fileName);
            int exceptedLength = input.available();
            final byte[] buffer = new byte[exceptedLength];
            final int len = input.read(buffer);
            if (len == exceptedLength) {
                returnString = new String(buffer, StandardCharsets.UTF_8);
            }
            } catch (IOException e) {
                Log.e(TAG, "IOE_readAssetFile: " + e.getMessage());
            }
            return returnString;
        }

        private static String convertStreamToString (InputStream is){
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
            } catch (IOException e) {
                Log.e(TAG, "IOE_convertStream: " + e.getMessage());
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOE_convertStream: " + e.getMessage());
                }
            }
            return stringBuilder.toString();
        }
    }
