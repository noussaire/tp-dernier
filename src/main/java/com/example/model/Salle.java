package com.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CacheConcurrencyStrategy;
@Entity @Table(name="salles")
@Cacheable @org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Salle {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@Version private int version;
public Long getId() { return id; }
public int getVersion() { return version; }
@Column(nullable=false, unique=true) private String nom;
public String getNom() { return nom; }
public void setNom(String value) { this.nom = value; }
@Column(nullable=false) private int capacite;
public int getCapacite() { return capacite; }
public void setCapacite(int value) { this.capacite = value; }
 private String description;
public String getDescription() { return description; }
public void setDescription(String value) { this.description = value; }

}