package com.hmall.item.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.doc.ItemDoc;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.item.service.ItemSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品搜索接口")
@RestController
@RequestMapping("/items/search")
@RequiredArgsConstructor
public class ItemSearchController {

    private final ItemSearchService searchService;

    @ApiOperation("从ES搜索商品（支持关键字、分类、品牌、价格过滤、排序）")
    @GetMapping
    public PageDTO<ItemDoc> search(ItemPageQuery query) {
        return searchService.search(query);
    }
}
