package cryptoDemoTest;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.NoSuchPaddingException;

/**
 * This code contains a misuse example CogniCrypt_SAST of a Signature object. 
 * CogniCrypt_SAST reports that the object is destroyed in an non-accepting state, or in other words the object is not used to fulfill a task.
 *
 */
public class IncompleOperationErrorExample {
	public static void main(String...args) throws NoSuchAlgorithmException, NoSuchPaddingException, GeneralSecurityException {
		Signature instance = Signature.getInstance("SHA256withRSA");
		instance.initSign(getPrivateKey());
		instance.update(args[0].getBytes());
		/**
		 * The following call is missing, therefore the Signature object is never actually used to compute a Signature.
		 */
		//instance.sign();
		
		IncompleOperationErrorExample ex = new IncompleOperationErrorExample();
		ex.doInit();
		ex.doUpate();
		ex.doSign();
	}


	private Signature signature;

	private void doInit() throws GeneralSecurityException {
		signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(getPrivateKey());
	}

	private void doUpate() throws GeneralSecurityException {
		signature.update("test".getBytes());
	}

	private void doSign() throws SignatureException {
		/**
		 * The following call is missing, therefore the Signature object is never actually used to compute a Signature.
		 */
//		signature.sign();	
	}
	
	private static PrivateKey getPrivateKey() throws GeneralSecurityException {
		KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
		kpgen.initialize(2048);
		KeyPair gp = kpgen.generateKeyPair();
		return gp.getPrivate();
	}
}
