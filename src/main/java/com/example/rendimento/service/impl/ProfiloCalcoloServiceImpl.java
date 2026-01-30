package com.example.rendimento.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.ProfiloCalcoloRepository;
import com.example.rendimento.service.ProfiloCalcoloService;

import jakarta.transaction.Transactional;

/**
 * Implementazione del servizio ProfiloCalcoloService.
 */
@Service
public class ProfiloCalcoloServiceImpl implements ProfiloCalcoloService {

    private static final Logger log = LoggerFactory.getLogger(ProfiloCalcoloServiceImpl.class);
    private static final String PROFILO_PREDEFINITO_NOME = "Profilo Predefinito";

    @Autowired
    private ProfiloCalcoloRepository profiloCalcoloRepository;
    

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfiloCalcolo> getAllProfiliCalcolo() {
        log.debug("Recupero di tutti i profili di calcolo");
        return profiloCalcoloRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProfiloCalcolo> getProfiloCalcoloById(Integer idProfilo) {
        log.debug("Recupero del profilo di calcolo con ID: {}", idProfilo);
        return profiloCalcoloRepository.findById(idProfilo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfiloCalcolo> getProfiliCalcoloByUtente(Utente utente) {
        log.debug("Recupero dei profili di calcolo per l'utente: {}", utente.getUsername());
        return profiloCalcoloRepository.findByUtente(utente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfiloCalcolo> getProfiliCalcoloByUtenteId(Integer idUtente) {
        log.debug("Recupero dei profili di calcolo per l'utente con ID: {}", idUtente);
        return profiloCalcoloRepository.findByUtenteIdUtente(idUtente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProfiloCalcolo> getProfiloCalcoloPredefinito(Utente utente) {
        log.debug("Recupero del profilo di calcolo predefinito per l'utente: {}", utente.getUsername());
        return profiloCalcoloRepository.findByUtenteAndIsDefaultTrue(utente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProfiloCalcolo> getProfiloCalcoloPredefinito(Integer idUtente) {
        log.debug("Recupero del profilo di calcolo predefinito per l'utente con ID: {}", idUtente);
        return profiloCalcoloRepository.findDefaultByUtenteId(idUtente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProfiloCalcolo saveProfiloCalcolo(ProfiloCalcolo profiloCalcolo) {
        log.debug("Salvataggio del profilo di calcolo: {}", profiloCalcolo);
        
        // Se il profilo è impostato come predefinito, rimuovi il flag predefinito dagli altri profili dell'utente
        if (Boolean.TRUE.equals(profiloCalcolo.getIsDefault())) {
            resetDefaultProfiles(profiloCalcolo.getUtente().getIdUtente(), profiloCalcolo.getIdProfilo());
        }
        
        return profiloCalcoloRepository.save(profiloCalcolo);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProfiloCalcolo setProfiloCalcoloPredefinito(Integer idProfilo, Integer idUtente) {
        log.debug("Impostazione del profilo di calcolo con ID {} come predefinito per l'utente con ID: {}", idProfilo, idUtente);
        
        // Resetta tutti i profili predefiniti dell'utente
        resetDefaultProfiles(idUtente, idProfilo);
        
        // Imposta il profilo specificato come predefinito
        Optional<ProfiloCalcolo> profiloOpt = profiloCalcoloRepository.findById(idProfilo);
        if (profiloOpt.isPresent()) {
            ProfiloCalcolo profilo = profiloOpt.get();
            
            // Verifica che il profilo appartenga all'utente specificato
            if (!profilo.getUtente().getIdUtente().equals(idUtente)) {
                throw new IllegalArgumentException("Il profilo non appartiene all'utente specificato");
            }
            
            profilo.setIsDefault(true);
            return profiloCalcoloRepository.save(profilo);
        } else {
            throw new IllegalArgumentException("Profilo di calcolo non trovato con ID: " + idProfilo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteProfiloCalcolo(Integer idProfilo) {
        log.debug("Eliminazione del profilo di calcolo con ID: {}", idProfilo);
        
        Optional<ProfiloCalcolo> profiloOpt = profiloCalcoloRepository.findById(idProfilo);
        if (profiloOpt.isPresent()) {
            ProfiloCalcolo profilo = profiloOpt.get();
            
            // Se il profilo è predefinito, non permettere l'eliminazione se è l'unico profilo dell'utente
            if (Boolean.TRUE.equals(profilo.getIsDefault())) {
                long count = profiloCalcoloRepository.findByUtenteIdUtente(profilo.getUtente().getIdUtente()).size();
                if (count <= 1) {
                    throw new IllegalStateException("Impossibile eliminare l'unico profilo predefinito dell'utente");
                }
            }
            
            profiloCalcoloRepository.delete(profilo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProfiloCalcolo(Integer idUtente) {
        log.debug("Verifica se l'utente con ID {} ha almeno un profilo di calcolo", idUtente);
        return !profiloCalcoloRepository.findByUtenteIdUtente(idUtente).isEmpty();
    }
    
    /**
     * Resetta il flag predefinito per tutti i profili di un utente, tranne quello specificato.
     * 
     * @param idUtente l'ID dell'utente
     * @param idProfiloEscluso l'ID del profilo da escludere dal reset (può essere null)
     */
    private void resetDefaultProfiles(Integer idUtente, Integer idProfiloEscluso) {
        List<ProfiloCalcolo> profili = profiloCalcoloRepository.findByUtenteIdUtente(idUtente);
        
        for (ProfiloCalcolo profilo : profili) {
            if (Boolean.TRUE.equals(profilo.getIsDefault()) && 
                (idProfiloEscluso == null || !profilo.getIdProfilo().equals(idProfiloEscluso))) {
                profilo.setIsDefault(false);
                profiloCalcoloRepository.save(profilo);
            }
        }
    }
}