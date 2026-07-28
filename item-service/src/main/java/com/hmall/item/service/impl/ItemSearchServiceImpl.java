package com.hmall.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.doc.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemSearchServiceImpl implements ItemSearchService {

    private final ElasticsearchRestTemplate esTemplate;
    private final ItemMapper itemMapper;

    /**
     * 启动时自动初始化索引并全量同步 MySQL 数据到 ES
     */
    @PostConstruct
    public void init() {
        log.info("===== 开始初始化 ES 索引和数据 =====");
        initIndexAndData();
        log.info("===== ES 索引和数据初始化完成 =====");
    }

    @Override
    public void initIndexAndData() {
        // 1. 创建或更新索引
        createIndexIfNeeded();

        // 2. 全量加载 MySQL 商品数据
        List<Item> allItems = itemMapper.selectList(null);

        // 3. 写入 ES
        if (allItems != null && !allItems.isEmpty()) {
            saveOrUpdateItems(allItems);
            log.info("全量同步 {} 条商品数据到 ES", allItems.size());
        }
    }

    /**
     * 创建索引（带 ik 分词器设置）
     */
    private void createIndexIfNeeded() {
        IndexOperations indexOps = esTemplate.indexOps(ItemDoc.class);

        if (!indexOps.exists()) {
            // 创建索引
            indexOps.create();

            // 创建映射
            Document mapping = indexOps.createMapping(ItemDoc.class);
            indexOps.putMapping(mapping);

            log.info("ES 索引 'item' 创建完成");
        } else {
            log.info("ES 索引 'item' 已存在，跳过创建");
        }
    }

    @Override
    public void saveOrUpdateItem(Item item) {
        ItemDoc doc = convertToDoc(item);
        esTemplate.save(doc, IndexCoordinates.of("item"));
    }

    @Override
    public void saveOrUpdateItems(List<Item> items) {
        // 分批写入 ES，每批 500 条，避免 bulk 超限
        int batchSize = 500;
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<Item> batch = items.subList(i, end);
            List<ItemDoc> docs = batch.stream()
                    .map(this::convertToDoc)
                    .collect(Collectors.toList());
            esTemplate.save(docs, IndexCoordinates.of("item"));
            log.debug("ES 批量写入 {}~{} / {}", i, end, items.size());
        }
    }

    @Override
    public void deleteItemById(Long id) {
        esTemplate.delete(String.valueOf(id), IndexCoordinates.of("item"));
    }

    @Override
    public PageDTO<ItemDoc> search(ItemPageQuery query) {
        // 1. 构建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1 关键字匹配（name 字段 ik分词）
        if (StrUtil.isNotBlank(query.getKey())) {
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 1.2 分类过滤
        if (StrUtil.isNotBlank(query.getCategory())) {
            boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }

        // 1.3 品牌过滤
        if (StrUtil.isNotBlank(query.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }

        // 1.4 价格范围过滤
        if (query.getMinPrice() != null || query.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                    .gte(query.getMinPrice() != null ? query.getMinPrice() : 0)
                    .lte(query.getMaxPrice() != null ? query.getMaxPrice() : Integer.MAX_VALUE));
        }

        // 1.5 只查正常状态(status=1)的商品
        boolQuery.filter(QueryBuilders.termQuery("status", 1));

        // 2. 构建搜索查询
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(query.getPageNo() - 1, query.getPageSize()));

        // 3. 排序
        if (StrUtil.isNotBlank(query.getSortBy())) {
            SortOrder order = query.getIsAsc() != null && query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC;
            builder.withSorts(SortBuilders.fieldSort(query.getSortBy()).order(order));
        }

        NativeSearchQuery searchQuery = builder.build();

        // 4. 执行搜索
        SearchHits<ItemDoc> searchHits = esTemplate.search(searchQuery, ItemDoc.class);

        // 5. 封装结果
        long total = searchHits.getTotalHits();
        long pages = (total + query.getPageSize() - 1) / query.getPageSize();
        List<ItemDoc> list = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageDTO<>(total, pages, list);
    }

    /**
     * Item PO → ItemDoc 转换
     */
    private ItemDoc convertToDoc(Item item) {
        ItemDoc doc = new ItemDoc();
        BeanUtils.copyProperties(item, doc);
        return doc;
    }
}
