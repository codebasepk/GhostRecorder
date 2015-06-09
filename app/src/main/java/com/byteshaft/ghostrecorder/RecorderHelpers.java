package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecorderHelpers extends ContextWrapper implements CustomMediaRecorder.OnNewFileWrittenListener {

    private CustomMediaRecorder mRecorder;

    public RecorderHelpers(Context base) {
        super(base);
        mRecorder = CustomMediaRecorder.getInstance();
    }

    void startRecording(int time) {
        if (CustomMediaRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }
        mRecorder.reset();
        mRecorder.setOnNewFileWrittenListener(this);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(16000);
        mRecorder.setDuration(time);
        mRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + "Recordings/" + getTimeStamp() + ".aac");

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    void stopRecording() {
        if (CustomMediaRecorder.isRecording()) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    void createRecordingDirectoryIfNotAlreadyCreated() {
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Recordings");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.US).format(new Date());
    }

    @Override
    public void onNewRecordingCompleted(String path) {
        System.out.println(path);
    }

    String getHashsumForFile(String path) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] dataBytes = new byte[1024];

        int bytesRead = 0;

        try {
            while ((bytesRead = fileInput.read(dataBytes)) != -1) {
                messageDigest.update(dataBytes, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] digestBytes = messageDigest.digest();

        StringBuffer sb = new StringBuffer("");

        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Checksum for the File: " + sb.toString());

        try {
            fileInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
