package tn.esprit.Map.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import tn.esprit.Map.interfaces.MandateServiceLocal;
import tn.esprit.Map.interfaces.SkillRemote;
import tn.esprit.Map.persistences.AvailabilityType;
import tn.esprit.Map.persistences.DayOff;
import tn.esprit.Map.persistences.Mandate;
import tn.esprit.Map.persistences.MandateId;
import tn.esprit.Map.persistences.Request;
import tn.esprit.Map.persistences.Resource;
import tn.esprit.Map.persistences.Skill;

@Stateless
public class MandateService implements MandateServiceLocal {
	@PersistenceContext(unitName = "MAP")
	EntityManager em;

	@EJB
	MailService mail;

	@EJB
	SkillRemote skillremote;

	@Override
	public boolean isAvailable(int resourceId, Date date) {
		List<DayOff> dayOffs = new ArrayList<DayOff>();
		TypedQuery<Resource> query1 = em.createQuery("SELECT r FROM Resource r where r.id=:rId", Resource.class);
		query1.setParameter("rId", resourceId);
		Resource resource;
		try {
			resource = query1.getSingleResult();

		} catch (Exception e) {
			return false;
		}

		if (resource.getAvailability() == AvailabilityType.available)
			return true;
		else if (resource.getAvailability() == AvailabilityType.availableSoon) {

			TypedQuery<Mandate> query = em.createQuery(
					"SELECT m FROM Mandate m where m.mandateId.ressourceId=:rId ORDER BY m.mandateId.dateFin AND m.archived = false",
					Mandate.class);
			query.setParameter("rId", resourceId);
			try {
				Mandate mandate = query.getSingleResult();
				dayOffs.addAll(resource.getDayOffs());
				Collections.sort(dayOffs, new Comparator<DayOff>() {
					@Override
					public int compare(DayOff d1, DayOff d2) {

						return d2.getEndDate().compareTo(d1.getEndDate());
					}
				});
				if (mandate.getMandateId().getDateFin().compareTo(date) >= 0) {
					return false;
				} else {
					if (dayOffs.isEmpty())
						return true;
					else if (dayOffs.get(0).getEndDate().compareTo(date) >= 0)
						return false;
					else
						return true;
				}
			} catch (Exception e) {
				dayOffs.addAll(resource.getDayOffs());
				Collections.sort(dayOffs, new Comparator<DayOff>() {
					@Override
					public int compare(DayOff d1, DayOff d2) {

						return d2.getEndDate().compareTo(d1.getEndDate());
					}
				});
				if (dayOffs.isEmpty())
					return true;
				else if (dayOffs.get(0).getEndDate().compareTo(date) >= 0)
					return false;
				else
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean notif(int resourceId, int requestId, String link) {
		TypedQuery<Request> query = em.createQuery("SELECT r FROM Request r where r.id=:rId", Request.class);
		query.setParameter("rId", requestId);
		Request request;
		TypedQuery<Resource> query1 = em.createQuery("SELECT r FROM Resource r where r.id=:rId", Resource.class);
		query1.setParameter("rId", resourceId);
		Resource resource;
		try {
			request = query.getSingleResult();
			resource = query1.getSingleResult();
		} catch (Exception e) {
			return false;
		}
		mail.send(resource.getEmail(), "New Mandate", "You were Appointed to a new request",
				"following the acceptance of your profile by our client", request.getClient().getNameSociety(),
				"you are assigned to a new project", "Project Name : " + request.getProject().getProjectName(), "",
				"Address : " + request.getProject().getAddress() + " <br>Start Date : " + request.getStartDateMondate()
						+ " <br>End Date : " + request.getEndDateMondate(),
				link);
		return true;
	}

	@Override
	public List<Mandate> getAll() {
		List<Mandate> results;
		TypedQuery<Mandate> query = em.createQuery("SELECT m FROM Mandate m WHERE m.archived = false", Mandate.class);
		try {
			results = query.getResultList();
		} catch (Exception e) {
			results = new ArrayList<>();
		}
		return results;
	}

	@Override
	public List<Mandate> getByResource(int resourceId) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.ressourceId=:rId AND m.archived = false", Mandate.class);
		query.setParameter("rId", resourceId);
		List<Mandate> results;
		try {
			results = query.getResultList();
			return results;
		} catch (Exception e) {
			return new ArrayList<Mandate>();
		}

	}

	@Override
	public List<Mandate> getByProject(int projectId) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.projetId = :pId AND m.archived = false", Mandate.class);
		query.setParameter("pId", projectId);
		List<Mandate> results = query.getResultList();
		return results;
	}

	@Override
	public List<Mandate> getByStartDate(Date startDate) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.dateDebut = :startDate AND m.archived = false",
				Mandate.class);
		query.setParameter("startDate", startDate, TemporalType.DATE);
		List<Mandate> results = query.getResultList();
		return results;
	}

	@Override
	public List<Mandate> getByEndDate(Date endDate) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.dateFin = :endDate AND m.archived = false", Mandate.class);
		query.setParameter("endDate", endDate, TemporalType.DATE);
		List<Mandate> results = query.getResultList();
		return results;
	}

	@Override
	public List<Mandate> getByPeriod(Date startDate, Date endDate) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.dateFin BETWEEN :endDate AND :startDate AND m.archived = false",
				Mandate.class);
		query.setParameter("endDate", endDate, TemporalType.DATE);
		query.setParameter("startDate", startDate, TemporalType.DATE);
		List<Mandate> results = new ArrayList<>();
		results = query.getResultList();
		return results;
	}

	@Override
	public Double calculateCost(int ressourceId, int projetId, Date startDate, Date endDate, int gpsId) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.dateDebut = :startDate AND m.mandateId.dateFin = :endDate AND m.mandateId.projetId = :pId AND  m.mandateId.ressourceId=:rId AND m.archived = false",
				Mandate.class);
		query.setParameter("startDate", startDate, TemporalType.DATE);
		query.setParameter("endDate", endDate, TemporalType.DATE);
		query.setParameter("pId", projetId);
		query.setParameter("rId", ressourceId);
		try {
			if (query.getSingleResult().getMontant() == 0)
				;
			{
				query.getSingleResult().setMontant(query.getSingleResult().getRessource().getSalary()
						* query.getSingleResult().getRessource().getTaux());
			}
			return query.getSingleResult().getMontant();
		} catch (Exception e) {
			return 0.0;
		}
	}

	@Override
	public boolean addGps(int ressourceId, int projetId, Date startDate, Date endDate, int gpsId) {
		Mandate results;
		TypedQuery<Mandate> query = em
				.createQuery("SELECT m FROM Mandate m where m.mandateId = :m AND m.archived = false", Mandate.class);
		MandateId mandateId = new MandateId();
		mandateId.setDateDebut(startDate);
		mandateId.setDateFin(endDate);
		mandateId.setProjetId(projetId);
		mandateId.setRessourceId(ressourceId);
		query.setParameter("m", mandateId);
		TypedQuery<Resource> query1 = em.createQuery("SELECT r FROM Resource r where r.id=:rId", Resource.class);
		query1.setParameter("rId", gpsId);
		Resource resourceGPS;
		try {
			resourceGPS = query1.getSingleResult();
			results = query.getSingleResult();
			results.setGps(resourceGPS);

		} catch (Exception e) {
			return false;
		}
		return true;

	}

	@Override
	public boolean addMandate(int requestId, int resourceId) {
		TypedQuery<Request> query = em.createQuery("SELECT r FROM Request r where r.id=:rId", Request.class);
		query.setParameter("rId", requestId);
		Request request;
		try {
			request = query.getSingleResult();
		} catch (Exception e)

		{
			return false;
		}

		TypedQuery<Resource> query1 = em.createQuery("SELECT r FROM Resource r where r.id=:rId", Resource.class);
		query1.setParameter("rId", resourceId);
		Resource resource;
		try {
			resource = query1.getSingleResult();
		} catch (Exception e) {

			return false;
		}

		Mandate mandate = new Mandate();
		MandateId mandateId = new MandateId();

		mandateId.setDateDebut(request.getStartDateMondate());
		mandateId.setDateFin(request.getEndDateMondate());
		mandateId.setProjetId(request.getProject().getId());
		mandateId.setRessourceId(resource.getId());
		mandate.setMontant(0.0);
		mandate.setMandateId(mandateId);
		mandate.setArchived(false);
		try {
			em.persist(mandate);
			UpdateAvailability(mandate.getMandateId().getRessourceId(), AvailabilityType.unavailable);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Schedule(second = "00", minute = "00", hour = "00")
	public void AlertEndMandate(Timer timer) {
		System.out.println(getDate());
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT  m FROM Mandate m where m.mandateId.dateFin = :d AND m.archived = false", Mandate.class);

		try {
			query.setParameter("d", simpleDateFormat.parse(getDate()), TemporalType.DATE);
			List<Mandate> mandates = query.getResultList();
			if (!mandates.isEmpty()) {
				mandates.forEach(e -> {
					notifEndProject(e);
					UpdateAvailability(e.getMandateId().getRessourceId(), AvailabilityType.availableSoon);
				});

			} else
				System.out.println("liste vide");

		} catch (Exception e) {
			System.out.println("liste vide");
		}

	}

	public String getDate() {
		String DATE_FORMAT = "yyyy-MM-dd";
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		// Get current date
		Date currentDate = new Date();
		LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		// plus one
		localDateTime = localDateTime.plusDays(40);

		// convert LocalDateTime to date
		Date currentDatePlus = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

		return dateFormat.format(currentDatePlus);

	}

	@Override
	public boolean notifEndProject(Mandate mandate) {
		mail.send(mandate.getRessource().getEmail(), "End Mandate", "Alert End Of Mandate",
				"Levio Administration want to Inform you that the project", mandate.getProjet().getProjectName(),
				"will end in just 40 days",
				"Mr/Ms " + mandate.getRessource().getLastName() + " " + mandate.getRessource().getFirstName(), "",
				"We want to thank you for your hard work and always remember Levio is  proud of your work <br> you can check further information in the link",
				"http://localhost:18080/Map-JavaEE-web/MAP/mandate?dateFin=" + mandate.getMandateId().getDateFin()
						+ "&dateDebut=" + mandate.getMandateId().getDateDebut() + "&ressourceId="
						+ mandate.getMandateId().getRessourceId() + "&projetId="
						+ mandate.getMandateId().getProjetId());
		mail.send(mandate.getProjet().getClient().getEmail(), "End Mandate", "Alert End Of Mandate",
				"Levio Administration want to Inform you that the project", mandate.getProjet().getProjectName(),
				"will end in just 40 days",
				"Mr/Ms " + mandate.getProjet().getClient().getLastName() + " "
						+ mandate.getProjet().getClient().getFirstName(),
				"",
				"We want to thank you for your hard work and always remember Levio is  proud of your work <br> you can check further information in the link",
				"http://localhost:18080/Map-JavaEE-web/MAP/mandate?dateFin=" + mandate.getMandateId().getDateFin()
						+ "&dateDebut=" + mandate.getMandateId().getDateDebut() + "&ressourceId="
						+ mandate.getMandateId().getRessourceId() + "&projetId="
						+ mandate.getMandateId().getProjetId());
		return true;
	}

	@Override
	public Mandate getMandate(int ressourceId, int projetId, Date startDate, Date endDate) {
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.mandateId.dateDebut = :startDate AND m.mandateId.dateFin = :endDate AND m.mandateId.projetId = :pId AND  m.mandateId.ressourceId=:rId AND m.archived = false",
				Mandate.class);
		query.setParameter("startDate", startDate, TemporalType.DATE);
		query.setParameter("endDate", endDate, TemporalType.DATE);
		query.setParameter("pId", projetId);
		query.setParameter("rId", ressourceId);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}

	}

	@Override
	public void UpdateAvailability(int resourceId, AvailabilityType availabilityType) {
		TypedQuery<Resource> query = em.createQuery("SELECT m FROM Resource m where m.id=:rId", Resource.class);
		query.setParameter("rId", resourceId);
		try {
			Resource resource = query.getSingleResult();
			resource.setAvailability(availabilityType);
		} catch (Exception e) {

		}
	}

	@Override
	public List<Resource> SearchResourceBySkill(int requestId) {
		Request request;
		List<Skill> listSkillsRequired = new ArrayList<>();
		List<Resource> listeRecourceNeeded = new ArrayList<>();
		TypedQuery<Request> query = em.createQuery("SELECT m FROM Request m where m.id=:rId", Request.class);
		query.setParameter("rId", requestId);
		TypedQuery<Resource> query1 = em.createQuery("SELECT m FROM Resource m", Resource.class);
		try {
			request = query.getSingleResult();
			List<Resource> resources = query1.getResultList();
			listSkillsRequired.addAll(request.getProject().getListeSkills());
			resources.forEach(e -> {
				List<Skill> resourceskills = skillremote.orderSkillsOfResource(e.getId());
				if (resourceskills.size() == listSkillsRequired.size()) {
					int i = 0;
					while (resourceskills.get(i).equals(listSkillsRequired.get(i)) && i < resourceskills.size()) {
						i++;
					}
					if (i == resourceskills.size())
						listeRecourceNeeded.add(e);
				}
			});
			return listeRecourceNeeded;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Schedule(second = "00", minute = "37", hour = "20")
	public void archive(Timer timer) {
		List<Mandate> results;
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.archived = false AND CURRENT_DATE = m.mandateId.dateFin",
				Mandate.class);
		try {
			results = query.getResultList();
			results.forEach(e -> {
				e.setArchived(true);
			});
		} catch (Exception e) {
		}
	}

	@Override
	public List<Mandate> getAllTypeMandate() {

		List<Mandate> results;
		TypedQuery<Mandate> query = em.createQuery("SELECT m FROM Mandate m WHERE m.archived = true", Mandate.class);
		try {
			results = query.getResultList();
		} catch (Exception e) {
			results = new ArrayList<>();
		}
		return results;
	}

	@Override
	public boolean restore(int ressourceId, int projetId, Date startDate, Date endDate) {
		Mandate results;
		TypedQuery<Mandate> query = em.createQuery(
				"SELECT m FROM Mandate m where m.archived = false AND CURRENT_DATE = m.mandateId.dateFin",
				Mandate.class);
		try {
			results = query.getSingleResult();
			results.setArchived(false);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

}
