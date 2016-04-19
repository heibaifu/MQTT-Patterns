package com.patterns.io.thingsflow;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
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

//import com.google.android.gms.security.ProviderInstaller;
;
//import com.google.android.gms.security.ProviderInstaller;
/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    //public SendMQTT             mqttSender;


    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.devicefragment, menu);
        //getMenuInflater().inflate(R.menu.devicefragment, menu);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ///////////////  ALL THIS IS   I/O    MQTT STUFF //////////////////////////////////////////
        // Find the listView by its ID
        Button buttonMQTT = (Button) rootView.findViewById(R.id.buttonMQTT);

        buttonMQTT.setOnClickListener(onClickListenerMQTT);



        //return inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }


    private OnClickListener onClickListenerMQTT = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.buttonMQTT:
                    //Handle Button click
                    SendMQTT mqttSender = new SendMQTT();
                    mqttSender.execute("simon,77");
                    break;
            }
        }
    };

    //The AsyncTask is called with <Params, Progress, Result>
    public class SendMQTT extends AsyncTask<String, Void, String[]> {

        public MqttAndroidClient    client;
        public Context              context;
        public MqttMessage          message;

        public String               content     = "Mensaje Tiburon";
        public String               topic       = "MQTT Examples";

        @Override
        protected String[] doInBackground(String... paramString) {
            Log.d("Things Flow", "Started Async Task");

            /*
            TODO: The construction of the MQTTClient, and the URI are incomplete, need to check TCP address
            In the constructor "context" was substituted by "this"
            */

            String connectionURI = "ssl://A33DKVX6YQAT9A.iot.us-west-2.amazonaws.com:8883";
            //String connectionURI = "tcp://broker.mqttdashboard.com:1883";
            String topic                = "MQTT/SHARK";
            String content              = "TIBURON TIBURON TIBURON TIBURON";
            int qos                     = 0;
            String broker               = connectionURI;
            String clientId             = "JavaSample";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);

                ///////////////////////////////////////////////////////////////////////////////////
                /*Properties SSLprops = new Properties();
                SSLprops.setProperty("com.ibm.ssl.protocol","TLSv1");
                connOpts.setSSLProperties(SSLprops);*/
                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);
                options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

                InputStream caCert      = getActivity().getResources().openRawResource(R.raw.root);
                InputStream clientCert  = getActivity().getResources().openRawResource(R.raw.certificate);
                InputStream privateKey  = getActivity().getResources().openRawResource(R.raw.privatekey);

                Log.d("Things Flow - I/O", "Checkpoint 1");
                options.setSocketFactory(getSocketFactory("caFilePath", "clientCrtFilePath", "clientKeyFilePath", "123456", caCert, clientCert, privateKey));
                //////////////////////////////////////////////////////////////////////////////////
                Log.d("Things Flow - I/O", "Checkpoint 2");
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(options);
                System.out.println("Connected");
                System.out.println("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
                //System.exit(0);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            } catch (Exception e) {
                Log.d("Things Flow I/O", "Error " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if(result != null){

            }
        }
    }

    private SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile, final String keyFile,
                                              final String password, final InputStream caFileStream,
                                              final InputStream clientCertStream,final InputStream clientPrivateStream) throws Exception
    {


        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        ///////////////////////////////////////////////////////////////////////////////////////////
        Security.addProvider(new BouncyCastleProvider());
        ///////////////////////////////////////////////////////////////////////////////////////////
        File caFile                = new File(caCrtFile);
        File clientCertFile        = new File(crtFile);
        File clientPrivateFile     = new File(keyFile);

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
        //KeyStore caKs = KeyStore.getInstance("PKCS11");
        //Activity act = getActivity();
        caKs.load(null,null);
        //caKs.load(getActivity().getResources().openRawResource(R.raw.root),password.toCharArray());
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
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        //InetAddress thisIp = InetAddress.getByName("A33DKVX6YQAT9A.iot.us-west-2.amazonaws.com");
        String host = "A33DKVX6YQAT9A.iot.us-west-2.amazonaws.com";
        SSLSocket sslSocket = (SSLSocket)context.getSocketFactory().createSocket(host, 8883);
        sslSocket.setEnabledProtocols(new String[] {"TLSv1.2"} );

        //sslSocket.setEnabledCipherSuites(new String[]{"AES256-SHA"});

        //AES128-SHA
        //ECDHE-RSA-AES128-SHA
        //((SSLServerSocket)serverSocket).setEnabledCipherSuites(context.getServerSocketFactory().getSupportedCipherSuites());
        ///////////////////////////////////////////////////////////////////////////////////////////
        return context.getSocketFactory();
    }



}
