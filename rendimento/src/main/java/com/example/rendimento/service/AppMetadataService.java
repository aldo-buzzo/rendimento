package com.example.rendimento.service;

import com.example.rendimento.dto.AppMetadataDTO;
import java.util.List;

/**
 * Interfaccia per il servizio che gestisce le operazioni relative all'entità AppMetadata.
 */
public interface AppMetadataService {
    
    /**
     * Recupera tutti i metadati delle applicazioni.
     * 
     * @return una lista di DTO contenenti tutti i metadati delle applicazioni
     */
    List<AppMetadataDTO> getAllAppMetadata();
    
    /**
     * Recupera i metadati di un'applicazione per nome.
     * 
     * @param appName il nome dell'applicazione
     * @return il DTO contenente i metadati dell'applicazione, o null se non trovati
     */
    AppMetadataDTO getAppMetadataByName(String appName);
}
