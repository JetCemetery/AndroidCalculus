package com.jetcemetery.androidcalulus.helper;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class getCPU_Cnt {

    public int getCount() {
        return getNumCores();
    }
    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        //taken from
        //http://forums.makingmoneywithandroid.com/android-development/280-%5Bhow-%5D-get-number-cpu-cores-android-device.html
        String TAG = "GetCPU_Cnt";
        try {

            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d(TAG, "CPU Count: "+files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;

        } catch(Exception e) {
            //Print exception
            Log.d(TAG, "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }


}
