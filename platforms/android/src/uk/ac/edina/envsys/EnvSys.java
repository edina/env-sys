/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package uk.ac.edina.envsys;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EnvSys extends CordovaActivity
{
    private static final String TAG = "CordovaLog";
    private static final String WEB_DB = "webDb";
    private static final String ANDROID_DATA_DIR = "/Android/data/";
    private static final String MAPCACHE_DIR = "mapcache";
    private static final String TILES_ZIP = "tiles.zip";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadUrl(launchUrl);

        File mapcacheBaseDirectory = buildMapcacheBaseDirectory();

        File targetWebDb = new File(mapcacheBaseDirectory, WEB_DB);
        File targetTilesZip = new File(mapcacheBaseDirectory, TILES_ZIP);

        copyFileFromAssets(WEB_DB, targetWebDb);
        boolean successfulCopy = copyFileFromAssets(TILES_ZIP, targetTilesZip);
        if(successfulCopy) {
            unzip(targetTilesZip, mapcacheBaseDirectory);
        }
    }

    private File buildMapcacheBaseDirectory() {

        String applicationPackage = getActivity().getApplicationContext().getPackageName();
        String mapcacheBaseDirectory = Environment.getExternalStorageDirectory().getPath() + ANDROID_DATA_DIR + applicationPackage + "/" + MAPCACHE_DIR ;

        File fbaseDirectory = new File(mapcacheBaseDirectory);

        if(!fbaseDirectory.exists()){
            fbaseDirectory.mkdirs();

        }
        return fbaseDirectory;
    }

    /**
     *
     * @param assetName
     * @param fileTarget
     * @return true if successful copy
     */
    public boolean copyFileFromAssets(String assetName, File fileTarget) {


        if(fileTarget.exists()){
            return false;
        }

        AssetManager assetManager = getActivity().getAssets();

        InputStream in;
        OutputStream out;

        try {
            Log.i(TAG, "copyFile() " + assetName);
            in = assetManager.open(assetName);

            out = new FileOutputStream(fileTarget);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in copyFile() of "+fileTarget);
            Log.e(TAG, "Exception in copyFile() "+e.toString());
            return false;
        }
        Log.d(TAG, "Finished Copying " + assetName);
        return true;

    }


    public void unzip(final File file, final File destination)  {
        new Thread() {
            public void run() {
                long START_TIME = System.currentTimeMillis();
                long FINISH_TIME = 0;
                long ELAPSED_TIME = 0;
                try {
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
                    String workingDir = destination.getAbsolutePath()+"/";

                    byte buffer[] = new byte[4096];
                    int bytesRead;
                    ZipEntry entry = null;
                    while ((entry = zin.getNextEntry()) != null) {
                        if (entry.isDirectory()) {
                            File dir = new File(workingDir, entry.getName());
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            Log.i(TAG, "[DIR] "+entry.getName());
                        } else {
                            FileOutputStream fos = new FileOutputStream(workingDir + entry.getName());
                            while ((bytesRead = zin.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                            fos.close();
                            Log.i(TAG, "[FILE] "+entry.getName());
                        }
                    }
                    zin.close();

                    FINISH_TIME = System.currentTimeMillis();
                    ELAPSED_TIME = FINISH_TIME - START_TIME;
                    Log.i(TAG, "COMPLETED in "+(ELAPSED_TIME/1000)+" seconds.");
                } catch (Exception e) {
                    Log.e(TAG, "FAILED " + e);
                }
            };
        }.start();
    }


}
