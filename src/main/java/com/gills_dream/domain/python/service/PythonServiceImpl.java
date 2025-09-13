package com.gills_dream.domain.python.service;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.PythonProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonServiceImpl implements PythonService {
    private final PythonProperties pythonProperties;

    @Override
    @Transactional
    public PythonResponseDto.ProcessResultDto processVideo(PlanRequestDto.VideoUrlDto request){
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(pythonProperties.getBearerToken());

            HttpEntity<PlanRequestDto.VideoUrlDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PythonResponseDto.ProcessResultDto> response = restTemplate.exchange(
                    pythonProperties.getProcessVideo(),
                    HttpMethod.POST,
                    entity,
                    PythonResponseDto.ProcessResultDto.class
            );

            if (Objects.requireNonNull(response.getBody()).getVideoResultDtoList() == null || response.getBody().getVideoResultDtoList().isEmpty()) {
                throw new GeneralException(ErrorStatus.CANNOT_PROCESS_VIDEO);
            }

            return PythonResponseDto.ProcessResultDto.builder()
                    .videoResultDtoList(response.getBody().getVideoResultDtoList())
                    .build();

        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.PYTHON_SERVER_CONNECTION_ERROR);
        }
    }
}