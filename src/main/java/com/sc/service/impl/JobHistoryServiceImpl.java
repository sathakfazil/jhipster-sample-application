package com.sc.service.impl;

import com.sc.service.JobHistoryService;
import com.sc.domain.JobHistory;
import com.sc.repository.JobHistoryRepository;
import com.sc.repository.search.JobHistorySearchRepository;
import com.sc.service.dto.JobHistoryDTO;
import com.sc.service.mapper.JobHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link JobHistory}.
 */
@Service
@Transactional
public class JobHistoryServiceImpl implements JobHistoryService {

    private final Logger log = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    private final JobHistoryRepository jobHistoryRepository;

    private final JobHistoryMapper jobHistoryMapper;

    private final JobHistorySearchRepository jobHistorySearchRepository;

    public JobHistoryServiceImpl(JobHistoryRepository jobHistoryRepository, JobHistoryMapper jobHistoryMapper, JobHistorySearchRepository jobHistorySearchRepository) {
        this.jobHistoryRepository = jobHistoryRepository;
        this.jobHistoryMapper = jobHistoryMapper;
        this.jobHistorySearchRepository = jobHistorySearchRepository;
    }

    /**
     * Save a jobHistory.
     *
     * @param jobHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public JobHistoryDTO save(JobHistoryDTO jobHistoryDTO) {
        log.debug("Request to save JobHistory : {}", jobHistoryDTO);
        JobHistory jobHistory = jobHistoryMapper.toEntity(jobHistoryDTO);
        jobHistory = jobHistoryRepository.save(jobHistory);
        JobHistoryDTO result = jobHistoryMapper.toDto(jobHistory);
        jobHistorySearchRepository.save(jobHistory);
        return result;
    }

    /**
     * Get all the jobHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobHistoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all JobHistories");
        return jobHistoryRepository.findAll(pageable)
            .map(jobHistoryMapper::toDto);
    }


    /**
     * Get one jobHistory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<JobHistoryDTO> findOne(Long id) {
        log.debug("Request to get JobHistory : {}", id);
        return jobHistoryRepository.findById(id)
            .map(jobHistoryMapper::toDto);
    }

    /**
     * Delete the jobHistory by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete JobHistory : {}", id);
        jobHistoryRepository.deleteById(id);
        jobHistorySearchRepository.deleteById(id);
    }

    /**
     * Search for the jobHistory corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobHistoryDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of JobHistories for query {}", query);
        return jobHistorySearchRepository.search(queryStringQuery(query), pageable)
            .map(jobHistoryMapper::toDto);
    }
}
