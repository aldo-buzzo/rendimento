package com.example.rendimento.mapper;

import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test per la classe SimulazioneMapper.
 * Verifica la corretta conversione tra entità Simulazione e DTO SimulazioneDTO.
 */
public class SimulazioneMapperTest {

    @Mock
    private TitoloRepository titoloRepository;

    @InjectMocks
    private SimulazioneMapper simulazioneMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test conversione da entità a DTO con campo version")
    public void testToDTO() {
        // Arrange
        Titolo titolo = new Titolo();
        titolo.setIdTitolo(1);
        titolo.setNome("BTP-1nv29 5,25%");
        titolo.setCodiceIsin("IT0001278511");
        titolo.setDataScadenza(LocalDate.of(2029, 11, 1));
        titolo.setTassoNominale(new BigDecimal("5.25"));
        titolo.setPeriodicitaCedole("SEMESTRALE");
        titolo.setPeriodicitaBollo("ANNUALE");

        Simulazione simulazione = new Simulazione();
        simulazione.setIdSimulazione(1);
        simulazione.setTitolo(titolo);
        simulazione.setDataAcquisto(LocalDate.now());
        simulazione.setPrezzoAcquisto(new BigDecimal("109.8887"));
        simulazione.setRendimentoLordo(new BigDecimal("5.25"));
        simulazione.setRendimentoTassato(new BigDecimal("4.59375"));
        simulazione.setCommissioniAcquisto(new BigDecimal("0.0009"));
        simulazione.setRendimentoNettoCedole(new BigDecimal("4.5"));
        simulazione.setImpostaBollo(new BigDecimal("0.002"));
        simulazione.setRendimentoNettoBollo(new BigDecimal("4.4"));
        simulazione.setPlusMinusValenza(new BigDecimal("-9.8887"));
        simulazione.setVersion(42L); // Impostiamo un valore di versione

        // Act
        SimulazioneDTO dto = simulazioneMapper.toDTO(simulazione);

        // Assert
        assertNotNull(dto, "Il DTO non dovrebbe essere null");
        assertEquals(simulazione.getIdSimulazione(), dto.getIdSimulazione(), "L'ID della simulazione dovrebbe essere preservato");
        assertEquals(simulazione.getVersion(), dto.getVersion(), "Il campo version dovrebbe essere preservato");
        assertEquals(42L, dto.getVersion(), "Il campo version dovrebbe essere 42");
    }

    @Test
    @DisplayName("Test conversione da DTO a entità con campo version")
    public void testToEntity() {
        // Arrange
        SimulazioneDTO dto = new SimulazioneDTO();
        dto.setIdSimulazione(1);
        dto.setIdTitolo(1);
        dto.setDataAcquisto(LocalDate.now());
        dto.setPrezzoAcquisto(new BigDecimal("109.8887"));
        dto.setRendimentoLordo(new BigDecimal("5.25"));
        dto.setRendimentoTassato(new BigDecimal("4.59375"));
        dto.setCommissioniAcquisto(new BigDecimal("0.0009"));
        dto.setRendimentoNettoCedole(new BigDecimal("4.5"));
        dto.setImpostaBollo(new BigDecimal("0.002"));
        dto.setRendimentoNettoBollo(new BigDecimal("4.4"));
        dto.setPlusMinusValenza(new BigDecimal("-9.8887"));
        dto.setVersion(42L); // Impostiamo un valore di versione

        Titolo titolo = new Titolo();
        titolo.setIdTitolo(1);
        titolo.setNome("BTP-1nv29 5,25%");
        titolo.setCodiceIsin("IT0001278511");
        titolo.setDataScadenza(LocalDate.of(2029, 11, 1));
        titolo.setTassoNominale(new BigDecimal("5.25"));
        titolo.setPeriodicitaCedole("SEMESTRALE");
        titolo.setPeriodicitaBollo("ANNUALE");

        when(titoloRepository.findById(1)).thenReturn(Optional.of(titolo));

        // Act
        Simulazione simulazione = simulazioneMapper.toEntity(dto);

        // Assert
        assertNotNull(simulazione, "L'entità non dovrebbe essere null");
        assertEquals(dto.getIdSimulazione(), simulazione.getIdSimulazione(), "L'ID della simulazione dovrebbe essere preservato");
        assertEquals(dto.getVersion(), simulazione.getVersion(), "Il campo version dovrebbe essere preservato");
        assertEquals(42L, simulazione.getVersion(), "Il campo version dovrebbe essere 42");
    }
}
