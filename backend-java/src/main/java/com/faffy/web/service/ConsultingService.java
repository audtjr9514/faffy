package com.faffy.web.service;

import com.faffy.web.dto.ConsultingCreateDto;
import com.faffy.web.dto.ConsultingGetDto;
import com.faffy.web.dto.HistoryConsultingDto;
import com.faffy.web.exception.IllegalInputException;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.util.List;

public interface ConsultingService {
    HistoryConsultingDto getHistoryConsulting(int no) throws IllegalInputException;
    File getSnapshot(int no) throws IllegalInputException;

    List<ConsultingGetDto> getConsultingsByViewCount(Pageable pageable);

    ConsultingGetDto createConsulting(ConsultingCreateDto consultingCreateDto, int no);
}
