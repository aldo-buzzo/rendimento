package com.example.rendimento.context;

import com.example.rendimento.model.ProfiloCalcolo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe che mantiene il contesto dell'utente, inclusi i profili di calcolo.
 */
public class UserContext {
    
    // Mappa statica che associa l'ID utente alla lista dei suoi profili
    public static final Map<Integer, List<ProfiloCalcolo>> USER_PROFILES = new ConcurrentHashMap<>();
    
    // Costruttore privato per impedire l'istanziazione
    private UserContext() {}
    
    /**
     * Imposta i profili per un utente.
     * 
     * @param userId l'ID dell'utente
     * @param profiles la lista dei profili dell'utente
     */
    public static void setProfiles(Integer userId, List<ProfiloCalcolo> profiles) {
        if (userId != null && profiles != null) {
            USER_PROFILES.put(userId, profiles);
        }
    }
    
    /**
     * Recupera i profili di un utente.
     * 
     * @param userId l'ID dell'utente
     * @return la lista dei profili dell'utente, o una lista vuota se non trovati
     */
    public static List<ProfiloCalcolo> getProfiles(Integer userId) {
        return userId != null ? USER_PROFILES.getOrDefault(userId, Collections.emptyList()) : Collections.emptyList();
    }
    
    /**
     * Rimuove i profili di un utente.
     * 
     * @param userId l'ID dell'utente
     */
    public static void removeProfiles(Integer userId) {
        if (userId != null) {
            USER_PROFILES.remove(userId);
        }
    }
    
    /**
     * Recupera il profilo predefinito di un utente.
     * Se esiste un solo profilo, questo viene assunto come profilo predefinito.
     * Se esistono due o più profili e nessuno è settato come predefinito, viene lanciata un'eccezione.
     * 
     * @param userId l'ID dell'utente
     * @return il profilo predefinito dell'utente
     * @throws IllegalStateException se ci sono più profili ma nessuno è impostato come predefinito
     */
    public static ProfiloCalcolo getDefaultProfile(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        
        List<ProfiloCalcolo> profiles = getProfiles(userId);
        
        if (profiles.isEmpty()) {
            throw new IllegalStateException("L'utente non ha profili di calcolo");
        }
        
        // Se c'è un solo profilo, restituiscilo come predefinito
        if (profiles.size() == 1) {
            return profiles.get(0);
        }
        
        // Se ci sono più profili, cerca quello predefinito
        Optional<ProfiloCalcolo> defaultProfile = profiles.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsDefault()))
                .findFirst();
        
        // Se esiste un profilo predefinito, restituiscilo
        if (defaultProfile.isPresent()) {
            return defaultProfile.get();
        }
        
        // Se ci sono più profili ma nessuno è predefinito, lancia un'eccezione
        throw new IllegalStateException("L'utente ha più profili ma nessuno è impostato come predefinito");
    }
}
