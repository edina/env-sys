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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EnvSys extends CordovaActivity
{
    private static final String DEFAULT_FORM = "/Android/data/uk.ac.edina.envsys/editors/private/envsys.edtr";
    private static final String SOURCE_FORM = "forms/envsys.edtr";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadUrl(launchUrl);

        try{
            File form = new File(Environment.getExternalStorageDirectory(), DEFAULT_FORM);
            if(!form.exists()){
                Log.v("CordovaLog", "Default form doesn't exist");
                this.copy(form);
            }
            else{
                Log.v("CordovaLog", "Using " + DEFAULT_FORM);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // copy form from www root to ftopen assets
    private void copy(File form) throws IOException{
        InputStream in = this.getApplicationContext().getAssets().open(SOURCE_FORM);
        OutputStream out = new FileOutputStream(form);
        byte[] buf = new byte[1024];
        int len; while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        in.close(); out.close();
        Log.v("CordovaLog", SOURCE_FORM + " copied to " + DEFAULT_FORM);
    }
}
