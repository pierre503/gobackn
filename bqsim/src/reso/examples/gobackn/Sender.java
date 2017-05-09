/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

/**
 *
 */
public class Sender extends AbstractApplication {

    private final IPLayer ip;
    private final IPAddress dst;
    private static int numberOfPackage;

    public Sender(IPHost host, IPAddress dst, int numberOfPackage) {
        super(host, "sender");
        this.dst = dst;
        ip = host.getIPLayer();
        this.numberOfPackage = numberOfPackage;
    }

    public void start()
            throws Exception {
        SenderProtocol senderProtocol = new SenderProtocol((IPHost) host, numberOfPackage);
        ip.addListener(SenderProtocol.IP_PROTO_SenderProtocol, senderProtocol);
        senderProtocol.launch(dst);
        
    }

    public void stop() {
    }

}
