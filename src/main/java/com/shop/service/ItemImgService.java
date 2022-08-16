package com.shop.service;


import com.shop.entyty.ItemImg;
import com.shop.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;


@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${cloud.aws.s3.bucket.url}")
    private String uploadPath;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //파일 업로드
        if(!StringUtils.isEmpty(oriImgName)){

            //사용자가 상품의 이미지를 등록했다면 FileService 클래스의 uploadFile 메소드를 호출하여 변수 imgName에 저장.
            imgName = fileService.uploadFile(uploadPath, oriImgName, itemImgFile);
            //저장한 상품 이미지를 불러올 경로를 설정.
            imgUrl = "/images/item/" + imgName;
        }

        //상품 이미지 정보 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        itemImgRepository.save(itemImg);
    }

    //상품 이미지를 수정한 경우 상품 이미지를 업데이트.
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception {

        //상품 이미지 아이디를 이용하여 기존에 저장했던 상품 이미지 엔티티를 조회.
        if (!itemImgFile.isEmpty()) {
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);
            //기존 이미지 파일 삭제
            if (!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(uploadPath + "/" + savedItemImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            //업데이트한 상품 이미지 파일을 업로드.
            String imgName = fileService.uploadFile(uploadPath, oriImgName, itemImgFile);
            String imgUrl = "/images/item/" + imgName;
            //변경된 상품 이미지 정보를 세팅.
            //savedItemImg 엔티티는 현재 영속 상태이므로 itemImgRepository.save() 로직을 호출하지 않음.
            //데이터를 변경하는 것만으로 변경 감지 기능이 동작하여 트랜잭션이 끝날 때 update 쿼리가 실행됨.
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }

    }
}