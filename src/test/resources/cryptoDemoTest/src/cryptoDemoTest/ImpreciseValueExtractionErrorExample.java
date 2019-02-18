package cryptoDemoTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/**
 * This code contains a misuse example CogniCrypt_SAST of a Cipher object. 
 * CogniCrypt_SAST reports that the String transformation used to instantiate the Cipher object is not extractable.
 *
 */
public class ImpreciseValueExtractionErrorExample {
	public static boolean NATIVE = false;
	public static void main(String... args) throws GeneralSecurityException, NoSuchPaddingException, IOException {
		String transformation;
		if(NATIVE) {
			/**
			 * The String transformation is either read from native:
			 */
			transformation = readFromNative();
		} else {
			/**
			 * or it is read from some configuration file:
			 */
			transformation = readFromConfigFile();
		}
		Cipher instance = Cipher.getInstance(transformation);
		instance.init(Cipher.ENCRYPT_MODE, getKey());
		instance.doFinal(args[0].getBytes());
	}

	private static Key getKey() throws GeneralSecurityException {
		return KeyGenerator.getInstance("AES").generateKey();
	}

	private static native String readFromNative();
	
	private static String readFromConfigFile() throws FileNotFoundException, IOException {
		return new BufferedReader(new FileReader(new File(""))).readLine();
	}

}
