package com.sc.web.rest;

import com.sc.JhipsterSampleApplicationApp;
import com.sc.domain.Job;
import com.sc.domain.Task;
import com.sc.domain.Employee;
import com.sc.repository.JobRepository;
import com.sc.repository.search.JobSearchRepository;
import com.sc.service.JobService;
import com.sc.service.dto.JobDTO;
import com.sc.service.mapper.JobMapper;
import com.sc.web.rest.errors.ExceptionTranslator;
import com.sc.service.dto.JobCriteria;
import com.sc.service.JobQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sc.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link JobResource} REST controller.
 */
@SpringBootTest(classes = JhipsterSampleApplicationApp.class)
public class JobResourceIT {

    private static final String DEFAULT_JOB_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_JOB_TITLE = "BBBBBBBBBB";

    private static final Long DEFAULT_MIN_SALARY = 1L;
    private static final Long UPDATED_MIN_SALARY = 2L;
    private static final Long SMALLER_MIN_SALARY = 1L - 1L;

    private static final Long DEFAULT_MAX_SALARY = 1L;
    private static final Long UPDATED_MAX_SALARY = 2L;
    private static final Long SMALLER_MAX_SALARY = 1L - 1L;

    @Autowired
    private JobRepository jobRepository;

    @Mock
    private JobRepository jobRepositoryMock;

    @Autowired
    private JobMapper jobMapper;

    @Mock
    private JobService jobServiceMock;

    @Autowired
    private JobService jobService;

    /**
     * This repository is mocked in the com.sc.repository.search test package.
     *
     * @see com.sc.repository.search.JobSearchRepositoryMockConfiguration
     */
    @Autowired
    private JobSearchRepository mockJobSearchRepository;

    @Autowired
    private JobQueryService jobQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restJobMockMvc;

    private Job job;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final JobResource jobResource = new JobResource(jobService, jobQueryService);
        this.restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createEntity(EntityManager em) {
        Job job = new Job()
            .jobTitle(DEFAULT_JOB_TITLE)
            .minSalary(DEFAULT_MIN_SALARY)
            .maxSalary(DEFAULT_MAX_SALARY);
        return job;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createUpdatedEntity(EntityManager em) {
        Job job = new Job()
            .jobTitle(UPDATED_JOB_TITLE)
            .minSalary(UPDATED_MIN_SALARY)
            .maxSalary(UPDATED_MAX_SALARY);
        return job;
    }

    @BeforeEach
    public void initTest() {
        job = createEntity(em);
    }

    @Test
    @Transactional
    public void createJob() throws Exception {
        int databaseSizeBeforeCreate = jobRepository.findAll().size();

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);
        restJobMockMvc.perform(post("/api/jobs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(jobDTO)))
            .andExpect(status().isCreated());

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate + 1);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(DEFAULT_JOB_TITLE);
        assertThat(testJob.getMinSalary()).isEqualTo(DEFAULT_MIN_SALARY);
        assertThat(testJob.getMaxSalary()).isEqualTo(DEFAULT_MAX_SALARY);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).save(testJob);
    }

    @Test
    @Transactional
    public void createJobWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = jobRepository.findAll().size();

        // Create the Job with an existing ID
        job.setId(1L);
        JobDTO jobDTO = jobMapper.toDto(job);

        // An entity with an existing ID cannot be created, so this API call must fail
        restJobMockMvc.perform(post("/api/jobs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(jobDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(0)).save(job);
    }


    @Test
    @Transactional
    public void getAllJobs() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList
        restJobMockMvc.perform(get("/api/jobs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(job.getId().intValue())))
            .andExpect(jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE)))
            .andExpect(jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.intValue())))
            .andExpect(jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.intValue())));
    }
    
    @SuppressWarnings({"unchecked"})
    public void getAllJobsWithEagerRelationshipsIsEnabled() throws Exception {
        JobResource jobResource = new JobResource(jobServiceMock, jobQueryService);
        when(jobServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restJobMockMvc.perform(get("/api/jobs?eagerload=true"))
        .andExpect(status().isOk());

        verify(jobServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllJobsWithEagerRelationshipsIsNotEnabled() throws Exception {
        JobResource jobResource = new JobResource(jobServiceMock, jobQueryService);
            when(jobServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
            MockMvc restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restJobMockMvc.perform(get("/api/jobs?eagerload=true"))
        .andExpect(status().isOk());

            verify(jobServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getJob() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get the job
        restJobMockMvc.perform(get("/api/jobs/{id}", job.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(job.getId().intValue()))
            .andExpect(jsonPath("$.jobTitle").value(DEFAULT_JOB_TITLE))
            .andExpect(jsonPath("$.minSalary").value(DEFAULT_MIN_SALARY.intValue()))
            .andExpect(jsonPath("$.maxSalary").value(DEFAULT_MAX_SALARY.intValue()));
    }


    @Test
    @Transactional
    public void getJobsByIdFiltering() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        Long id = job.getId();

        defaultJobShouldBeFound("id.equals=" + id);
        defaultJobShouldNotBeFound("id.notEquals=" + id);

        defaultJobShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultJobShouldNotBeFound("id.greaterThan=" + id);

        defaultJobShouldBeFound("id.lessThanOrEqual=" + id);
        defaultJobShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllJobsByJobTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle equals to DEFAULT_JOB_TITLE
        defaultJobShouldBeFound("jobTitle.equals=" + DEFAULT_JOB_TITLE);

        // Get all the jobList where jobTitle equals to UPDATED_JOB_TITLE
        defaultJobShouldNotBeFound("jobTitle.equals=" + UPDATED_JOB_TITLE);
    }

    @Test
    @Transactional
    public void getAllJobsByJobTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle not equals to DEFAULT_JOB_TITLE
        defaultJobShouldNotBeFound("jobTitle.notEquals=" + DEFAULT_JOB_TITLE);

        // Get all the jobList where jobTitle not equals to UPDATED_JOB_TITLE
        defaultJobShouldBeFound("jobTitle.notEquals=" + UPDATED_JOB_TITLE);
    }

    @Test
    @Transactional
    public void getAllJobsByJobTitleIsInShouldWork() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle in DEFAULT_JOB_TITLE or UPDATED_JOB_TITLE
        defaultJobShouldBeFound("jobTitle.in=" + DEFAULT_JOB_TITLE + "," + UPDATED_JOB_TITLE);

        // Get all the jobList where jobTitle equals to UPDATED_JOB_TITLE
        defaultJobShouldNotBeFound("jobTitle.in=" + UPDATED_JOB_TITLE);
    }

    @Test
    @Transactional
    public void getAllJobsByJobTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle is not null
        defaultJobShouldBeFound("jobTitle.specified=true");

        // Get all the jobList where jobTitle is null
        defaultJobShouldNotBeFound("jobTitle.specified=false");
    }
                @Test
    @Transactional
    public void getAllJobsByJobTitleContainsSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle contains DEFAULT_JOB_TITLE
        defaultJobShouldBeFound("jobTitle.contains=" + DEFAULT_JOB_TITLE);

        // Get all the jobList where jobTitle contains UPDATED_JOB_TITLE
        defaultJobShouldNotBeFound("jobTitle.contains=" + UPDATED_JOB_TITLE);
    }

    @Test
    @Transactional
    public void getAllJobsByJobTitleNotContainsSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where jobTitle does not contain DEFAULT_JOB_TITLE
        defaultJobShouldNotBeFound("jobTitle.doesNotContain=" + DEFAULT_JOB_TITLE);

        // Get all the jobList where jobTitle does not contain UPDATED_JOB_TITLE
        defaultJobShouldBeFound("jobTitle.doesNotContain=" + UPDATED_JOB_TITLE);
    }


    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary equals to DEFAULT_MIN_SALARY
        defaultJobShouldBeFound("minSalary.equals=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary equals to UPDATED_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.equals=" + UPDATED_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary not equals to DEFAULT_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.notEquals=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary not equals to UPDATED_MIN_SALARY
        defaultJobShouldBeFound("minSalary.notEquals=" + UPDATED_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsInShouldWork() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary in DEFAULT_MIN_SALARY or UPDATED_MIN_SALARY
        defaultJobShouldBeFound("minSalary.in=" + DEFAULT_MIN_SALARY + "," + UPDATED_MIN_SALARY);

        // Get all the jobList where minSalary equals to UPDATED_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.in=" + UPDATED_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary is not null
        defaultJobShouldBeFound("minSalary.specified=true");

        // Get all the jobList where minSalary is null
        defaultJobShouldNotBeFound("minSalary.specified=false");
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary is greater than or equal to DEFAULT_MIN_SALARY
        defaultJobShouldBeFound("minSalary.greaterThanOrEqual=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary is greater than or equal to UPDATED_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.greaterThanOrEqual=" + UPDATED_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary is less than or equal to DEFAULT_MIN_SALARY
        defaultJobShouldBeFound("minSalary.lessThanOrEqual=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary is less than or equal to SMALLER_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.lessThanOrEqual=" + SMALLER_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsLessThanSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary is less than DEFAULT_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.lessThan=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary is less than UPDATED_MIN_SALARY
        defaultJobShouldBeFound("minSalary.lessThan=" + UPDATED_MIN_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMinSalaryIsGreaterThanSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where minSalary is greater than DEFAULT_MIN_SALARY
        defaultJobShouldNotBeFound("minSalary.greaterThan=" + DEFAULT_MIN_SALARY);

        // Get all the jobList where minSalary is greater than SMALLER_MIN_SALARY
        defaultJobShouldBeFound("minSalary.greaterThan=" + SMALLER_MIN_SALARY);
    }


    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary equals to DEFAULT_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.equals=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary equals to UPDATED_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.equals=" + UPDATED_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary not equals to DEFAULT_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.notEquals=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary not equals to UPDATED_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.notEquals=" + UPDATED_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsInShouldWork() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary in DEFAULT_MAX_SALARY or UPDATED_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.in=" + DEFAULT_MAX_SALARY + "," + UPDATED_MAX_SALARY);

        // Get all the jobList where maxSalary equals to UPDATED_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.in=" + UPDATED_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary is not null
        defaultJobShouldBeFound("maxSalary.specified=true");

        // Get all the jobList where maxSalary is null
        defaultJobShouldNotBeFound("maxSalary.specified=false");
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary is greater than or equal to DEFAULT_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.greaterThanOrEqual=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary is greater than or equal to UPDATED_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.greaterThanOrEqual=" + UPDATED_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary is less than or equal to DEFAULT_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.lessThanOrEqual=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary is less than or equal to SMALLER_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.lessThanOrEqual=" + SMALLER_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsLessThanSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary is less than DEFAULT_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.lessThan=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary is less than UPDATED_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.lessThan=" + UPDATED_MAX_SALARY);
    }

    @Test
    @Transactional
    public void getAllJobsByMaxSalaryIsGreaterThanSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        // Get all the jobList where maxSalary is greater than DEFAULT_MAX_SALARY
        defaultJobShouldNotBeFound("maxSalary.greaterThan=" + DEFAULT_MAX_SALARY);

        // Get all the jobList where maxSalary is greater than SMALLER_MAX_SALARY
        defaultJobShouldBeFound("maxSalary.greaterThan=" + SMALLER_MAX_SALARY);
    }


    @Test
    @Transactional
    public void getAllJobsByTaskIsEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);
        Task task = TaskResourceIT.createEntity(em);
        em.persist(task);
        em.flush();
        job.addTask(task);
        jobRepository.saveAndFlush(job);
        Long taskId = task.getId();

        // Get all the jobList where task equals to taskId
        defaultJobShouldBeFound("taskId.equals=" + taskId);

        // Get all the jobList where task equals to taskId + 1
        defaultJobShouldNotBeFound("taskId.equals=" + (taskId + 1));
    }


    @Test
    @Transactional
    public void getAllJobsByEmployeeIsEqualToSomething() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);
        Employee employee = EmployeeResourceIT.createEntity(em);
        em.persist(employee);
        em.flush();
        job.setEmployee(employee);
        jobRepository.saveAndFlush(job);
        Long employeeId = employee.getId();

        // Get all the jobList where employee equals to employeeId
        defaultJobShouldBeFound("employeeId.equals=" + employeeId);

        // Get all the jobList where employee equals to employeeId + 1
        defaultJobShouldNotBeFound("employeeId.equals=" + (employeeId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultJobShouldBeFound(String filter) throws Exception {
        restJobMockMvc.perform(get("/api/jobs?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(job.getId().intValue())))
            .andExpect(jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE)))
            .andExpect(jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.intValue())))
            .andExpect(jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.intValue())));

        // Check, that the count call also returns 1
        restJobMockMvc.perform(get("/api/jobs/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultJobShouldNotBeFound(String filter) throws Exception {
        restJobMockMvc.perform(get("/api/jobs?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restJobMockMvc.perform(get("/api/jobs/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingJob() throws Exception {
        // Get the job
        restJobMockMvc.perform(get("/api/jobs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateJob() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        int databaseSizeBeforeUpdate = jobRepository.findAll().size();

        // Update the job
        Job updatedJob = jobRepository.findById(job.getId()).get();
        // Disconnect from session so that the updates on updatedJob are not directly saved in db
        em.detach(updatedJob);
        updatedJob
            .jobTitle(UPDATED_JOB_TITLE)
            .minSalary(UPDATED_MIN_SALARY)
            .maxSalary(UPDATED_MAX_SALARY);
        JobDTO jobDTO = jobMapper.toDto(updatedJob);

        restJobMockMvc.perform(put("/api/jobs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(jobDTO)))
            .andExpect(status().isOk());

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(UPDATED_JOB_TITLE);
        assertThat(testJob.getMinSalary()).isEqualTo(UPDATED_MIN_SALARY);
        assertThat(testJob.getMaxSalary()).isEqualTo(UPDATED_MAX_SALARY);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).save(testJob);
    }

    @Test
    @Transactional
    public void updateNonExistingJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().size();

        // Create the Job
        JobDTO jobDTO = jobMapper.toDto(job);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobMockMvc.perform(put("/api/jobs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(jobDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(0)).save(job);
    }

    @Test
    @Transactional
    public void deleteJob() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);

        int databaseSizeBeforeDelete = jobRepository.findAll().size();

        // Delete the job
        restJobMockMvc.perform(delete("/api/jobs/{id}", job.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Job> jobList = jobRepository.findAll();
        assertThat(jobList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).deleteById(job.getId());
    }

    @Test
    @Transactional
    public void searchJob() throws Exception {
        // Initialize the database
        jobRepository.saveAndFlush(job);
        when(mockJobSearchRepository.search(queryStringQuery("id:" + job.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(job), PageRequest.of(0, 1), 1));
        // Search the job
        restJobMockMvc.perform(get("/api/_search/jobs?query=id:" + job.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(job.getId().intValue())))
            .andExpect(jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE)))
            .andExpect(jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.intValue())))
            .andExpect(jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.intValue())));
    }
}
