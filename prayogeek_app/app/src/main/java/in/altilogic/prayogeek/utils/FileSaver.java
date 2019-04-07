package in.altilogic.prayogeek.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileSaver {
    public static final String TAG = "YOUSCOPE-SAVER";
    private String mFilePath = "PRAYOGeek/Serial_Log";
    private String mFileName;
    private String extension;

    public FileSaver(String filename){
        mFileName = filename;
        if(mFileName != null && mFileName.contains(".") ){
            extension = "";
        }
        else {
            extension = ".log";
        }

        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/PRAYOGeek");
        if(!f.exists()) {
            if(!f.mkdirs()){
                Log.d(TAG, "Error: directory PRAYOGeek not created");
            }
        }

        f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/PRAYOGeek/Serial_Log");
        if(!f.exists()) {
            if(!f.mkdirs()){
                Log.d(TAG, "Error: directory PRAYOGeek/Serial_Log not created");
            }
        }
    }

    public void write(String msg) {
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/"
                            + mFilePath + "/"+ mFileName +  extension, true), "utf-8"));

            writer.write(msg);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
