package reso.examples.gobackn;

import static reso.examples.gobackn.SenderProtocol.IP_PROTO_SenderProtocol;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class GoBackNProtocol
        implements IPInterfaceListener {

    public static final int IP_PROTO_GoBackN = Datagram.allocateProtocolNumber("GoBackN");

    private final IPHost host;
    private int actualSequenceNumber = 0;

    public GoBackNProtocol(IPHost host) {
        this.host = host;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        /*
         Cette partie verifie que le numero de sequence est celui attendu si c'est celui attendu renvoi le ack sinon renvoi le ack du dernier numero recu.           
         */
        PayloadMessage msg = (PayloadMessage) datagram.getPayload();

        String sequenceSN = msg.getPayload().substring(0, 32);

        int sequenceNumber = Integer.parseInt(sequenceSN, 2);
        int ack = 0;
        if (sequenceChecker(sequenceNumber)) {
            ack = actualSequenceNumber;
            actualSequenceNumber = actualSequenceNumber + 1;
        } else if (actualSequenceNumber != 0) {
            ack = actualSequenceNumber - 1;
        }
        AckMessage ackMessage = new AckMessage(ack);
        System.out.println("ACK (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)"
                + " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst="
                + datagram.dst + ", iif=" + src + ", counter=" + ack);
        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SenderProtocol, ackMessage);

        System.out.println(" ");
    }

    /**
     * Cette methode verifie si le numero de sequence envoye est celui voulu.
     *
     * @return true si le numero de sequence recu est le numero voulu, false
     * sinon.
	*
     */
    public boolean sequenceChecker(int sequenceNumber) {
        return actualSequenceNumber == sequenceNumber;
    }

}
