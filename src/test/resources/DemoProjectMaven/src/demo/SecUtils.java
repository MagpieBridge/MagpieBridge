package demo;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;

public class SecUtils {
	private Signature sig;
	SecUtils() throws GeneralSecurityException{
		sig = Signature.getInstance("SHA256withRSA");
	}
	public byte[] sign(String data) throws GeneralSecurityException {
		sig.initSign(getPrivateKey());
		sig.update(data.getBytes());
		return sig.sign();
	}

	private static PrivateKey getPrivateKey() throws GeneralSecurityException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(208);
		KeyPair keyPair = new KeyPair(null, null);
		return keyPair.getPrivate();
	}
	
//	public byte[] sign(String data) throws GeneralSecurityException {
//		sig.initSign(getPrivateKey());
//		sig.update(data.getBytes());
//		return sig.sign();
//	}
//
//	private static PrivateKey getPrivateKey() throws GeneralSecurityException {
//		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//		kpg.initialize(2048);
//		KeyPair keyPair = kpg.generateKeyPair();
//		return keyPair.getPrivate();
//	}
}
