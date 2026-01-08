package com.example.rendimento.dto;

import java.time.ZonedDateTime;

/**
 * DTO (Data Transfer Object) per l'entità AppMetadata.
 * Utilizzato per trasferire dati tra il livello di servizio e il livello di presentazione.
 */
public class AppMetadataDTO {

    private Long id;
    private String appName;
    private String appVersion;
    private ZonedDateTime createdAt;

    /**
     * Costruttore predefinito.
     */
    public AppMetadataDTO() {
    }

    /**
     * Costruttore con parametri.
     *
     * @param id l'ID dell'applicazione
     * @param appName il nome dell'applicazione
     * @param appVersion la versione dell'applicazione
     * @param createdAt la data di creazione
     */
    public AppMetadataDTO(Long id, String appName, String appVersion, ZonedDateTime createdAt) {
        this.id = id;
        this.appName = appName;
        this.appVersion = appVersion;
        this.createdAt = createdAt;
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

    @Override
    public String toString() {
        return "AppMetadataDTO{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}