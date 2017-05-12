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

/**
 *
 */
public class SenderProtocol
        implements IPInterfaceListener {

    public static final int IP_PROTO_SenderProtocol = Datagram.allocateProtocolNumber("Sender");

    private final IPHost host;
    private static int actualSequenceNumber = 0;//numero de sequence attendu.
    private static ArrayList<PayloadMessage> packageToSend = new ArrayList<PayloadMessage>();//liste des package a envoyer.
    private int cursorSenderWindow = 1;//position du curseur dans la fenetre.

    // private static boolean testTimer=false;
    private int sizeOfWindow = 1;//taille de la fenetre d'envoi.
    private int ssTresh;
    private int numberOfDuplicateAck = 0;//nombre de ack dupliquer.
    private int lastAck;//dernier ack connu.
    private int lostPercentage = 10;//pourcentage de perte de package.
    private Timer actualTimer;
    private float restOfAI = 0;
    private double r;
    private double srtt;
    private double rttvar;
    private double timerValue;

    public SenderProtocol(IPHost host, int numberOfPackage, int ssTresh) {
        this.host = host;
        packageToSend.add(new PayloadMessage(0));
        for (int i = 1; i < numberOfPackage + 1; i++) {
            packageToSend.add(new PayloadMessage(i));
        }
        this.ssTresh = ssTresh;
    }

    public SenderProtocol(IPHost host, int numberOfPackage, int ssTresh, int lostPercentage) {
        this.host = host;
        packageToSend.add(new PayloadMessage(0));
        for (int i = 1; i < numberOfPackage + 1; i++) {
            packageToSend.add(new PayloadMessage(i));
        }
        this.lostPercentage = lostPercentage;
        this.ssTresh = ssTresh;
    }

    /**
     * Cette methode permet de lancer une premiere fois l'application.
     */
    public void launch(IPAddress address) throws Exception {
        host.getIPLayer().send(IPAddress.ANY, address, IP_PROTO_GoBackN,
                this.packageToSend.get(0));
        Timer.setDepartTimer(host.getNetwork().getScheduler().getCurrentTime());
        System.out.println("---------------------------------------------------");
        System.out.println("Package test launch");
    }

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        AckMessage msg = (AckMessage) datagram.getPayload();
        int sequenceN = msg.getPayload();
        /*cette partie verifie si on obtient le numero voulu.
                Si on obtient le numero voulu on avance d'un element la fenetre,si le numero est plus grand on "saute" jusque ce numero la car on est en go-back-n et si on a un numero plus petit on renvoi la fenetre actuel.
         */
        System.out.println(" ");
        System.out.println("---------------------------------------------------");
        System.out.println("Ack receive: " + sequenceN + ",Ack expected: " + this.actualSequenceNumber);
        if (sequenceN == this.packageToSend.size() - 1) {
            if (this.actualTimer != null) {
                this.actualTimer.stop();
                this.actualTimer = null;
            }

            // Si le ACK recu est plus grand que celui attendu  
        } else if (sequenceN >= this.actualSequenceNumber) {
            if (sequenceN == 0) {
                // Reception du paquet test afin de determiner le R
                Timer.setArrivalTimer(host.getNetwork().getScheduler().getCurrentTime());
                this.r = Timer.getArrivalTimer() - Timer.getDepartTimer();
                this.srtt = this.r;
                this.rttvar = this.r/2;
                this.timerValue = 3000;
                System.out.println("Package test receive; R value calculate: "+ this.r+"s");
                System.out.println("---------------------------------------------------");
            }
            this.numberOfDuplicateAck = 0;
            if (sequenceN != 0) {
                //calcul du srtt,rrttvar et de la nouvelle valeur du timer.
                this.srtt = (0.875*this.srtt)+(0.125*this.r);
                this.rttvar = (0.75*this.rttvar)+0.25*(Math.abs(this.srtt - this.r));
                this.timerValue = this.srtt + (4*this.rttvar);
                //calcul de la taille de la fenetre d'envoi.
                for (int i = 0; i < sequenceN - this.actualSequenceNumber + 1; i++) {
                    //slow start
                    if (sizeOfWindow < ssTresh) {
                        System.out.println("Actual State: Slow Start.");
                        sizeOfWindow += 1;
                    }//additive increase. 
                    else {
                        System.out.println("Actual State: additive increase.");
                        float additiveIncreseNumber = 1 / (float) sizeOfWindow; //car MSS²/cwd = 1/cwd dans ce cas-ci.
                        int additiveIncreseNumberI = (int) additiveIncreseNumber;
                        this.restOfAI = restOfAI + additiveIncreseNumber - additiveIncreseNumberI;
                        sizeOfWindow += additiveIncreseNumberI + (int) restOfAI;
                        this.restOfAI = this.restOfAI - (int) restOfAI;
                    }
                }
            }
            //calcul du nouveau numero de sequence et gestion du curseur de la fenetre d'envoi.
            if (sequenceN > this.actualSequenceNumber) {
                cursorSenderWindow -= sequenceN - this.actualSequenceNumber + 1;
                this.actualSequenceNumber = sequenceN + 1;
            } else {
                this.actualSequenceNumber += 1;
                cursorSenderWindow -= 1;
            }
            //lancement du timer.
            gestionDuTimer(src, datagram);
            if ((this.actualSequenceNumber + this.cursorSenderWindow) < this.packageToSend.size()) {
                lastAck = sequenceN;
                sendPackageOfWindow(src, datagram);
            }
        } else if (lastAck == sequenceN) {
            //gestion des  ACK duplique.
            this.numberOfDuplicateAck++;
            if (this.numberOfDuplicateAck == 3) {
                System.out.println("---------------------------");
                System.out.println("/!\\/!\\/!\\/!\\/!\\/!\\/!\\/!\\/!\\");
                System.out.println("---------------------------");
                System.out.println("3 ACK duplicate");
                System.out.println("The lost package is the package number:" + this.actualSequenceNumber);
                System.out.println("last window size: " + this.sizeOfWindow + ", new window size: " + (sizeOfWindow / 2));
                System.out.println("last ssTresh: " + this.ssTresh + ", new ssTresh: " + (sizeOfWindow / 2));
                this.sizeOfWindow = sizeOfWindow / 2;
                this.ssTresh = this.sizeOfWindow;
                cursorSenderWindow = 0;
                this.restOfAI = 0;
                sendPackageOfWindow(src, datagram);
            }
        }
       
        LauncherGoBackN.plot.write(Double.toString(System.currentTimeMillis()));  
        LauncherGoBackN.plot.write("   ");
        LauncherGoBackN.plot.write(Integer.toString(sizeOfWindow));
        LauncherGoBackN.plot.write("\n");
        LauncherGoBackN.plot.flush();
        

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
        if (actualSequenceNumber == 0) {
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN,
                    this.packageToSend.get(cursorSenderWindow + actualSequenceNumber));
            Timer.setDepartTimer(System.currentTimeMillis());
        } else {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("Actual timer value: " + this.timerValue + "s");
            System.out.println("Size of the sending window: " + sizeOfWindow + ", with already " + cursorSenderWindow + " message(s) sent.");
            int numberOfPackageToSend = sizeOfWindow - cursorSenderWindow;
            //envoi des packages pas encore envoye dans la fenetre d'envoi.
            for (int i = 0; i < numberOfPackageToSend; i++) {
                if (i + actualSequenceNumber < this.packageToSend.size()) {
                    Random r = new Random();
                    int pLP = r.nextInt(101);//tirage au sort d'un nombre entre 0 et 100 pour savoir si on perd le packet ou pas.
                    if (pLP > this.lostPercentage) {
                        System.out.println("Sender of Message (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)"
                                + " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst="
                                + datagram.dst + ", iif=" + src + ", message=" + this.packageToSend.get(cursorSenderWindow + actualSequenceNumber).getPayload() + ", counter=" + (cursorSenderWindow + actualSequenceNumber));
                        // Envoi du paquet
                        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN, this.packageToSend.get(cursorSenderWindow + actualSequenceNumber));
                    }
                }
                cursorSenderWindow += 1;
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
    }
    
    /**
     * Cette methode permet de gerer l'expiration d'un timer.
     * @param src
     * @param datagram
     * @throws Exception 
     */
    public void sendPackageOutTimer(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        System.out.println("---------------------------");
        System.out.println("/!\\/!\\/!\\/!\\/!\\/!\\/!\\/!\\/!\\");
        System.out.println("---------------------------");
        System.out.println("Loss event => timer exception launch");
        System.out.println("The lost package is the package number:" + this.actualSequenceNumber);
        System.out.println("Last window size: " + this.sizeOfWindow + ", new window size: " + 1);
        System.out.println("Last ssTresh: " + this.ssTresh + ", new ssTresh: " + (sizeOfWindow / 2));
        this.cursorSenderWindow = 0;
        this.ssTresh = sizeOfWindow / 2;
        this.sizeOfWindow = 1;
        this.timerValue = 2*this.timerValue;
        gestionDuTimer(src, datagram);
        this.restOfAI = 0;
        sendPackageOfWindow(src, datagram);
    }

    public static int getActualSequenceNumber() {
        return actualSequenceNumber;
    }
    
    /**
     * Cette methode permet de gerer le timer (a lancer si ack recu permet d'avancer dans la fenetre d'envoi).
     * @param src
     * @param datagram 
     */
    public void gestionDuTimer(IPInterfaceAdapter src, Datagram datagram) {
        if (this.actualTimer != null) {
            this.actualTimer.stop();
        }
        // creation d un timer avec le numero de sequence attendu
        actualTimer = new Timer(host.getNetwork().getScheduler(), this.timerValue, false, actualSequenceNumber, src, datagram, this);
        this.actualTimer.start();
    }

}
