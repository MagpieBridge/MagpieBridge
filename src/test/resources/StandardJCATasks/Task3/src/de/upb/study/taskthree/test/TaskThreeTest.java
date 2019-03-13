package de.upb.study.taskthree.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.study.taskthree.TaskThreeMain;

public class TaskThreeTest {

	private static TaskThreeMain ttm;
	
	@BeforeClass
	public static void setup() throws SQLException {
		ttm = new TaskThreeMain();
	}
	
	@Test
	public void testRetrievedPWNotPlainText() throws SQLException, GeneralSecurityException {
		String user = UUID.randomUUID().toString();
		char[] initPW = generatePW();
		ttm.addUser(user, initPW);
		
		String retPW = ttm.retrievePasswordAsStoredInDB(user);
		assertNotEquals(new String(initPW), retPW);
	}
	
	@Test
	public void testRetrievedPWExists() throws SQLException, GeneralSecurityException {
		String user = UUID.randomUUID().toString();
		ttm.addUser(user, generatePW());
		
		String retPW = ttm.retrievePasswordAsStoredInDB(user);
		assertNotEquals(null, retPW);
		assertFalse(retPW.isEmpty());
	}
	
	@Test
	public void testVerification() throws SQLException, GeneralSecurityException {
		String user = UUID.randomUUID().toString();
		char[] initPW = generatePW();
		ttm.addUser(user, initPW);
		assertTrue(ttm.verifyPassword(user, initPW));
	}
	
	private char[] generatePW() {
		SecureRandom rand = new SecureRandom();
		char[] pwd = new char[32];
		
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = (char) (rand.nextInt(26) + 'a');
		}
		return pwd;
	}

}
