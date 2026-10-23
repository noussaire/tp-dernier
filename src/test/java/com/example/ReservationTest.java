package com.example;
import com.example.model.*;
import com.example.service.ReservationService;
import com.example.util.*;
import javax.persistence.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {
    EntityManagerFactory emf; ReservationService service; Long user,salle;
    LocalDateTime debut=LocalDateTime.now().plusDays(3).withNano(0);
    @BeforeEach void setup() throws Exception {
        String url="jdbc:h2:mem:test"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1";
        MigrationExecutor migration=new MigrationExecutor(url,"sa",""); migration.migrate(); migration.migrate();
        emf=Persistence.createEntityManagerFactory("ReservationPU",Map.of("javax.persistence.jdbc.url",url,"hibernate.show_sql","false"));
        service=new ReservationService(emf);
        Jpa.transaction(emf,em->{ Utilisateur u=new Utilisateur(); u.setNom("Test"); u.setEmail("test@example.com"); em.persist(u); user=u.getId();
            Salle s=new Salle(); s.setNom("Test A"); s.setCapacite(20); em.persist(s); salle=s.getId(); return null; });
    }
    @AfterEach void close() { if(emf!=null) emf.close(); }
    @Test void crudEtRequetes() {
        Long id=service.creer(user,salle,debut,debut.plusHours(1)); assertNotNull(id);
        assertEquals(1,service.rechercherJpql("Test A",debut.minusDays(1)).size());
        assertEquals(id,service.rechercherCriteria("Test A",debut.minusDays(1)).get(0).getId());
        service.modifier(id,user,salle,debut.plusHours(2),debut.plusHours(3));
        assertEquals(debut.plusHours(2),service.rechercherJpql("Test A",debut).get(0).getDateDebut());
        service.supprimer(id); assertTrue(service.rechercherCriteria("Test A",debut).isEmpty());
    }
    @Test void conflitEtCreneauxAdjacents() {
        service.creer(user,salle,debut,debut.plusHours(1));
        assertThrows(IllegalStateException.class,()->service.creer(user,salle,debut.plusMinutes(30),debut.plusHours(2)));
        assertNotNull(service.creer(user,salle,debut.plusHours(1),debut.plusHours(2)));
        assertEquals(2,service.rechercherJpql("Test A",debut.minusDays(1)).size());
    }
    @Test void erreurs() {
        assertThrows(IllegalArgumentException.class,()->service.creer(user,salle,debut,debut));
        assertThrows(IllegalArgumentException.class,()->service.creer(user,999L,debut,debut.plusHours(1)));
        assertThrows(PersistenceException.class,()->Jpa.transaction(emf,em->{ Utilisateur u=new Utilisateur(); u.setNom("Doublon"); u.setEmail("test@example.com"); em.persist(u); return null; }));
    }
    @Test void verrouOptimiste() {
        EntityManager a=emf.createEntityManager(), b=emf.createEntityManager();
        try {
            a.getTransaction().begin(); b.getTransaction().begin();
            Utilisateur ua=a.find(Utilisateur.class,user), ub=b.find(Utilisateur.class,user);
            ua.setNom("Premier"); a.getTransaction().commit(); ub.setNom("Second");
            assertThrows(OptimisticLockException.class, b::flush);
        } finally { if(a.getTransaction().isActive()) a.getTransaction().rollback(); if(b.getTransaction().isActive()) b.getTransaction().rollback(); a.close(); b.close(); }
    }
    @Test void cacheSecondNiveauEtRequetes() {
        var stats=emf.unwrap(SessionFactory.class).getStatistics(); emf.getCache().evictAll(); stats.clear();
        Jpa.transaction(emf,em->em.find(Salle.class,salle));
        long statements=stats.getPrepareStatementCount();
        Jpa.transaction(emf,em->em.find(Salle.class,salle));
        assertTrue(stats.getSecondLevelCacheHitCount()>0); assertEquals(statements,stats.getPrepareStatementCount());
        service.creer(user,salle,debut,debut.plusHours(1));
        service.rechercherJpql("Test A",debut.minusDays(1)); service.rechercherJpql("Test A",debut.minusDays(1));
        assertTrue(stats.getQueryCacheHitCount()>0);
        service.creer(user,salle,debut.plusHours(2),debut.plusHours(3));
        assertEquals(2,service.rechercherJpql("Test A",debut.minusDays(1)).size());
    }
    @Test void creationsConcurrentes() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> task=()->{ start.await(); try {service.creer(user,salle,debut,debut.plusHours(1));return true;} catch(IllegalStateException e) {return false;} };
        try { Future<Boolean> a=pool.submit(task), b=pool.submit(task); start.countDown();
            assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));
            assertEquals(1,service.rechercherJpql("Test A",debut.minusDays(1)).size());
        } finally { pool.shutdownNow(); }
    }
    @Test void initialisationRejouable() {
        Jpa.transaction(emf,em->{em.remove(em.find(Utilisateur.class,user));return null;});
        DataInitializer init=new DataInitializer(emf); init.initData(); init.initData();
        long count=Jpa.transaction(emf,em->em.createQuery("select count(r) from Reservation r",Long.class).getSingleResult());
        assertEquals(20L,count);
    }
    @Test void migrationAbsente() {
        assertThrows(java.io.FileNotFoundException.class,()->new MigrationExecutor("jdbc:h2:mem:missing","sa","").executeMigration("absent.sql"));
    }
}
