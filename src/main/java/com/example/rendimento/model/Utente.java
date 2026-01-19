package com.example.rendimento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità che rappresenta un utente del sistema.
 */
@Entity
@Table(name = "utente")
public class Utente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utente")
    private Integer idUtente;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "cognome")
    private String cognome;
    
    @Column(name = "data_registrazione", nullable = false)
    private LocalDateTime dataRegistrazione;
    
    @Column(name = "is_system_user")
    private Boolean isSystemUser = false;
    
    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Titolo> titoli = new HashSet<>();
    
    /**
     * Costruttore predefinito.
     */
    public Utente() {
    }
    
    /**
     * Costruttore con parametri.
     *
     * @param username il nome utente
     * @param password la password (già codificata)
     * @param email l'indirizzo email
     * @param nome il nome dell'utente
     * @param cognome il cognome dell'utente
     * @param dataRegistrazione la data di registrazione
     */
    public Utente(String username, String password, String email, String nome, String cognome, LocalDateTime dataRegistrazione) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.dataRegistrazione = dataRegistrazione;
    }
    
    /**
     * Aggiunge un titolo all'utente.
     *
     * @param titolo il titolo da aggiungere
     */
    public void addTitolo(Titolo titolo) {
        titoli.add(titolo);
        titolo.setUtente(this);
    }
    
    /**
     * Rimuove un titolo dall'utente.
     *
     * @param titolo il titolo da rimuovere
     */
    public void removeTitolo(Titolo titolo) {
        titoli.remove(titolo);
        titolo.setUtente(null);
    }
    
    // Getter e Setter
    
    public Integer getIdUtente() {
        return idUtente;
    }
    
    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCognome() {
        return cognome;
    }
    
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    
    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }
    
    public void setDataRegistrazione(LocalDateTime dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }
    
    public Boolean getIsSystemUser() {
        return isSystemUser;
    }
    
    public void setIsSystemUser(Boolean isSystemUser) {
        this.isSystemUser = isSystemUser;
    }
    
    public Set<Titolo> getTitoli() {
        return titoli;
    }
    
    public void setTitoli(Set<Titolo> titoli) {
        this.titoli = titoli;
    }
    
    @Override
    public String toString() {
        return "Utente{" +
                "idUtente=" + idUtente +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", dataRegistrazione=" + dataRegistrazione +
                ", isSystemUser=" + isSystemUser +
                '}';
    }
}
