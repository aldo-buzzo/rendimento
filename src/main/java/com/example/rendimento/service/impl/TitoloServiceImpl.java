package com.example.rendimento.service.impl;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.TitoloMapper;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.TitoloService;
import com.example.rendimento.service.factory.BorsaItalianaServiceFactory;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementazione dell'interfaccia TitoloService.
 */
@Service
public class TitoloServiceImpl implements TitoloService {

    private static final Logger log = LoggerFactory.getLogger(TitoloServiceImpl.class);
    
    @Autowired
    private TitoloRepository titoloRepository;
    
    @Autowired
    private TitoloMapper titoloMapper;
    
    @Autowired
    private BorsaItalianaServiceFactory borsaItalianaServiceFactory;

    @Override
    public List<TitoloDTO> getAllTitoli() {
        List<Titolo> titoli = titoloRepository.findAll();
        return titoloMapper.toDtoList(titoli);
    }

    @Override
    @Transactional
    public TitoloDTO saveTitolo(TitoloDTO titoloDTO) {
        try {
            // Creazione di un nuovo titolo
            Titolo titolo = titoloMapper.toEntity(titoloDTO);
            titolo = titoloRepository.save(titolo);
            return titoloMapper.toDto(titolo);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Gestione dell'eccezione di concorrenza ottimistica
            throw new ConflittoModificaException(
                "Il titolo è stato modificato da un altro utente. Ricarica e riprova.", e);
        }
    }

    @Override
    public TitoloDTO findById(Integer id) {
        return titoloRepository.findById(id)
                .map(titoloMapper::toDto)
                .orElse(null);
    }
    
    @Override
    public TitoloDTO findByCodiceIsin(String codiceIsin) {
        Titolo titolo = titoloRepository.findByCodiceIsin(codiceIsin);
        return titoloMapper.toDto(titolo);
    }

    @Override
    public boolean existsByCodiceIsin(String codiceIsin) {
        return titoloRepository.existsByCodiceIsin(codiceIsin);
    }

    @Override
    @Transactional
    public void deleteTitolo(Integer id) {
        titoloRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public TitoloDTO importaTitoloDaBorsaItaliana(String codiceIsin, String tipoTitoloStr) {
        log.info("Importazione titolo da Borsa Italiana - ISIN: {}, Tipo: {}", codiceIsin, tipoTitoloStr);
        
        try {
            // Converti la stringa del tipo titolo in enum
            TipoTitolo tipoTitolo = TipoTitolo.valueOf(tipoTitoloStr.toUpperCase());
            
            // Ottieni il servizio appropriato per il tipo di titolo
            BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
            
            // Recupera i dati del titolo da Borsa Italiana
            TitoloDTO titoloDTO;
            if (tipoTitolo == TipoTitolo.BTP) {
                titoloDTO = service.getTitoloByIsin(codiceIsin);
            } else if (tipoTitolo == TipoTitolo.BOT) {
                titoloDTO = service.getTitoloBotByIsin(codiceIsin);
            } else {
                throw new IllegalArgumentException("Tipo titolo non supportato: " + tipoTitolo);
            }
            
            if (titoloDTO == null) {
                log.error("Titolo non trovato su Borsa Italiana - ISIN: {}, Tipo: {}", codiceIsin, tipoTitoloStr);
                throw new IllegalArgumentException("Titolo non trovato su Borsa Italiana");
            }
            
            // Assicurati che il tasso sia un numero (se N/A, imposta a 0)
            if (titoloDTO.getTassoNominale() == null) {
                titoloDTO.setTassoNominale(BigDecimal.ZERO);
                log.info("Tasso nominale non disponibile, impostato a 0 - ISIN: {}", codiceIsin);
            }
            
            // Imposta il tipo di titolo
            titoloDTO.setTipoTitolo(tipoTitolo);
            
            // Imposta la periodicità delle cedole se non presente
            if (titoloDTO.getPeriodicitaCedole() == null) {
                titoloDTO.setPeriodicitaCedole("SEMESTRALE");
                log.info("Periodicità cedole non disponibile, impostata a SEMESTRALE - ISIN: {}", codiceIsin);
            }
            
            // Imposta la periodicità del bollo
            titoloDTO.setPeriodicitaBollo("ANNUALE");
            
            // Salva il titolo nel database
            TitoloDTO savedTitolo = saveTitolo(titoloDTO);
            log.info("Titolo importato con successo - ISIN: {}, ID: {}", codiceIsin, savedTitolo.getIdTitolo());
            
            return savedTitolo;
        } catch (IllegalArgumentException e) {
            log.error("Errore durante l'importazione del titolo - ISIN: {}, Tipo: {}, Errore: {}", 
                    codiceIsin, tipoTitoloStr, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore imprevisto durante l'importazione del titolo - ISIN: {}, Tipo: {}, Errore: {}", 
                    codiceIsin, tipoTitoloStr, e.getMessage());
            throw new RuntimeException("Errore durante l'importazione del titolo", e);
        }
    }
}
