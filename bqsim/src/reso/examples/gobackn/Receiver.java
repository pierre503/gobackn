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

    public Receiver(IPHost host) {
        super(host, "receiver");
        ip = host.getIPLayer();
    }

    public void start() {
        ip.addListener(GoBackNProtocol.IP_PROTO_GoBackN, new GoBackNProtocol((IPHost) host));
    }

    public void stop() {
    }
}
