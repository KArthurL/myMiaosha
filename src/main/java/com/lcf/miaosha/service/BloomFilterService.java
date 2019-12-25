package com.lcf.miaosha.service;

public interface BloomFilterService {



    void addByBloomFilter(Integer value);


    boolean includeByBloomFilter(Integer value);
}
