package de.upb.study.taskthree;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.cognicrypt.crypto.PWHasher;

public class TaskThreeMain {

	private Connection conn;

	public TaskThreeMain() throws SQLException {
		String url = "jdbc:sqlite:C:\\Users\\adm\\git\\Paper-CogniCryptUserStudy\\User Study Documents\\tasks\\Task3\\resources\\test.db";
		conn = DriverManager.getConnection(url);
	}

	public boolean addUser(String user, char[] pass) throws SQLException, GeneralSecurityException {
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users(name,password) VALUES(?,?)");
		pstmt.setString(1, user);

		String pwdHash = PWHasher.createPWHash(pass); // This call hashes the password pwd.
		pstmt.setString(2, pwdHash);
		return pstmt.executeUpdate() > 0;
	}

	public String retrievePasswordAsStoredInDB(String user) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement("Select password FROM users where name = ?");
		pstmt.setString(1, user);

		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			return rs.getString("password");
		}
		return "";
	}

	public Boolean verifyPassword(String user, char[] initialPwd) throws SQLException, GeneralSecurityException {
		String passwordInDatabase = retrievePasswordAsStoredInDB(user);

		return PWHasher.verifyPWHash(initialPwd, passwordInDatabase);
	}

}
