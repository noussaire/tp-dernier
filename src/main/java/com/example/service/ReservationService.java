package com.example.service;
import com.example.model.*;
import com.example.util.Jpa;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ReservationService {
    private final EntityManagerFactory emf;
    public ReservationService(EntityManagerFactory emf) { this.emf=emf; }
    public Long creer(Long utilisateur, Long salle, LocalDateTime debut, LocalDateTime fin) {
        return enregistrer(null,utilisateur,salle,debut,fin);
    }
    public Long modifier(Long id, Long utilisateur, Long salle, LocalDateTime debut, LocalDateTime fin) {
        return enregistrer(id,utilisateur,salle,debut,fin);
    }
    private Long enregistrer(Long id, Long userId, Long salleId, LocalDateTime debut, LocalDateTime fin) {
        if(debut==null || fin==null || !fin.isAfter(debut)) throw new IllegalArgumentException("Creneau invalide");
        return Jpa.transaction(emf, em -> {
            // Toutes les ecritures de reservation prennent le verrou de la salle.
            // Il serialise le controle d'absence de chevauchement, meme sur une salle vide.
            Salle salle=em.find(Salle.class,salleId,LockModeType.PESSIMISTIC_WRITE);
            Utilisateur user=em.find(Utilisateur.class,userId);
            if(salle==null || user==null) throw new IllegalArgumentException("Salle ou utilisateur introuvable");
            Reservation r=id==null ? new Reservation() : em.find(Reservation.class,id);
            if(r==null) throw new IllegalArgumentException("Reservation introuvable");
            if(id!=null && !r.getSalle().getId().equals(salleId)) throw new IllegalArgumentException("Le changement de salle exige une nouvelle reservation");
            Long count=em.createQuery("select count(r) from Reservation r where r.salle.id=:s and r.dateDebut<:fin and r.dateFin>:debut and (:id is null or r.id<>:id)",Long.class)
                .setParameter("s",salleId).setParameter("debut",debut).setParameter("fin",fin).setParameter("id",id).getSingleResult();
            if(count>0) throw new IllegalStateException("La salle est deja reservee sur ce creneau");
            r.setSalle(salle); r.setUtilisateur(user); r.setDateDebut(debut); r.setDateFin(fin);
            if(id==null) em.persist(r);
            Logger.getLogger(getClass().getName()).info("Reservation enregistree pour "+user.getEmail());
            return r.getId();
        });
    }
    public void supprimer(Long id) {
        Jpa.transaction(emf,em->{ Reservation r=em.find(Reservation.class,id);
            if(r==null) throw new IllegalArgumentException("Reservation introuvable"); em.remove(r); return null; });
    }
    public List<Reservation> rechercherJpql(String nom,LocalDateTime apres) {
        return Jpa.transaction(emf,em->em.createQuery("select r from Reservation r join fetch r.salle s join fetch r.utilisateur where s.nom=:nom and r.dateDebut>:date order by r.dateDebut",Reservation.class)
            .setParameter("nom",nom).setParameter("date",apres).setHint("org.hibernate.cacheable",true).getResultList());
    }
    public List<Reservation> rechercherCriteria(String nom,LocalDateTime apres) {
        return Jpa.transaction(emf,em->{ CriteriaBuilder cb=em.getCriteriaBuilder(); CriteriaQuery<Reservation> q=cb.createQuery(Reservation.class);
            Root<Reservation> r=q.from(Reservation.class); r.fetch("salle"); r.fetch("utilisateur");
            q.select(r).where(cb.and(cb.equal(r.get("salle").get("nom"),nom),cb.greaterThan(r.<LocalDateTime>get("dateDebut"),apres))).orderBy(cb.asc(r.get("dateDebut")));
            return em.createQuery(q).getResultList(); });
    }
}
