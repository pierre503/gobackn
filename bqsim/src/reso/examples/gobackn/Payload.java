package reso.examples.gobackn;
import java.util.Random;
import reso.common.Message;

/**
*Cette classe permet de representer un payload et de le creer.
**/
public class Payload
	implements Message{

	private String payload;	
	
	/**
	*Cree le payload et le place dans la variable payload.
	*
	*@param sequenceNumber numero de sequence du payload.
	**/
	public Payload(int sequenceNumber){
		//cree un message aleatoire.
		Random r = new Random();
		int message = r.nextInt(101);
		//traduis le numero de sequence en binaire.
		String sequenceNumS = Integer.toBinaryString(sequenceNumber);


		//regarder si on peut ameliorer la boucle
		for (int i = 0; i < (32-sequenceNumS.length()); i++){
			sequenceNumS = "0"+sequenceNumS;
		}
		//

		//cree le payload
		this.payload = sequenceNumS + message;
	}

	public String getpayload(){
		return this.payload;
	}

	public int getByteLength(){
		return payload.getByte().length;
	}
}
