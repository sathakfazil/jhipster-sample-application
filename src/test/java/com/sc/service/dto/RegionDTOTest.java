package com.sc.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.sc.web.rest.TestUtil;

public class RegionDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RegionDTO.class);
        RegionDTO regionDTO1 = new RegionDTO();
        regionDTO1.setId(1L);
        RegionDTO regionDTO2 = new RegionDTO();
        assertThat(regionDTO1).isNotEqualTo(regionDTO2);
        regionDTO2.setId(regionDTO1.getId());
        assertThat(regionDTO1).isEqualTo(regionDTO2);
        regionDTO2.setId(2L);
        assertThat(regionDTO1).isNotEqualTo(regionDTO2);
        regionDTO1.setId(null);
        assertThat(regionDTO1).isNotEqualTo(regionDTO2);
    }
}
