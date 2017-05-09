package reso.examples.gobackn;

import reso.common.*;
import reso.ip.Datagram;
import reso.ip.IPInterfaceAdapter;
import reso.scheduler.AbstractScheduler;

public class Timer extends AbstractTimer {

    /**
     * Cette classe permet de creer un timer afin de suivre l evolution du
     * paquet sur le lien
     */

    private int number;
    private IPInterfaceAdapter src;
    private Datagram datagram;
    private SenderProtocol senderProtocol;

    private static double departTimer = 0;
    private static double arrivalTimer = 0;

    public Timer(AbstractScheduler scheduler, double interval, boolean repeat, int n, IPInterfaceAdapter src,
            Datagram datagram, SenderProtocol senderProtocol) {
        super(scheduler, interval, repeat);
        this.number = n;
        this.src = src;
        this.datagram = datagram;
        this.senderProtocol = senderProtocol;
    }

    @Override
    protected void run() throws Exception {
        if (number <= senderProtocol.getActualSequenceNumber()) {
            try {
                senderProtocol.sendPackageOutTimer(src, datagram);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public static double getArrivalTimer() {
        return arrivalTimer;
    }

    public static double getDepartTimer() {
        return departTimer;
    }

    public static void setDepartTimer(double newVal) {
        departTimer = newVal;
    }

    public static void setArrivalTimer(double newVal) {
        arrivalTimer = newVal;
    }

}
