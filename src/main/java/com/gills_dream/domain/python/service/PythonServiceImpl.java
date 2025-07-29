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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonServiceImpl implements PythonService {
    private final PythonProperties pythonProperties;

    @Override
    public List<String> process_youtube(PlanRequestDto.YoutubeUrlDto request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PlanRequestDto.YoutubeUrlDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PythonResponseDto.ProcessYoutubeDto> response = restTemplate.exchange(
                    pythonProperties.getProcessYoutube(),
                    HttpMethod.POST,
                    entity,
                    PythonResponseDto.ProcessYoutubeDto.class
            );

            return  response.getBody().getPlaceList();

        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.PYTHON_SERVER_CONNECTION_ERROR);
        }
    }
}
