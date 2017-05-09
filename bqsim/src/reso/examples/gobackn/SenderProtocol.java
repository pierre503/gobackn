/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import java.util.ArrayList;
import java.util.Random;
import static reso.examples.gobackn.GoBackNProtocol.IP_PROTO_GoBackN;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;

/**
 *
 */
public class SenderProtocol
        implements IPInterfaceListener {

    public static final int IP_PROTO_SenderProtocol = Datagram.allocateProtocolNumber("Sender");

    private final IPHost host;
    private static int actualSequenceNumber = -1;//numero de sequence attendu.
    private static ArrayList<PayloadMessage> packageToSend = new ArrayList<PayloadMessage>();//liste des package a envoyer.
    private int cursorSenderWindow = 1;//position du curseur dans la fenetre.

    // private static boolean testTimer=false;
    private int sizeOfWindow = 1;//taille de la fenetre d'envoi
    private int ssTresh = 5;
    private int numberOfDuplicateAck = 0;//nombre de ack dupliquer.
    private int lastAck;//dernier ack connu.
    private int lostPercentage = 10;//pourcentage de perte de package.

    public SenderProtocol(IPHost host, int numberOfPackage) {
        this.host = host;
        for (int i = 0; i < numberOfPackage; i++) {
            packageToSend.add(new PayloadMessage(i));
        }
    }

    /**
     * Cette methode permet de lancer une premiere fois l'application.
     */
    public void launch(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        sendPackageOfWindow(src, datagram);
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        AckMessage msg = (AckMessage) datagram.getPayload();
        int sequenceN = msg.getPayload();
        /*cette partie verifie si on obtient le numero voulu.
                Si on obtient le numero voulu on avance d'un element la fenetre,si le numero est plus grand on "saute" jusque ce numero la car on est en go-back-n et si on a un numero plus petit on renvoi la fenetre actuel.
         */

        if (sequenceN == -1) {
            // Reception du paquet test afin de determiner le RTT
            Timer.setArrivalTimer(System.currentTimeMillis());
            actualSequenceNumber = 0;
            
          // Si le ACK recu est plus grand que celui attendu  
        } else if (sequenceN >= this.actualSequenceNumber) {
            numberOfDuplicateAck = 0;
            if (sizeOfWindow < ssTresh) {
                sizeOfWindow += sizeOfWindow;
            } else {
                sizeOfWindow += 1;
            }

            if ((this.actualSequenceNumber + this.cursorSenderWindow) < this.packageToSend.size()) {
                if (sequenceN > this.actualSequenceNumber) {
                    cursorSenderWindow -= sequenceN - this.actualSequenceNumber + 1;
                    this.actualSequenceNumber = sequenceN + 1;
                } else {
                    this.actualSequenceNumber += 1;
                    cursorSenderWindow -= 1;
                }
                numberOfDuplicateAck = 0;
                lastAck = sequenceN;
                sendPackageOfWindow(src, datagram);
            }
        } else {
            if (lastAck == sequenceN) {
                numberOfDuplicateAck++;
                if (numberOfDuplicateAck == 3) {
                    sizeOfWindow = sizeOfWindow / 2;
                }
            }
            cursorSenderWindow = 0;
            if (lastAck != sequenceN) {
                numberOfDuplicateAck = 0;
            }
            lastAck = sequenceN;
            sendPackageOfWindow(src, datagram);
        }
    }

    public static ArrayList<PayloadMessage> getPackageToSend() {
        return packageToSend;
    }

    /**
     * Cette methode permet d'envoyer l'emlement se trouvant au niveau du
     * curseur et tout les elements suivants dans la fenetre d'envoi.
     *
     * @param src
     * @param datagram
     * @throws Exception
     */
    public void sendPackageOfWindow(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        // Lancement d un paquet test afin de determiner le RTT
        if (actualSequenceNumber == -1) {
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN,
                    this.packageToSend.get(cursorSenderWindow + actualSequenceNumber));
            Timer.setDepartTimer(System.currentTimeMillis());
        } else {
            int numberOfPackageToSend = sizeOfWindow - cursorSenderWindow;
            for (int i = 0; i < numberOfPackageToSend; i++) {

                if (i + actualSequenceNumber < this.packageToSend.size() - 1) {
                    Random r = new Random();
                    int pLP = r.nextInt(101);//tirage au sort d'un nombre entre 0 et 100 pour savoir si on perd le packet ou pas.

                    if (pLP > this.lostPercentage) {
                        System.out.println("Sender of Message (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)"
                                + " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst="
                                + datagram.dst + ", iif=" + src + ", message=" + this.packageToSend.get(cursorSenderWindow + actualSequenceNumber).getPayload() + ", counter=" + (cursorSenderWindow + actualSequenceNumber));
                        // Envoi du paquet
                        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN, this.packageToSend.get(cursorSenderWindow + actualSequenceNumber));
                        
                        // creation d un timer avec le numero de sequence attendu
                        new Timer(new Scheduler(), Timer.getArrivalTimer() - Timer.getDepartTimer() + 5000000, false, actualSequenceNumber, src,
                                datagram, this);
                    }
                }
                cursorSenderWindow += 1;
            }
        }
    }

    public void sendPackageOutTimer(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        this.cursorSenderWindow = 0;
        sendPackageOfWindow(src, datagram);
    }

    public static int getActualSequenceNumber() {
        return actualSequenceNumber;
    }

}
