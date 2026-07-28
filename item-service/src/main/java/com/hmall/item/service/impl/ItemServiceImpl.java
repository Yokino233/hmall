package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import com.hmall.item.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    private final ItemSearchService itemSearchService;

    @Override
    public boolean save(Item entity) {
        boolean result = super.save(entity);
        if (result) {
            itemSearchService.saveOrUpdateItem(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(Item entity) {
        boolean result = super.updateById(entity);
        if (result) {
            // 查询完整数据（避免 status 等字段被前端限制为 null）
            Item fullItem = getById(entity.getId());
            if (fullItem != null) {
                itemSearchService.saveOrUpdateItem(fullItem);
            }
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            itemSearchService.deleteItemById((Long) id);
        }
        return result;
    }

    @Override
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.hmall.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
        // 同步库存到 ES
        items.forEach(dto -> {
            Item item = getById(dto.getItemId());
            if (item != null) {
                itemSearchService.saveOrUpdateItem(item);
            }
        });
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
}
