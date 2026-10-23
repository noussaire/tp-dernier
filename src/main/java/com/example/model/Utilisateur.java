package com.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CacheConcurrencyStrategy;
@Entity @Table(name="utilisateurs")
@Cacheable @org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Utilisateur {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@Version private int version;
public Long getId() { return id; }
public int getVersion() { return version; }
@Column(nullable=false, unique=true) private String email;
public String getEmail() { return email; }
public void setEmail(String value) { this.email = value; }
@Column(nullable=false) private String nom;
public String getNom() { return nom; }
public void setNom(String value) { this.nom = value; }

}