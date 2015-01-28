package ru.etrust.CRL.CAObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMironenko on 10.12.2014.
 */
public class KeyInfo {
    private String KeyId;
    public List<String> CRLUrlList = new ArrayList<String>();
    public List<CertificateInfo> CertificateInfoList = new ArrayList<CertificateInfo>();

    public String getKeyId() {
        return KeyId;
    }

    public void setKeyId(String keyId) {
        KeyId = keyId;
    }

}
