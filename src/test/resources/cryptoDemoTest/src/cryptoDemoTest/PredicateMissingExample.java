package cryptoDemoTest;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * This code contains a misuse example CogniCrypt_SAST of a Cipher object. 
 * CogniCrypt_SAST reports that the Cipher instance is initialized (call to init(...,key)) with a key that was not properly generated.
 * The key generator is supplied with a key length of 46 (call to init(46)) which is not a correct key length for AES. 
 * Therefore, the generated secret key will be insecure and transitively the Cipher usage.
 *
 */
public class PredicateMissingExample {
	public static void main(String...args) throws  GeneralSecurityException {
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		
		//CogniCryt_SAST reports an error in the next line saying that the key size is chosen inappropriately. 
		keygen.init(46);
		SecretKey key = keygen.generateKey();
		Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
		
		//CogniCryt_SAST reports an error in the next line as the key flowing to this Cipher usage was not generated securely. 
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encText = c.doFinal("".getBytes());
	}
}
