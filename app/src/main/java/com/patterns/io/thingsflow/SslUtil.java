package com.patterns.io.thingsflow;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class SslUtil
{

    static SSLSocketFactory getSocketFactory ( final String password, final String brokerString, final String brokerPort, final InputStream caFileStream,
                                                final InputStream clientCertStream,final InputStream clientPrivateStream) throws Exception
    {


        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        ///////////////////////////////////////////////////////////////////////////////////////////
        //////////// INPUT STREAMS                            /////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////
        Security.addProvider(new BouncyCastleProvider());

        int caSize              = caFileStream.available();
        int clientCertSize      = clientCertStream.available();
        int clientPrivateSize   = clientPrivateStream.available();

        byte[] caCertBytes          = new byte[caSize];
        byte[] clientCertBytes      = new byte[clientCertSize];
        byte[] clientPrivateBytes   = new byte[clientPrivateSize];

        try {
            BufferedInputStream caBuf               = new BufferedInputStream(caFileStream);
            BufferedInputStream clientCertFileBuf   = new BufferedInputStream(clientCertStream);
            BufferedInputStream clientPrivateBuf    = new BufferedInputStream(clientPrivateStream);


            caBuf            .read(caCertBytes,        0, caCertBytes       .length);
            clientCertFileBuf.read(clientCertBytes,    0, clientCertBytes   .length);
            clientPrivateBuf .read(clientPrivateBytes, 0, clientPrivateBytes.length);

            caBuf            .close();
            clientCertFileBuf.close();
            clientPrivateBuf .close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ///////////////////////////////////////////////////////////////////////////////////////////

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Load CA certificate
        PEMParser reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(caCertBytes  )));
        X509Certificate caCert = new JcaX509CertificateConverter().setProvider( "BC" )
                .getCertificate((X509CertificateHolder) reader.readObject());
        reader.close();

        // Load client certificate
        reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(clientCertBytes  )));
        X509Certificate cert = new JcaX509CertificateConverter().setProvider( "BC" )
                .getCertificate((X509CertificateHolder) reader.readObject());
        reader.close();

        // Load client private key
        reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(clientPrivateBytes )));
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");
        KeyPair key = keyConverter.getKeyPair((PEMKeyPair) reader.readObject());
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null,null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        //context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        context.init(null, tmf.getTrustManagers(), null);

        int port = Integer.valueOf(brokerPort);
        SSLSocket sslSocket = (SSLSocket)context.getSocketFactory().createSocket(brokerString, port);
        sslSocket.setEnabledProtocols(new String[] {"TLSv1.2"} );
        ///////////////////////////////////////////////////////////////////////////////////////////
        return context.getSocketFactory();
    }
}
