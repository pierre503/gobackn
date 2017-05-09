/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

/**
 *
 */
public class Receiver extends AbstractApplication {

    private final IPLayer ip;
    private int lostPercentage = -1;

    public Receiver(IPHost host) {
        super(host, "receiver");
        ip = host.getIPLayer();
    }
    
        public Receiver(IPHost host, int lostPercentage) {
        super(host, "receiver");
        ip = host.getIPLayer();
        this.lostPercentage = lostPercentage;
    }

    public void start() {
        GoBackNProtocol goBackNProtocol;
        if(lostPercentage == -1){
            goBackNProtocol = new GoBackNProtocol((IPHost) host);
        }else{
            goBackNProtocol = new GoBackNProtocol((IPHost) host, lostPercentage);
        }
        ip.addListener(GoBackNProtocol.IP_PROTO_GoBackN, goBackNProtocol);
    }

    public void stop() {
    }
}
