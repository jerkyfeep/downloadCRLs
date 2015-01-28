package ru.etrust.CRL.downloadCRLs;

import javax.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMironenko on 05.12.2014.
 */
public class CAData {

    public String caId = "default CA ID";
    public List<String> CrlUrls = new ArrayList<String>();
    public List<X509Certificate> CACerts = new ArrayList<X509Certificate>();
}
