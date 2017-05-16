package reso.examples.gobackn;

import java.util.Random;
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
    private int lostPercentage = 10;//pourcentage de perte de package.

    public GoBackNProtocol(IPHost host) {
        this.host = host;
    }

    public GoBackNProtocol(IPHost host, int lostPercentage) {
        this.host = host;
        this.lostPercentage = lostPercentage;
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        /*
         Cette partie verifie que le numero de sequence est celui attendu si c'est celui attendu renvoi le 
         ack sinon renvoi le ack du dernier numero recu.           
         */
        PayloadMessage msg = (PayloadMessage) datagram.getPayload();

        String sequenceSN = msg.getPayload().substring(0, 32);
        int sequenceNumber = Integer.parseInt(sequenceSN, 2);

        if (sequenceNumber == 0) {
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SenderProtocol, new AckMessage(0));//on fais ca separement parce que on n'a pas besoin du package test.
            this.actualSequenceNumber++;
        } else {
            int ack = 0;
            //gestion du ACK recu.
            //Message attendu
            if (sequenceChecker(sequenceNumber)) {
                ack = actualSequenceNumber;
                actualSequenceNumber = actualSequenceNumber + 1;
            }//mauvais ACK recu.
            else if (actualSequenceNumber != 0) {
                ack = actualSequenceNumber - 1;
            }
            AckMessage ackMessage = new AckMessage(ack);

            Random r = new Random();
            int pLP = r.nextInt(101);//tirage au sort d'un nombre entre 0 et 100 pour savoir si on perd le packet ou pas.
            if (pLP > this.lostPercentage) {
                System.out.println(" ");
                System.out.println("ACK (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)"
                        + " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst="
                        + datagram.dst + ", iif=" + src + ", counter=" + ack);
                host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_SenderProtocol, ackMessage);
                System.out.println(" ");
            }
        }
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
