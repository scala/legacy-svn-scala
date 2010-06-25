package com.example.android.wiktionary;

import android.os.AsyncTask;

/**
 * Temporary workaround to solve a Scala compiler issue which shows up
 * at runtime with the error message
 * "java.lang.AbstractMethodError: abstract method not implemented"
 * for the missing method LookupTask.doInBackground(String... args).
 *
 * Our solution: the Java method doInBackground(String... args) forwards
 * the call to the Scala method doInBackground1(String[] args).
 */
public abstract class MyAsyncTask extends AsyncTask<String, String, String> {

    protected abstract String doInBackground1(String[] args);

    @Override
    protected String doInBackground(String... args) {
        String[] args1 = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            args1[i] = args[i];
        }
        return doInBackground1(args1);
    }

}

