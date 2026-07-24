package com.hmall.cart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.cart.service.ICartService;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {
    private final ItemClient itemClient;


    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        Long userId = UserContext.getUser();
        if (checkItemExists(cartFormDTO.getItemId(), userId)) {
            baseMapper.updateNum(cartFormDTO.getItemId(), userId);
            return;
        }
        checkCartsFull(userId);
        Cart cart = BeanUtils.copyBean(cartFormDTO, Cart.class);
        cart.setUserId(userId);
        save(cart);
    }

    @Override
    public List<CartVO> queryMyCarts() {
        List<Cart> carts = lambdaQuery().eq(Cart::getUserId, UserContext.getUser()).list();
        if (CollUtils.isEmpty(carts)) {
            return CollUtils.emptyList();
        }
        List<CartVO> vos = BeanUtils.copyList(carts, CartVO.class);
        handleCartItems(vos);
        return vos;
    }

    private void handleCartItems(List<CartVO> vos) {
        Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 远程调用 item-service 查询商品信息
//        List<ItemDTO> items = queryItemsByIds(itemIds);
//        if (CollUtils.isEmpty(items)) {
//            return;
//        }
//        Map<Long, ItemDTO> itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
//        for (CartVO v : vos) {
//            ItemDTO item = itemMap.get(v.getItemId());
//            if (item == null) continue;
//            v.setNewPrice(item.getPrice());
//            v.setStatus(item.getStatus());
//            v.setStock(item.getStock());
//        }
        List<ItemDTO> itemDTOS = itemClient.queryItemsByIds(itemIds);

    }

    private List<ItemDTO> queryItemsByIds(Set<Long> ids) {
        String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        String url = "http://localhost:8081/items?ids=" + idsStr;
        RestTemplate rt = new RestTemplate();
        try {
            return Arrays.asList(rt.getForObject(url, ItemDTO[].class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void removeByItemIds(Collection<Long> itemIds) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, UserContext.getUser())
                .in(Cart::getItemId, itemIds);
        remove(queryWrapper);
    }

    private void checkCartsFull(Long userId) {
        int count = lambdaQuery().eq(Cart::getUserId, userId).count();
        if (count >= 10) {
            throw new BizIllegalException(StrUtil.format("用户购物车课程不能超过{}", 10));
        }
    }

    private boolean checkItemExists(Long itemId, Long userId) {
        int count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId)
                .count();
        return count > 0;
    }
}
