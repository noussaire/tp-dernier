package com.example.util;
import javax.persistence.*;
import java.util.function.Function;
import java.util.logging.*;
public final class Jpa {
    private Jpa() {}
    public static <T> T transaction(EntityManagerFactory emf, Function<EntityManager,T> action) {
        EntityManager em=emf.createEntityManager();
        try {
            em.getTransaction().begin(); T result=action.apply(em); em.getTransaction().commit(); return result;
        } catch(RuntimeException ex) {
            if(em.getTransaction().isActive()) em.getTransaction().rollback();
            Logger.getLogger(Jpa.class.getName()).log(Level.WARNING,"Transaction annulee",ex);
            throw ex;
        } finally { em.close(); }
    }
}
