package com.example.util;
import com.example.model.*;
import com.example.service.ReservationService;
import javax.persistence.*;
import java.time.LocalDate;
public class DataInitializer {
    private final EntityManagerFactory emf;
    public DataInitializer(EntityManagerFactory emf) { this.emf=emf; }
    public void initData() {
        Jpa.transaction(emf,em->{
            if(em.createQuery("select count(u) from Utilisateur u",Long.class).getSingleResult()>0) return null;
            String[] noms={"Alice Dupont","Youssef Alami","Sara Bennani","Mehdi Idrissi","Lina Martin"};
            Utilisateur[] users=new Utilisateur[noms.length];
            for(int i=0;i<noms.length;i++) { users[i]=new Utilisateur(); users[i].setNom(noms[i]); users[i].setEmail("utilisateur"+i+"@example.com"); em.persist(users[i]); }
            for(int i=0;i<4;i++) {
                Salle s=new Salle(); s.setNom("Salle "+(char)('A'+i)); s.setCapacite(10+i*10); s.setDescription("Projecteur, Wi-Fi, tableau - etage "+(i+1)); em.persist(s);
                for(int j=1;j<=5;j++) { Reservation r=new Reservation(); r.setSalle(s); r.setUtilisateur(users[(i+j)%users.length]);
                    r.setDateDebut(LocalDate.now().plusDays(j).atTime(9+i,0)); r.setDateFin(r.getDateDebut().plusHours(2)); em.persist(r); }
            }
            return null;
        });
    }
}
