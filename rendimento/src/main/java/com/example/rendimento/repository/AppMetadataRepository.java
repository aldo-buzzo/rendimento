package com.example.rendimento.repository;

import com.example.rendimento.model.AppMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per l'entità AppMetadata.
 * Fornisce metodi per operazioni CRUD e query personalizzate sulla tabella app_metadata.
 */
@Repository
public interface AppMetadataRepository extends JpaRepository<AppMetadata, Long> {
    
    /**
     * Trova un'applicazione per nome.
     * 
     * @param appName il nome dell'applicazione da cercare
     * @return l'entità AppMetadata corrispondente, se esiste
     */
    AppMetadata findByAppName(String appName);
    
    /**
     * Trova un'applicazione per nome e versione.
     * 
     * @param appName il nome dell'applicazione
     * @param appVersion la versione dell'applicazione
     * @return l'entità AppMetadata corrispondente, se esiste
     */
    AppMetadata findByAppNameAndAppVersion(String appName, String appVersion);
    
    /**
     * Verifica se esiste un'applicazione con il nome specificato.
     * 
     * @param appName il nome dell'applicazione da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByAppName(String appName);
}