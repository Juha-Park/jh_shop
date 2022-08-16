package com.shop.service;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final AmazonS3Client amazonS3Client;

    //버킷 이름 동적 할당
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String mkFileName(String oriImgName, MultipartFile itemImgFile) {

        //서로 다른 개체들을 구별하기 위해서는 이름을 부여해야 하므로 UUID(Universally Unique Identifier) 사용.(파일명 중복 문제 해결.)
        UUID uuid = UUID.randomUUID();
        //확장자를 찾기 위한 코드.
        String extension = oriImgName.substring(oriImgName.lastIndexOf("."));
        //UUID로 받은 값과 원래 파일의 이름의 확장자를 조합하여 저장될 파일 이름을 만듦.
        String fileName = uuid.toString() + extension;
        return fileName;
    }

    public String uploadFile(String oriImgName, MultipartFile itemImgFile) throws IOException {

        //S3에 Multipartfile 타입은 전송이 안 되므로 MultiPartFile을 File로 전환.
        File uploadFile = convert(itemImgFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환이 실패했습니다."));
        String uploadImageUrl = putS3(uploadFile, mkFileName(oriImgName, itemImgFile));
        removeNewFile(uploadFile);
        //업로드된 파일의 S3 url 주소를 반환.
        return uploadImageUrl;
    }

    //Spring Boot Cloud AWS를 사용하게 되면 S3 관련 Bean을 자동으로 생성해주므로, @Configuration 없이 AmazonS3Client를 DI 받음.
    //외부에서 정적 파일을 읽을 수 있도록 하기 위해서, 전환된 File을 S3에 public 읽기 권한으로 넣어줌.
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    //Multipartfile이 File로 전환되면서 로컬에 생성된 File을 삭제.
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    public void deleteFile(String filePath) throws Exception{

        //파일이 저장된 경로를 이용하여 파일 객체를 생성.
        File deleteFile = new File(filePath);

        //해당 파일이 존재하면 파일을 삭제.
        if(deleteFile.exists()){
            deleteFile.delete();
            log.info("파일을 삭제하였습니다.");
        } else{
            log.info("파일이 존재하지 않습니다.");
        }
    }
}
