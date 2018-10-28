package tn.esprit.Map.services;

import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import tn.esprit.Map.interfaces.PersonServiceRemote;
import tn.esprit.Map.persistences.Message;



@Stateful
public class PersonService implements PersonServiceRemote{

	@PersistenceContext(unitName="MAP")
	EntityManager em;
	

	@Override
	public String test(Message msg) {
		em.persist(msg);
		return msg.getContent();
	}




	

	

	

	

	

	



}