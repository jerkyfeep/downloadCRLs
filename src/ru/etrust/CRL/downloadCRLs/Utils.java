package ru.etrust.CRL.downloadCRLs;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.etrust.CRL.CAObjects.CAInfo;
import ru.etrust.CRL.CAObjects.CertificateInfo;
import ru.etrust.CRL.CAObjects.ComplexInfo;
import ru.etrust.CRL.CAObjects.KeyInfo;

import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by AMironenko on 30.10.2014.
 */
public class Utils {
    private final static Logger LOGGER = Logger.getLogger(Utils.class.getName());

    protected static String convertCertToPem(X509Certificate cert) throws CertificateEncodingException {
//        Base64 encoder = new Base64(64);
        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";

        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String(DatatypeConverter.printBase64Binary(derCert));
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }
    protected static String convertCrlToPem(X509CRL crl) throws CertificateEncodingException, CRLException {
//        Base64 encoder = new Base64(64);
        String crl_begin = "-----BEGIN X509 CRL-----\n";
        String end_crl = "-----END X509 CRL-----";

        byte[] derCrl = crl.getEncoded();
        String pemCertPre = new String(DatatypeConverter.printBase64Binary(derCrl));
        String pemCert = crl_begin + pemCertPre + end_crl;
        return pemCert;
    }

    public static void printThreadError(Exception e, String message) {
        PrintWriter writer = new PrintWriter(System.err);
        writer.print(Thread.currentThread().getName() + "\tError: " + message + "\terror: ");
        e.printStackTrace(writer);
//        writer.println(Thread.currentThread().getName() + " fetching " + url + " failed!");
        writer.flush();
    }

    public static void printThreadError(String error) {
        PrintWriter writer = new PrintWriter(System.err);
        writer.println(Thread.currentThread().getName() + " " + error);
        writer.flush();
    }

    public static void printThreadInfo(String info) {
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(Thread.currentThread().getName() + " info: " + info);
        writer.flush();
    }
    /*public static ArrayList<CAData> parseCRLAddresses(String xmlurl) throws Exception{
        DocumentBuilderFactory factory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;
        Node certificatesNode = null;
        ArrayList<CAData> caDataList = new ArrayList<CAData>();

        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            docBuilder = factory.newDocumentBuilder();
            URL url = new URL(xmlurl);
            doc = docBuilder.parse(url.openStream());

            NodeList addressesList = doc.getElementsByTagName("АдресаСписковОтзыва");
            for (int i = 0; i<addressesList.getLength(); i++ ) {
                Node addresses = addressesList.item(i);
                NodeList crlAddressesList = addresses.getChildNodes();

                //ArrayList<String> crlUrls = new ArrayList<String>();
                CAData caData = new CAData();
                for (int j = 0; j<crlAddressesList.getLength(); j++ ) {
                    Node crlAddress = crlAddressesList.item(j);
                    if(crlAddress.getNodeType() == Node.TEXT_NODE) continue;
                    //crlUrls.add(crlAddress.getTextContent());
                    caData.CrlUrls.add(crlAddress.getTextContent());
                }
                //CRLs.add(crlUrls);

*//**
 * Вот тут начинаем вынимать сертификаты из окружения Адресов Списков Отзыва
 *//*
                certificatesNode = addresses;
                while ((certificatesNode = certificatesNode.getNextSibling())!=null) {
                    if (certificatesNode.getNodeName().equals("Сертификаты")) break;

                }
                if (certificatesNode==null)
                    LOGGER.warning("No certificates for CRL");
                NodeList certificateNodeList = certificatesNode.getChildNodes();
                for (int j = 0; j < certificateNodeList.getLength(); j++) {
                    Node certificateNode = certificateNodeList.item(j);
                    if(certificateNode.getNodeType() == Node.TEXT_NODE) continue;
                    Node certificateDataNode = null;
                    Node certificateDataNodeLookup = certificateNode.getFirstChild();
                    while (!certificateDataNodeLookup.getNodeName().equals("Данные") && certificateDataNodeLookup!=null) {
                        certificateDataNodeLookup = certificateDataNodeLookup.getNextSibling();
                    }
                    if (certificateDataNodeLookup == null)
                        LOGGER.warning("No certificate for CRL");
                    certificateDataNode = certificateDataNodeLookup;
                    X509Certificate crossCert = X509Certificate.getInstance(DatatypeConverter.parseBase64Binary(certificateDataNode.getTextContent()));
                    caData.CACerts.add(crossCert);
                }
                caDataList.add(caData);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        finally {
            return caDataList;
        }
    }*/

    public static List<CAInfo> parseCRLAddresses(Object xmlurl) throws Exception {

        DocumentBuilderFactory factory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;
        Node certificatesNode = null;

        List<CAInfo> caInfoList = new ArrayList<CAInfo>();

        factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        docBuilder = factory.newDocumentBuilder();

        //URL url = new URL(xmlurl);

        if (xmlurl instanceof  URL) {
            doc = docBuilder.parse(((URL) xmlurl).openStream());
        } else if (xmlurl instanceof File) {
            doc = docBuilder.parse((File)xmlurl);
        }


        XPath xPath =  XPathFactory.newInstance().newXPath();
        NodeList caNodeList = (NodeList)xPath.compile("/АккредитованныеУдостоверяющиеЦентры/УдостоверяющийЦентр").evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i<caNodeList.getLength(); i++ ) {
            CAInfo caInfo = new CAInfo();
            Node caNode = caNodeList.item(i);
            String CAId = (String)xPath.compile("Название").evaluate(caNode,XPathConstants.STRING);
            caInfo.setCAId(CAId);
            NodeList complexNodeList = (NodeList) xPath.compile("ПрограммноАппаратныеКомплексы/ПрограммноАппаратныйКомплекс").evaluate(caNode, XPathConstants.NODESET);
            for (int j = 0; j<complexNodeList.getLength(); j++) {
                Node complexNode = complexNodeList.item(j);
                ComplexInfo complexInfo = new ComplexInfo();
                String complexAlias = (String)xPath.compile("Псевдоним").evaluate(complexNode, XPathConstants.STRING);
                complexInfo.setComplexName(complexAlias);
                NodeList keysNodeList = (NodeList) xPath.compile("КлючиУполномоченныхЛиц/Ключ").evaluate(complexNode, XPathConstants.NODESET);
                for (int n = 0; n<keysNodeList.getLength(); n++) {
                    Node keyNode = keysNodeList.item(n);
                    KeyInfo keyInfo = new KeyInfo();
                    String keyId = (String) xPath.compile("ИдентификаторКлюча").evaluate(keyNode, XPathConstants.STRING);
                    keyInfo.setKeyId(keyId);
                    NodeList crlUrlList = (NodeList) xPath.compile("АдресаСписковОтзыва/Адрес").evaluate(keyNode, XPathConstants.NODESET);
                    for (int m = 0; m<crlUrlList.getLength(); m++) {
                        Node crlUrlNode = crlUrlList.item(m);
                        String crlUrl = crlUrlNode.getTextContent();
                        keyInfo.CRLUrlList.add(crlUrl);
                    }
                    NodeList certificateDataNodeList = (NodeList)xPath.compile("Сертификаты/ДанныеСертификата").evaluate(keyNode, XPathConstants.NODESET);
                    for (int m = 0; m<certificateDataNodeList.getLength(); m++) {
                        Node certificateDataNode = certificateDataNodeList.item(m);
                        CertificateInfo certInfo = new CertificateInfo();
                        String Serial = (String)xPath.compile("СерийныйНомер").evaluate(certificateDataNode, XPathConstants.STRING);
                        String certData = (String)xPath.compile("Данные").evaluate(certificateDataNode, XPathConstants.STRING);
                        certInfo.setSerial(new BigInteger(Serial, 16));
                        certInfo.setBase64CertData(certData);
                        keyInfo.CertificateInfoList.add(certInfo);
                    }
                    complexInfo.KeyInfoList.add(keyInfo);
                }
                caInfo.ComplexInfoList.add(complexInfo);
            }
            caInfoList.add(caInfo);
            System.out.println("CA " + caInfo.getCAId() + " added.");
        }


        return caInfoList;
    }

    public static Boolean checkDirectoryIsWriteable(String path) {
        File f = new File(path + "/" + String.valueOf(UUID.randomUUID()));
        Boolean answer = false;
        try {
            if (f.createNewFile())
                f.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Boolean cleanDirectory(String path) {
        try {
            FileUtils.cleanDirectory(new File(path));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static String getAbsolutePath (String path) {
        return (new File(path)).getAbsolutePath();
    }

}
