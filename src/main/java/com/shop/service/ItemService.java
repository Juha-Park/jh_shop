package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.entyty.Item;
import com.shop.entyty.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.shop.dto.ItemImgDto;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;

import com.shop.dto.ItemSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shop.dto.MainItemDto;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        //상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            //첫 번째 이미지일 경우 대표 상품 이미지 여부 값을 "Y"로 세팅. 나머지 상품 이미지는 "N"로 설정.
            if(i == 0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }

    /* 데이터의 수정이 일어나지 않으므로 상품 데이터를 읽어오는 트랜잭션을 읽기 전용으로 설정.
       이럴 경우 JPA가 더티 체킹(변경 감지)를 수행하지 않아서 성능을 향상시킬 수 있음.*/
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){

        //해당 상품의 이미지를 조회. 등록순으로 가져오기 위해서 상품 이미지 아이디 오름차순으로 검색.
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();

        //조회한 ItemImg 엔티티를 ItemImgDto 객체로 만들어서 리스트에 추가.
        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        //상품의 아이디를 통해 상품 엔티티를 조회. 존재하지 않을 경우 EntityNodFoundException을 발생시킴.
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        //상품 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);

        //상품 이미지 아이디 리스트를 조회.
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),
                    itemImgFileList.get(i));
        }

        return item.getId();
    }

    //상품 조회 조건과 페이지 정보를 파라미터로 받아 상품 데이터를 조회.
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    //메인 페이지에 보여줄 상품 데이터를 조회.
    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

}