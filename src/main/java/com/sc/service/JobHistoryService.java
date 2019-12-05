package com.sc.service;

import com.sc.service.dto.JobHistoryDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.sc.domain.JobHistory}.
 */
public interface JobHistoryService {

    /**
     * Save a jobHistory.
     *
     * @param jobHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    JobHistoryDTO save(JobHistoryDTO jobHistoryDTO);

    /**
     * Get all the jobHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JobHistoryDTO> findAll(Pageable pageable);


    /**
     * Get the "id" jobHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<JobHistoryDTO> findOne(Long id);

    /**
     * Delete the "id" jobHistory.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the jobHistory corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JobHistoryDTO> search(String query, Pageable pageable);
}
