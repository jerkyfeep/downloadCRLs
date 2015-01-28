package ru.etrust.CRL.downloadCRLs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import ru.etrust.CRL.CAObjects.CAInfo;
import ru.etrust.CRL.CAObjects.CertificateInfo;
import ru.etrust.CRL.CAObjects.ComplexInfo;
import ru.etrust.CRL.CAObjects.KeyInfo;

import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.UUID;

/**
 * Created by AMironenko on 29.10.2014.
 */
public class WorkerThread implements Runnable{

    private String crlOutputDirectory;
    private String crtOutputDirectory;
    private String unverifiedCRLOutputDirectory;
//    private CAData caData = null;
    private CAInfo caInfo = new CAInfo();

//    public WorkerThread(String s){
//        this.command=s;
//    }

    public WorkerThread(CAInfo caInfo, String crlOutputDirectory, String crtOutputDirectory, String unverifiedCRLOutputDirectory) {
        this.caInfo = caInfo;
        this.crlOutputDirectory = crlOutputDirectory;
        this.crtOutputDirectory = crtOutputDirectory;
        this.unverifiedCRLOutputDirectory = unverifiedCRLOutputDirectory;
    }

    /**
     * Запускает поток. Потом пытается слить сначала первый файл из списка, если не получается - то второй и т.д.
     */
    @Override
    public void run() {
        for (ComplexInfo complexInfo : caInfo.ComplexInfoList) {
            for (KeyInfo keyInfo: complexInfo.KeyInfoList) {
                Boolean crlLoaded = false;
                for (String crlUrl : keyInfo.CRLUrlList) {
                    Boolean crlChecked = false;
                    String fn = null;
                    try {
                        fn = saveFile(crlUrl);
                        InputStream ins = new FileInputStream(fn);
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        X509CRL crl = (X509CRL) cf.generateCRL(ins);
                        ins.close();
                        crlLoaded = true;
                        X509Certificate signerCACert = null;
                        for (CertificateInfo certificateInfo : keyInfo.CertificateInfoList) {
                            try {
                                X509Certificate caCert = null;
                                caCert = X509Certificate.getInstance(DatatypeConverter.parseBase64Binary(certificateInfo.getBase64CertData()));
                                caCert.checkValidity();
                                crl.verify(caCert.getPublicKey());
                                signerCACert = caCert;
                                crlChecked = true;
                            } catch (javax.security.cert.CertificateExpiredException e) {
//                                   умерший сертификат, ничего не делаем
                            } catch (CertificateException e) {
                                Utils.printThreadError(e, "Cert parsing error. KeyId: " + keyInfo.getKeyId() + ". Cert serial: " + certificateInfo.getSerial());
                            } catch (NoSuchProviderException e) {
//                                не можем разобрать вообще весь CRL, удаляем файл, кидаем исключение выше
                                FileUtils.forceDelete(new File(fn));
                                throw e;
                            } catch (NoSuchAlgorithmException e) {
//                                нет алгоритма, удаляем файл, кидаем исключение выше
                                FileUtils.forceDelete(new File(fn));
                                throw e;
                            } catch (InvalidKeyException e) {
                                Utils.printThreadError(e, "Cert error: invalid public key. KeyId: " + keyInfo.getKeyId() + ". Cert serial: " + certificateInfo.getSerial());
                            } catch (SignatureException e) {
//                                а тут просто не получилось проверить конкретным сертификатом конкретный CRL
                            }
                            if (crlChecked) break;
                        }
                        if (!crlChecked) {
                            Utils.printThreadError("No correct certificate for CRL: " + crlUrl);
//                            FileUtils.moveFileToDirectory(new File(fn), new File(this.unverifiedCRLOutputDirectory), false);
                            FileUtils.forceDelete(new File(fn));
                        }
                        else {
                            try {
                                String certFileName = FilenameUtils.getBaseName(fn).concat(".crt");
                                FileOutputStream fos = new FileOutputStream(this.crtOutputDirectory + "\\" + certFileName);
                                fos.write(signerCACert.getEncoded());
                                fos.close();
                                FileUtils.moveFileToDirectory(new File(fn), new File(this.crlOutputDirectory),false);
                                break;
                            } catch (CertificateEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (NoSuchAlgorithmException e) {
                        Utils.printThreadError(e, "Can't validate CRL: no such provider. KeyId: " + keyInfo.getKeyId() + ". CRL url: " + crlUrl);

                    } catch (NoSuchProviderException e) {
                            Utils.printThreadError(e, "CRL validation error: no such provider. KeyId: " + keyInfo.getKeyId() + ". CRL url: " + crlUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (java.security.cert.CertificateException e) {
                        Utils.printThreadError(e, "Can't parse CRL. KeyId: " + keyInfo.getKeyId() + ". CRL url: " + crlUrl);
                    } catch (CRLException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (!crlLoaded) {
                            Utils.printThreadInfo("No CRLs loaded for KeyID: " + keyInfo.getKeyId());
                            if (fn != null) try {
                                FileUtils.forceDelete(new File(fn));
                            } catch (IOException e) {
//                                e.printStackTrace();
                                Utils.printThreadError(e, "Couldn't remove file " + fn);
                            }
                        }
                    }


                }

            }
        }
    }


//    @Override
//    public void run() {
//        int failed = 0;
//        int totally = caInfo.ComplexInfoList.CrlUrls.size();
//        for (String url:caData.CrlUrls) {
//            try {
////              сохраняем CRL на файловую систему
//
//                String fn = saveFile(url);
//                InputStream inStream = null;
//                Boolean crlChecked = false;
//                X509Certificate signerCACert = null;
//
////              пытаемся провалидировать CRL с помощью сертификата
//                try {
//                    inStream = new FileInputStream(fn);
//
//                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                    X509CRL crl = (X509CRL)cf.generateCRL(inStream);
//
//                    for (X509Certificate caCert:caData.CACerts) {
//                        try {
//                            caCert.checkValidity();
//                            crl.verify(caCert.getPublicKey());
//                            Utils.printThreadInfo("CRL " + url + " verified using certificate with SN: " + String.format("%X", caCert.getSerialNumber()));
//                            crlChecked = true;
//                            signerCACert = caCert;
//                            break;
//
//                        } catch (CertificateExpiredException e) {
////                            тут ничего не делаем
//                        } catch (NoSuchAlgorithmException e) {
//                            e.printStackTrace();
//                        } catch (InvalidKeyException e) {
//                            e.printStackTrace();
//                        } catch (NoSuchProviderException e) {
//                            e.printStackTrace();
//                        } catch (SignatureException e) {
////                              здесь ничего не делаем, т.к. возможно мы проверяли не тем сертификатом
////                            Utils.printThreadError(e, fn);
//                            Utils.printThreadError("CRL " + url + " was not verified using certificate with SN: " + String.format("%X", caCert.getSerialNumber()));
//                        }
//                    }
//                    if (crlChecked) {
//                        String certFileName = FilenameUtils.getBaseName(fn).concat(".crt");
//
//                        FileOutputStream fos = new FileOutputStream(this.crtOutputDirectory + "\\" + certFileName);
//                        fos.write(signerCACert.getEncoded());
//                        fos.close();
//                    }
//                    else {
//                        Utils.printThreadError("No correct certificate for CRL: " + url);
//                        if (inStream != null) { inStream.close(); }
//                        FileUtils.moveFileToDirectory(new File(fn), new File(this.unverifiedCRLOutputDirectory), false);
////                        FileUtils.moveFileToDirectory(new File);
//                    }
//
//                }
//                catch (CertificateException e) {
//                    Utils.printThreadError(e, fn);
//                }
//                catch (CRLException e) {
//                    Utils.printThreadError(e, fn);
//                    if (inStream != null) { inStream.close(); }
//                    FileUtils.moveFileToDirectory(new File(fn), new File(this.unverifiedCRLOutputDirectory), false);
//                }
//                catch (java.security.cert.CertificateException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (inStream != null) {
//                        inStream.close();
//                    }
//                }
//
//                System.out.println(Thread.currentThread().getName() + " fetching " + url + " done!");
//                break;
//            }
//            catch (MalformedURLException e) {
//                Utils.printThreadError(e, url);
//                failed++;
//                continue;
//            }
//            catch (IOException e) {
//                Utils.printThreadError(e, url);
//                failed++;
//                continue;
//            }
//
//        }
//        if (failed == totally)
//            System.err.println("No valid CRLs found: " + caData.CrlUrls.get(0));
//
//    }

    /**
     * Запускает сохранение данных напрямую из потока в файл на диске
     * @param url
     * @throws MalformedURLException
     * @throws IOException
     */
    private String saveFile(String url) throws MalformedURLException, IOException {
        URL website = new URL(url);

        String filename = FilenameUtils.getBaseName(url) + UUID.randomUUID().toString().concat(".crl");

//        елси адрес CDP задан как http://cdp.ru/ , то нужно сгенерить уникальное имя
//        if (filename.length()==0)
//            filename = UUID.randomUUID().toString().concat(".crl");
        filename = this.unverifiedCRLOutputDirectory + "\\" + filename;

        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(filename);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        rbc.close();
        fos.close();
        return filename;
    }

}

