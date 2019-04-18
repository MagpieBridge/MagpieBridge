package demo;

import java.security.GeneralSecurityException;

public class SignatureExample {

	public static void main(String...args) throws GeneralSecurityException {
		SecUtils secUtils = new SecUtils();
		byte[] sign = secUtils.sign("data");
	}

}
