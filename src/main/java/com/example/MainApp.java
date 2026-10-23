package com.example;
import com.example.model.Salle;
import com.example.service.ReservationService;
import com.example.util.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.logging.*;
public class MainApp {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf=null;
        try {
            new MigrationExecutor("jdbc:h2:./data/reservation","sa","").migrate();
            emf=Persistence.createEntityManagerFactory("ReservationPU");
            new DataInitializer(emf).initData();
            ReservationService service=new ReservationService(emf);
            service.rechercherJpql("Salle A",LocalDateTime.now());
            service.rechercherJpql("Salle A",LocalDateTime.now().toLocalDate().atStartOfDay());
            service.rechercherJpql("Salle A",LocalDateTime.now().toLocalDate().atStartOfDay());
            service.rechercherCriteria("Salle A",LocalDateTime.now());
            Long id=Jpa.transaction(emf,em->em.createQuery("select s.id from Salle s order by s.id",Long.class).setMaxResults(1).getSingleResult());
            emf.getCache().evict(Salle.class,id);
            for(int i=0;i<2;i++) Jpa.transaction(emf,em->em.find(Salle.class,id));
            PerformanceReport report=new PerformanceReport(emf); report.printReport(); report.save(Path.of("target/rapport-performance.txt"));
        } catch(Exception e) { Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE,"Echec de l'application",e); throw e; }
        finally { if(emf!=null) emf.close(); }
    }
}
