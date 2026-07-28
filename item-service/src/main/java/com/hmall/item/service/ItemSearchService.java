package com.hmall.item.service;

import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.doc.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.query.ItemPageQuery;

import java.util.List;

public interface ItemSearchService {

    /**
     * ES 商品搜索（分页 + 排序 + 过滤）
     */
    PageDTO<ItemDoc> search(ItemPageQuery query);

    /**
     * 初始化索引（创建索引和映射，全量导入数据）
     */
    void initIndexAndData();

    /**
     * 新增/更新单个商品到 ES
     */
    void saveOrUpdateItem(Item item);

    /**
     * 批量新增/更新商品到 ES
     */
    void saveOrUpdateItems(List<Item> items);

    /**
     * 根据 id 从 ES 删除商品
     */
    void deleteItemById(Long id);
}
