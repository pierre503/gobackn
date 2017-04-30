/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import java.util.ArrayList;
import static reso.examples.gobackn.GoBackNProtocol.IP_PROTO_GoBackN;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

/**
 *
 * @author pierre
 */
public class SenderProtocol
        implements IPInterfaceListener {

    public static final int IP_PROTO_SenderProtocol = Datagram.allocateProtocolNumber("Sender");

    private final IPHost host;
    private int actualSequenceNumber = 0;
    private static ArrayList<PayloadMessage> packageToSend = new ArrayList<PayloadMessage>();
    private int cursorSenderWindow = 1;
    private int sizeOfWindow = 5;//taille de la fenetre d'envoi

    public SenderProtocol(IPHost host, int numberOfPackage) {
        this.host = host;
        for (int i = 0; i < numberOfPackage; i++) {
            packageToSend.add(new PayloadMessage(i));
        }
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        AckMessage msg = (AckMessage) datagram.getPayload();
        int sequenceN = msg.getPayload();
        /*cette partie verifie si on obtient le numero voulu.
                Si on obtient le numero voulu on avance d'un element la fenetre,si le numero est plus grand on "saute" jusque ce numero la car on est en go-back-n et si on a un numero plus petit on renvoi la fenetre actuel.
         */
        if (sequenceN >= this.actualSequenceNumber) {
            if (sequenceN > this.actualSequenceNumber) {
                cursorSenderWindow -= sequenceN - this.actualSequenceNumber ;
                this.actualSequenceNumber = sequenceN + 1;
            }else{
                this.actualSequenceNumber += 1;
                cursorSenderWindow -= 1;
            }/*
            Cette partie envoi le nombre d'elements contenu dans la fenetre actuel et qui n'ont pas encore ete envoye
            */
            int numberOfPackageToSend = sizeOfWindow-cursorSenderWindow;
            for(int i = 0;i < numberOfPackageToSend;i++){
                if(i+actualSequenceNumber < this.packageToSend.size()){
                    System.out.println("Sender of Message (" + (int) (host.getNetwork().getScheduler().getCurrentTime()*1000) + "ms)" +
				" host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
				datagram.dst + ", iif=" + src + ", counter=" + (cursorSenderWindow+actualSequenceNumber));
                    host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN, this.packageToSend.get(cursorSenderWindow+actualSequenceNumber));
                }
                cursorSenderWindow += 1;
            }
        }else {

        }
    }
    
    public static ArrayList<PayloadMessage> getPackageToSend(){
        return packageToSend;
    }

}
