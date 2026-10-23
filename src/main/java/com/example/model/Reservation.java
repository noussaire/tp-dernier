package com.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CacheConcurrencyStrategy;
@Entity @Table(name="reservations")
@Cacheable @org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Reservation {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@Version private int version;
public Long getId() { return id; }
public int getVersion() { return version; }
@ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="salle_id", nullable=false) private Salle salle;
public Salle getSalle() { return salle; }
public void setSalle(Salle value) { this.salle = value; }
@ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="utilisateur_id", nullable=false) private Utilisateur utilisateur;
public Utilisateur getUtilisateur() { return utilisateur; }
public void setUtilisateur(Utilisateur value) { this.utilisateur = value; }
@Column(nullable=false) private LocalDateTime dateDebut;
public LocalDateTime getDateDebut() { return dateDebut; }
public void setDateDebut(LocalDateTime value) { this.dateDebut = value; }
@Column(nullable=false) private LocalDateTime dateFin;
public LocalDateTime getDateFin() { return dateFin; }
public void setDateFin(LocalDateTime value) { this.dateFin = value; }
@PrePersist @PreUpdate private void validate() { if (dateDebut == null || dateFin == null || !dateFin.isAfter(dateDebut)) throw new IllegalArgumentException("La fin doit suivre le debut."); }
}