package com.naver.error_reporting_sdk;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class ErrorHandler implements Thread.UncaughtExceptionHandler {
    static final String LOG_TAG = ErrorHandler.class.getSimpleName();

    private Thread.UncaughtExceptionHandler defaultHandler;
    private boolean handled;
    private Context context;

    ErrorHandler(Thread.UncaughtExceptionHandler defaultHandler, Context context) {
        this.defaultHandler = defaultHandler;
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            if(handled) {
                Log.w(LOG_TAG, "ExceptionHandler has been already executed.");
                return;
            }
            handled = true;

            if(e != null) {
                e.printStackTrace(pw);
            }
            String stackTrace = e != null ? sw.toString() : "";

            ReportInfo reportInfo = new ReportInfo.Builder(context)
                    .message(e != null ? e.toString() : "")
                    .stackTrace(stackTrace)
                    .build();

            Reporter.reportError(reportInfo);
        } catch(Exception tr) {
            Reporter.log.e(LOG_TAG, "Error has occurred while reporting exception. : " + tr.getMessage());
        } finally {
            defaultHandler.uncaughtException(t, e);
            try {
                sw.close();
                pw.close();
            } catch(IOException ex) {
                Reporter.log.e(LOG_TAG, "Error has occurred while closing StringWriter. : " + ex.getMessage());
            }
        }
    }
}
