package reso.examples.gobackn;

import java.util.Random;
import reso.common.Message;

/**
 * Cette classe s'occupe de créer le message qui formera le paquet a envoye
 */
public class PayloadMessage implements Message {

    private String payload;

    /**
     * Cree le payload et le place dans la variable payload.
     *
     * @param sequenceNumber numero de sequence du payload.
     */
    public PayloadMessage(int sequenceNumber) {
        String sequenceNumS = "";
        int message;
        //cree un message aleatoire.
        Random r = new Random();
        if (sequenceNumber != 0) {
            message = r.nextInt(1001);
        } else {
            message = 0;
        }
        //traduis le numero de sequence en binaire.
        sequenceNumS = Integer.toBinaryString(sequenceNumber);

        //place le numero de sequence sur 32 "bits"
        int zeroNeed = (32 - sequenceNumS.length());
        for (int i = 0; i < zeroNeed; i++) {
            sequenceNumS = "0" + sequenceNumS;
        }

        //cree le payload
        this.payload = sequenceNumS + message;
    }

    public String getPayload() {
        return this.payload;
    }

    public int getByteLength() {
        return payload.getBytes().length;
    }
}
