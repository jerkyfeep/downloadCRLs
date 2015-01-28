package ru.etrust.CRL.downloadCRLs;

import ru.etrust.CRL.CAObjects.CAInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AMironenko on 29.10.2014.
 */

public class Main {

    static String xmlurl = "http://e-trust.gosuslugi.ru/CA/DownloadTSL?schemaVersion=0";
//    static String xmlurl = "D:\\tmp\\etrust\\TSLExt.1.0.xml";
    private static String crlOutputDirectory;
    private static String crtOutputDirectory;
    private static String unverifiedCRLOutputDirectory;

    private int numberOfThreads = 5;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: downloadCRLs <crlOutputDirectory> <crtOutputDirectory> <unverifiedCRLOutputDirectory>");
            return;
        }
        crlOutputDirectory = args[0];
        crtOutputDirectory = args[1];
        unverifiedCRLOutputDirectory = args[2];

//          проверяем возможность записи в директории
        if (!Utils.checkDirectoryIsWriteable(Utils.getAbsolutePath(crlOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(crlOutputDirectory) + " is not writable!");
            return;
        }
        if (!Utils.checkDirectoryIsWriteable(Utils.getAbsolutePath(crtOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(crtOutputDirectory) + " is not writable!");
            return;
        }
        if (!Utils.checkDirectoryIsWriteable(Utils.getAbsolutePath(unverifiedCRLOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(unverifiedCRLOutputDirectory) + " is not writable!");
            return;
        }
//        Очищаем директории
        if (!Utils.cleanDirectory(Utils.getAbsolutePath(crlOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(crlOutputDirectory) + " couldn't be cleared!");
            return;
        }
        if (!Utils.cleanDirectory(Utils.getAbsolutePath(crtOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(crtOutputDirectory) + " couldn't be cleared!");
            return;
        }
        if (!Utils.cleanDirectory(Utils.getAbsolutePath(unverifiedCRLOutputDirectory))) {
            System.err.println("Error: directory " + Utils.getAbsolutePath(unverifiedCRLOutputDirectory) + " couldn't be cleared!");
            return;
        }


        int count = 0;
//        ArrayList<List<String>> CRLs = new ArrayList<List<String>>();
//        ArrayList<CAData> caDataList = null;
        List<CAInfo> caInfoList = new ArrayList<CAInfo>();

        long startTime = new Date().getTime();

        try {
            caInfoList = Utils.parseCRLAddresses(new URL(xmlurl));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to parse XML");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (CAInfo caInfo : caInfoList ) {
            count++;
            Runnable worker = new WorkerThread(caInfo, crlOutputDirectory, crtOutputDirectory, unverifiedCRLOutputDirectory);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
        long finishTime = new Date().getTime();
        System.out.println(String.valueOf(count) +" CAs handled in " + String.valueOf((finishTime - startTime)/1000) + " seconds.");
    }
}
