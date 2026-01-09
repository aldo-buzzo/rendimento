package com.example.rendimento.service.impl;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.mapper.TitoloMapper;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.TitoloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementazione dell'interfaccia TitoloService.
 */
@Service
public class TitoloServiceImpl implements TitoloService {

    @Autowired
    private TitoloRepository titoloRepository;
    
    @Autowired
    private TitoloMapper titoloMapper;

    @Override
    public List<TitoloDTO> getAllTitoli() {
        List<Titolo> titoli = titoloRepository.findAll();
        return titoloMapper.toDtoList(titoli);
    }

    @Override
    @Transactional
    public TitoloDTO saveTitolo(TitoloDTO titoloDTO) {
        // Creazione di un nuovo titolo
        Titolo titolo = titoloMapper.toEntity(titoloDTO);
        titolo = titoloRepository.save(titolo);
        return titoloMapper.toDto(titolo);
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
}