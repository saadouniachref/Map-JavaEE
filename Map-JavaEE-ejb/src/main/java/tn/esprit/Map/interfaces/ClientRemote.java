package tn.esprit.Map.interfaces;

import java.util.List;

import javax.ejb.Remote;

import tn.esprit.Map.persistences.Client;

@Remote
public interface ClientRemote {
	//public int addClient(Client client);
	//public List<Client> getAllClients(int id);
	public String getAllClients(int id);
}
