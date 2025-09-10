package com.gills_dream.domain.s3.service;

import com.gills_dream.domain.member.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    String uploadImg(Member member, MultipartFile image);
    void deleteImg(String imgUrl);
}
