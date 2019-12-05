package com.sc.web.rest;

import com.sc.JhipsterSampleApplicationApp;
import com.sc.domain.Country;
import com.sc.repository.CountryRepository;
import com.sc.repository.search.CountrySearchRepository;
import com.sc.service.CountryService;
import com.sc.service.dto.CountryDTO;
import com.sc.service.mapper.CountryMapper;
import com.sc.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
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
 * Integration tests for the {@link CountryResource} REST controller.
 */
@SpringBootTest(classes = JhipsterSampleApplicationApp.class)
public class CountryResourceIT {

    private static final String DEFAULT_COUNTRY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY_NAME = "BBBBBBBBBB";

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private CountryService countryService;

    /**
     * This repository is mocked in the com.sc.repository.search test package.
     *
     * @see com.sc.repository.search.CountrySearchRepositoryMockConfiguration
     */
    @Autowired
    private CountrySearchRepository mockCountrySearchRepository;

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

    private MockMvc restCountryMockMvc;

    private Country country;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CountryResource countryResource = new CountryResource(countryService);
        this.restCountryMockMvc = MockMvcBuilders.standaloneSetup(countryResource)
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
    public static Country createEntity(EntityManager em) {
        Country country = new Country()
            .countryName(DEFAULT_COUNTRY_NAME);
        return country;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Country createUpdatedEntity(EntityManager em) {
        Country country = new Country()
            .countryName(UPDATED_COUNTRY_NAME);
        return country;
    }

    @BeforeEach
    public void initTest() {
        country = createEntity(em);
    }

    @Test
    @Transactional
    public void createCountry() throws Exception {
        int databaseSizeBeforeCreate = countryRepository.findAll().size();

        // Create the Country
        CountryDTO countryDTO = countryMapper.toDto(country);
        restCountryMockMvc.perform(post("/api/countries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryDTO)))
            .andExpect(status().isCreated());

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll();
        assertThat(countryList).hasSize(databaseSizeBeforeCreate + 1);
        Country testCountry = countryList.get(countryList.size() - 1);
        assertThat(testCountry.getCountryName()).isEqualTo(DEFAULT_COUNTRY_NAME);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(1)).save(testCountry);
    }

    @Test
    @Transactional
    public void createCountryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = countryRepository.findAll().size();

        // Create the Country with an existing ID
        country.setId(1L);
        CountryDTO countryDTO = countryMapper.toDto(country);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCountryMockMvc.perform(post("/api/countries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll();
        assertThat(countryList).hasSize(databaseSizeBeforeCreate);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(0)).save(country);
    }


    @Test
    @Transactional
    public void getAllCountries() throws Exception {
        // Initialize the database
        countryRepository.saveAndFlush(country);

        // Get all the countryList
        restCountryMockMvc.perform(get("/api/countries?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(country.getId().intValue())))
            .andExpect(jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME)));
    }
    
    @Test
    @Transactional
    public void getCountry() throws Exception {
        // Initialize the database
        countryRepository.saveAndFlush(country);

        // Get the country
        restCountryMockMvc.perform(get("/api/countries/{id}", country.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(country.getId().intValue()))
            .andExpect(jsonPath("$.countryName").value(DEFAULT_COUNTRY_NAME));
    }

    @Test
    @Transactional
    public void getNonExistingCountry() throws Exception {
        // Get the country
        restCountryMockMvc.perform(get("/api/countries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCountry() throws Exception {
        // Initialize the database
        countryRepository.saveAndFlush(country);

        int databaseSizeBeforeUpdate = countryRepository.findAll().size();

        // Update the country
        Country updatedCountry = countryRepository.findById(country.getId()).get();
        // Disconnect from session so that the updates on updatedCountry are not directly saved in db
        em.detach(updatedCountry);
        updatedCountry
            .countryName(UPDATED_COUNTRY_NAME);
        CountryDTO countryDTO = countryMapper.toDto(updatedCountry);

        restCountryMockMvc.perform(put("/api/countries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryDTO)))
            .andExpect(status().isOk());

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll();
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate);
        Country testCountry = countryList.get(countryList.size() - 1);
        assertThat(testCountry.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(1)).save(testCountry);
    }

    @Test
    @Transactional
    public void updateNonExistingCountry() throws Exception {
        int databaseSizeBeforeUpdate = countryRepository.findAll().size();

        // Create the Country
        CountryDTO countryDTO = countryMapper.toDto(country);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCountryMockMvc.perform(put("/api/countries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll();
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(0)).save(country);
    }

    @Test
    @Transactional
    public void deleteCountry() throws Exception {
        // Initialize the database
        countryRepository.saveAndFlush(country);

        int databaseSizeBeforeDelete = countryRepository.findAll().size();

        // Delete the country
        restCountryMockMvc.perform(delete("/api/countries/{id}", country.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Country> countryList = countryRepository.findAll();
        assertThat(countryList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(1)).deleteById(country.getId());
    }

    @Test
    @Transactional
    public void searchCountry() throws Exception {
        // Initialize the database
        countryRepository.saveAndFlush(country);
        when(mockCountrySearchRepository.search(queryStringQuery("id:" + country.getId())))
            .thenReturn(Collections.singletonList(country));
        // Search the country
        restCountryMockMvc.perform(get("/api/_search/countries?query=id:" + country.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(country.getId().intValue())))
            .andExpect(jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME)));
    }
}
