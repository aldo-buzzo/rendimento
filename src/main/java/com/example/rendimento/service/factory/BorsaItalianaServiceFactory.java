package com.example.rendimento.service.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.impl.BorsaItalianaBotServiceImpl;
import com.example.rendimento.service.impl.BorsaItalianaBtpServiceImpl;

/**
 * Factory per ottenere l'implementazione appropriata di BorsaItalianaService in base al tipo di titolo.
 * Questa factory sostituisce sia CorsoServiceFactory che CorsoTitoloServiceFactory.
 */
@Component
public class BorsaItalianaServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(BorsaItalianaServiceFactory.class);
    
    private final BorsaItalianaBtpServiceImpl borsaItalianaBtpService;
    private final BorsaItalianaBotServiceImpl borsaItalianaBotService;

    @Autowired
    public BorsaItalianaServiceFactory(BorsaItalianaBtpServiceImpl borsaItalianaBtpService, BorsaItalianaBotServiceImpl borsaItalianaBotService) {
        this.borsaItalianaBtpService = borsaItalianaBtpService;
        this.borsaItalianaBotService = borsaItalianaBotService;
    }

    /**
     * Restituisce l'implementazione appropriata di BorsaItalianaService in base al tipo di titolo.
     *
     * @param tipoTitolo il tipo di titolo
     * @return l'implementazione appropriata di BorsaItalianaService
     * @throws IllegalArgumentException se il tipo di titolo non è supportato
     */
    public BorsaItalianaService getBorsaItalianaService(TipoTitolo tipoTitolo) {
        logger.debug("Richiesta implementazione BorsaItalianaService per tipo titolo: {}", tipoTitolo);
        
        switch (tipoTitolo) {
            case BTP:
                return borsaItalianaBtpService;
            case BOT:
                return borsaItalianaBotService;
            default:
                logger.error("Tipo titolo non supportato: {}", tipoTitolo);
                throw new IllegalArgumentException("Tipo titolo non supportato: " + tipoTitolo);
        }
    }
    
    /**
     * Restituisce l'implementazione appropriata di BorsaItalianaService in base al tipo di titolo.
     *
     * @param tipoTitolo il tipo di titolo come stringa
     * @return l'implementazione appropriata di BorsaItalianaService
     * @throws IllegalArgumentException se il tipo di titolo non è supportato
     */
    public BorsaItalianaService getBorsaItalianaService(String tipoTitolo) {
        logger.debug("Richiesta implementazione BorsaItalianaService per tipo titolo (stringa): {}", tipoTitolo);
        
        if (tipoTitolo == null) {
            logger.error("Il tipo di titolo non può essere null");
            throw new IllegalArgumentException("Il tipo di titolo non può essere null");
        }
        
        try {
            TipoTitolo tipo = TipoTitolo.valueOf(tipoTitolo.toUpperCase());
            return getBorsaItalianaService(tipo);
        } catch (IllegalArgumentException e) {
            logger.error("Tipo di titolo non valido: {}", tipoTitolo, e);
            throw new IllegalArgumentException("Tipo di titolo non valido: " + tipoTitolo, e);
        }
    }
}
