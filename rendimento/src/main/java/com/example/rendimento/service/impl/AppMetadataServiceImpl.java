package com.example.rendimento.service.impl;

import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.mapper.AppMetadataMapper;
import com.example.rendimento.model.AppMetadata;
import com.example.rendimento.repository.AppMetadataRepository;
import com.example.rendimento.service.AppMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementazione del servizio AppMetadataService.
 */
@Service
public class AppMetadataServiceImpl implements AppMetadataService {

    @Autowired
    private AppMetadataRepository appMetadataRepository;
    
    @Autowired
    private AppMetadataMapper appMetadataMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AppMetadataDTO> getAllAppMetadata() {
        List<AppMetadata> entities = appMetadataRepository.findAll();
        return appMetadataMapper.toDtoList(entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppMetadataDTO getAppMetadataByName(String appName) {
        AppMetadata entity = appMetadataRepository.findByAppName(appName);
        return appMetadataMapper.toDto(entity);
    }
}
