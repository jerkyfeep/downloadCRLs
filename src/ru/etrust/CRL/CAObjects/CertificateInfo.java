package ru.etrust.CRL.CAObjects;

import java.math.BigInteger;

/**
 * Created by AMironenko on 10.12.2014.
 */
public class CertificateInfo {
    private String base64CertData;
    private BigInteger serial;

    public String getBase64CertData() {
        return base64CertData;
    }

    public void setBase64CertData(String base64CertData) {
        this.base64CertData = base64CertData;
    }

    public BigInteger getSerial() {
        return serial;
    }

    public void setSerial(BigInteger serial) {
        this.serial = serial;
    }
}
