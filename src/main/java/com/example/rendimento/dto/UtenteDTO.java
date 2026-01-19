package com.example.rendimento.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) per l'entità Utente.
 * Utilizzato per trasferire dati tra il livello di servizio e il livello di presentazione.
 */
public class UtenteDTO {

    private Integer idUtente;
    private String username;
    private String password;
    private String email;
    private String nome;
    private String cognome;
    private LocalDateTime dataRegistrazione;
    private Boolean isSystemUser;
    
    /**
     * Costruttore predefinito.
     */
    public UtenteDTO() {
    }
    
    /**
     * Costruttore con parametri.
     *
     * @param idUtente l'ID dell'utente
     * @param username il nome utente
     * @param email l'indirizzo email
     * @param nome il nome dell'utente
     * @param cognome il cognome dell'utente
     * @param dataRegistrazione la data di registrazione
     */
    public UtenteDTO(Integer idUtente, String username, String email, String nome, String cognome, LocalDateTime dataRegistrazione) {
        this.idUtente = idUtente;
        this.username = username;
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.dataRegistrazione = dataRegistrazione;
    }
    
    /**
     * Costruttore con parametri incluso isSystemUser.
     *
     * @param idUtente l'ID dell'utente
     * @param username il nome utente
     * @param email l'indirizzo email
     * @param nome il nome dell'utente
     * @param cognome il cognome dell'utente
     * @param dataRegistrazione la data di registrazione
     * @param isSystemUser indica se l'utente è un utente di sistema
     */
    public UtenteDTO(Integer idUtente, String username, String email, String nome, String cognome, LocalDateTime dataRegistrazione, Boolean isSystemUser) {
        this.idUtente = idUtente;
        this.username = username;
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.dataRegistrazione = dataRegistrazione;
        this.isSystemUser = isSystemUser;
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
    
    @Override
    public String toString() {
        return "UtenteDTO{" +
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
