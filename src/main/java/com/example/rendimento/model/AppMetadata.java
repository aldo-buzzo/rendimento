package com.example.rendimento.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

/**
 * Classe entità JPA che rappresenta la tabella app_metadata nel database.
 * Implementa il controllo ottimistico della concorrenza tramite il campo version.
 */
@Entity
@Table(name = "app_metadata")
public class AppMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    @Column(name = "app_version", nullable = false, length = 20)
    private String appVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Costruttore predefinito richiesto da JPA.
     */
    public AppMetadata() {
    }

    /**
     * Costruttore con parametri per creare una nuova istanza di AppMetadata.
     *
     * @param appName    il nome dell'applicazione
     * @param appVersion la versione dell'applicazione
     */
    public AppMetadata(String appName, String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    // Getter e Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "AppMetadata{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", createdAt=" + createdAt +
                ", version=" + version +
                '}';
    }
}
