package com.sc.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class JobMapperTest {

    private JobMapper jobMapper;

    @BeforeEach
    public void setUp() {
        jobMapper = new JobMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 2L;
        assertThat(jobMapper.fromId(id).getId()).isEqualTo(id);
        assertThat(jobMapper.fromId(null)).isNull();
    }
}
