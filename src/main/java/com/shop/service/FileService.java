package com.shop.service;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final AmazonS3Client amazonS3Client;

    //버킷 이름 동적 할당
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //버킷 주소 동적 할당
    @Value("${cloud.aws.s3.bucket.url}")
    private String uploadPath;

    public String uploadFile(String uploadPath, String originalFileName, MultipartFile itemImgFile) throws Exception{

        //서로 다른 개체들을 구별하기 위해서는 이름을 부여해야 하므로 UUID(Universally Unique Identifier) 사용.(파일명 중복 문제 해결.)
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        //UUID로 받은 값과 원래 파일의 이름의 확장자를 조합하여 저장될 파일 이름을 만듦.
        String savedFileName = uuid.toString() + extension;
        //시스템 환경에 관한 정보를 얻기 위해 System.getProperty 사용. (user.dir : 현재 작업 디렉토리)
        File file = new File(System.getProperty("user.dir") + savedFileName);
        //파일 변환
        itemImgFile.transferTo(file);
        //S3 파일 업로드
        uploadOnS3(savedFileName, file);
        //주소 할당
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;
        //바이트 단위의 출력을 내보내는 FileOutputStream 클래스 사용.
        //생성자로 파일이 저장될 위치와 파일의 이름을 넘겨 파일에 쓸 파일 출력 스트림을 만듦.
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        //fileData를 파일 출력 스트림에 입력.
        fos.write(itemImgFile.getBytes());
        fos.close();
        //업로드된 파일의 이름을 반환.
        return savedFileName;
    }

    private void uploadOnS3(final String savedFileName, final File file) {
        //AWS S3 전송 객체 생성
        final TransferManager transferManager = new TransferManager(this.amazonS3Client);
        //요청 객체 생성
        final PutObjectRequest request = new PutObjectRequest(bucket, savedFileName, file);
        //업로드 시도
        final Upload upload = transferManager.upload(request);

        try {
            upload.waitForCompletion();
        } catch (AmazonClientException amazonClientException) {
            log.error(amazonClientException.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
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
