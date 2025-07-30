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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonServiceImpl implements PythonService {
    private final PythonProperties pythonProperties;

    @Override
    public List<String> process_youtube(PlanRequestDto.YoutubeUrlDto request) {
        try {
            RestClient restClient = RestClient.builder().build();

            PythonResponseDto.ProcessYoutubeDto response = restClient
                    .post()
                    .uri(pythonProperties.getProcessYoutube())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PythonResponseDto.ProcessYoutubeDto.class);

            return  response.getPlaceList();

        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus.PYTHON_SERVER_CONNECTION_ERROR);
        }
    }
}
