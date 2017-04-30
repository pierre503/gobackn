/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reso.examples.gobackn;

import reso.common.Message;

/**
 *Cette classe represente un ACK.
 * @author pierre
 */
public class AckMessage implements Message{
    private int Payload;
    
    /**
     * Permet de cree un ACK.
     * @param sequenceNumber  numero de sequence du ACK
     */
    public AckMessage(int sequenceNumber){
        this.Payload = sequenceNumber;
    }

    @Override
    public int getByteLength() {
        return Integer.SIZE / 8;
    }
    
    public int getPayload(){
        return this.Payload;
    }
    
}
