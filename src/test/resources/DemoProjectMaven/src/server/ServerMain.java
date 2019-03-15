package server;

public class ServerMain {

	public static void main(String[] args) {
		User user = new User();
		user.setName("Adam");
		Server server = new Server();
		server.setUser(user);
		server.login();
		
		User b = new User();
		System.out.println(b);
	}

}
