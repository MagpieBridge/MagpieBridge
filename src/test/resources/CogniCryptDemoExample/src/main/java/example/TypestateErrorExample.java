package example;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;

/**
 * This code contains a misuse example CogniCrypt_SAST of a Signature object. 
 * CogniCrypt_SAST reports that the usage pattern does not correspond to the specification within the ORDER block of the CrySL rule for 
 * Signature
 *
 */
public class TypestateErrorExample {
	public static void main(String...args) throws GeneralSecurityException {
		Signature s = Signature.getInstance("SHA256withRSA");
		s.initSign(getPrivateKey());
		/**
		 * The Signature API expects a call to update here. This call supplied the actual data to the signature instance.
		 * A call such as s.update(data); would resolve this finding.
		 */
//		s.update(args[0].getBytes());
		s.sign();
	}

	private static PrivateKey getPrivateKey() throws GeneralSecurityException {
		KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
		kpgen.initialize(2048);
		KeyPair gp = kpgen.generateKeyPair();
		return gp.getPrivate();
	}

}
