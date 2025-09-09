package com.gills_dream.domain.s3.service;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class S3ServiceImpl implements S3Service {
    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private static final Map<String, String> mimeToExtensionMap = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif",
            "image/bmp", "bmp",
            "image/svg+xml", "svg"
    );

    @Override
    @Transactional
    public String uploadImg(Member member, MultipartFile image) {
        try {
            String contentType = image.getContentType();
            String extension = Optional.ofNullable(mimeToExtensionMap.get(contentType))
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CANNOT_PROCESS_EXTENSION));

            String fileName = "profile/" + member.getId() + "_" + UUID.randomUUID() + "." + extension;

            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileName)
                    .contentType(image.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));

            return "https://" + s3Properties.getBucket() + ".s3." + s3Properties.getRegion() + ".amazonaws.com/" + fileName;
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.CANNOT_UPLOAD_IMAGE);
        }
    }

    @Override
    @Transactional
    public void deleteImg(String imageUrl) {
        try {
            // URL에서 key 추출
            String bucketUrl = "https://" + s3Properties.getBucket() + ".s3." + s3Properties.getRegion() + ".amazonaws.com/";
            if (!imageUrl.startsWith(bucketUrl)) {
                throw new GeneralException(ErrorStatus.CANNOT_FIND_IMAGE);
            }

            String key = imageUrl.substring(bucketUrl.length());

            // S3 삭제 요청
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.CANNOT_DELETE_IMAGE);
        }
    }
}
