package com.hmall.item.domain.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "item", createIndex = true)
public class ItemDoc {

    @Id
    private Long id;

    /**
     * SKU名称 — ik分词
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String name;

    /**
     * 价格（分）
     */
    @Field(type = FieldType.Integer)
    private Integer price;

    /**
     * 商品图片
     */
    @Field(type = FieldType.Keyword, index = false)
    private String image;

    /**
     * 类目名称 — 关键字类型，用于过滤聚合
     */
    @Field(type = FieldType.Keyword)
    private String category;

    /**
     * 品牌名称 — 关键字类型
     */
    @Field(type = FieldType.Keyword)
    private String brand;

    /**
     * 规格
     */
    @Field(type = FieldType.Keyword, index = false)
    private String spec;

    /**
     * 销量 — 用于排序
     */
    @Field(type = FieldType.Integer)
    private Integer sold;

    /**
     * 评论数 — 用于排序
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 是否是推广广告
     */
    @Field(type = FieldType.Boolean)
    private Boolean isAD;

    /**
     * 商品状态 1-正常，2-下架，3-删除
     */
    @Field(type = FieldType.Integer)
    private Integer status;
}
