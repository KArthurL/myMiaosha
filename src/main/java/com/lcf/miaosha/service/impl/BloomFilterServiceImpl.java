package com.lcf.miaosha.service.impl;

import com.lcf.miaosha.dao.ItemDOMapper;
import com.lcf.miaosha.service.BloomFilterService;
import com.lcf.miaosha.util.BloomFilter;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BloomFilterServiceImpl implements BloomFilterService {


    private final String  key="itemId";



    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BloomFilter<Integer> bloomFilter;


    public void init(){
        List<Integer> res=itemDOMapper.getIds();
        if(res!=null) {
            for (int i : res) {
                this.addByBloomFilter(i);
            }
        }
    }


    @Override
    public void addByBloomFilter(Integer value) {
        int[] offset=bloomFilter.murmurHashOffset(value);
        for(int i:offset){
            redisTemplate.opsForValue().setBit(key,i,true);
        }
    }

    @Override
    public boolean includeByBloomFilter(Integer value) {
        int[] offset = bloomFilter.murmurHashOffset(value);
        for (int i : offset) {
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }

        return true;

    }
}
