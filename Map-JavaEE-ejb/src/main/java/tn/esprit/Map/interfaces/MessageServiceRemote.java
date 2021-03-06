package tn.esprit.Map.interfaces;

import java.util.List;

import javax.ejb.Remote;


import tn.esprit.Map.persistences.Message;
import tn.esprit.Map.persistences.Person;
import tn.esprit.Map.persistences.Request;

@Remote
public interface MessageServiceRemote {
	
	public int addMessage(Message message,int personID);
	public int addMessage2(Message message);
	public int deleteMessage(int messageID);
	public String deleteMessageJPQL(Message message);
	public Message getMessageByID(int messageID);
	public List<Message> AllMessage(); 
	public List<Message> MessageByPerson(Person personne);
	public long countReclamationMessageByPerson(Person personne);
	public long countSatisfactionMessageByPerson(Person personne);
	public long countProblemMessageByPerson(Person personne);
	public double calculNotePerson(Person personne);
	public String updateMessage(Person personne);
	public String updatePersonMessage(Message message);
	public List<Message> allMessageSortedByPerson();  

}
