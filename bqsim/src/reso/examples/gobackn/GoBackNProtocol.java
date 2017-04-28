package reso.examples.gobackn;

/*
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
*/

public class GoBackNProtocol 
implements IPInterfaceListener {
	public static final int IP_PROTO_GoBackN= Datagram.allocateProtocolNumber("GoBackN");
	
	private final IPHost host; 
	private int actualSequenceNumber = 0;
	
	public PingPongProtocol(IPHost host) {
		this.host= host;
	}
	
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		Payload msg = (Payload) datagram.getPayload();
		String sequenceSN = msg.getpayload().substring(0, 32);
		int sequenceNumber = Integer.parseInt(sequenceSN,2);
		int ack = 0;
		if(sequenceChecker(sequenceNumber)){
			ack = actualSequenceNumber;
			actualSequenceNumber = actualSequenceNumber + 1;
		}else{
			if (actualSequenceNumber!=0){
				ack = actualSequenceNumber-1;
			}
		}
		host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GoBackN, new Payload(ack));
	}


	/**
	*Cette methode verifie si le numero de sequence envoye est celui voulu.
	*	
	*@return true si le numero de sequence recu est le numero voulu, false sinon.
	**/
	public boolean sequenceChecker(int sequenceNumber){
		return actualSequenceNumber == sequenceNumber;
	}

}
