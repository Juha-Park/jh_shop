package com.shop.service;


import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@Log
public class FileService {

    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{

        //서로 다른 개체들을 구별하기 위해서는 이름을 부여해야 하므로 UUID(Universally Unique Identifier) 사용.(파일명 중복 문제 해결.)
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        //UUID로 받은 값과 원래 파일의 이름의 확장자를 조합하여 저장될 파일 이름을 만듦.
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;
        //바이트 단위의 출력을 내보내는 FileOutputStream 클래스 사용.
        //생성자로 파일이 저장될 위치와 파일의 이름을 넘겨 파일에 쓸 파일 출력 스트림을 만듦.
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        //fileData를 파일 출력 스트림에 입력.
        fos.write(fileData);
        fos.close();
        //업로드된 파일의 이름을 반환.
        return savedFileName;
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
