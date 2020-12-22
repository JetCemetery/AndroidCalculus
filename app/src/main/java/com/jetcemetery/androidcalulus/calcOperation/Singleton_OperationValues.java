package com.jetcemetery.androidcalulus.calcOperation;

public class Singleton_OperationValues {
    private static final String TAG = "Singleton_OperationValues";
    public static String DATA_OBJ_NAME = "Singleton_OperationValues";
    private static Singleton_OperationValues instance;

    public static void initInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new Singleton_OperationValues();
        }
    }


    
}
