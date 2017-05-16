/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import java.util.ArrayList;
import java.util.Random;
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
    private int lostPercentage = -1;
    private static int ssTresh = 200;
    private ArrayList<Integer> packageToSend = new ArrayList<Integer>();

    public Sender(IPHost host, IPAddress dst, int numberOfPackage) {
        super(host, "sender");
        this.dst = dst;
        ip = host.getIPLayer();
        this.numberOfPackage = numberOfPackage;
    }

    public Sender(IPHost host, IPAddress dst, int numberOfPackage, int lostPercentage) {
        super(host, "sender");
        this.dst = dst;
        ip = host.getIPLayer();
        if (numberOfPackage > 0) {
            this.numberOfPackage = numberOfPackage;
        }
        this.lostPercentage = lostPercentage;
    }

    public Sender(IPHost host, IPAddress dst, int numberOfPackage, int lostPercentage, int ssTresh) {
        super(host, "sender");
        this.dst = dst;
        ip = host.getIPLayer();
        if (numberOfPackage > 0) {
            this.numberOfPackage = numberOfPackage;
        }
        this.lostPercentage = lostPercentage;
        this.ssTresh = ssTresh;
    }

    public void start()
            throws Exception {
        Random r = new Random();
        for (int i = 1; i < numberOfPackage + 1; i++) {
            packageToSend.add(r.nextInt(1001));
        }
        SenderProtocol senderProtocol;
        if (lostPercentage == -1) {
            senderProtocol = new SenderProtocol((IPHost) host, ssTresh);
        } else {
            senderProtocol = new SenderProtocol((IPHost) host, ssTresh, lostPercentage);
        }
        ip.addListener(SenderProtocol.IP_PROTO_SenderProtocol, senderProtocol);
        senderProtocol.launch(dst);
        senderProtocol.addMessageTosend(packageToSend);
        senderProtocol.end();

    }

    public void stop() {
    }

}
