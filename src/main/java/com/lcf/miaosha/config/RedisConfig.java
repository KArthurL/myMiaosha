package com.lcf.miaosha.config;

import com.google.common.base.Charsets;

import com.google.common.hash.Funnel;

import com.google.common.hash.PrimitiveSink;
import com.lcf.miaosha.util.BloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {
    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate<Object,Object> template=new RedisTemplate<>();
        FastJsonRedisSerializer fastJsonRedisSerializer=new FastJsonRedisSerializer(Object.class);

        // value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(fastJsonRedisSerializer);
        template.setHashValueSerializer(fastJsonRedisSerializer);
        // key的序列化采用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setConnectionFactory(redisConnectionFactory);


        return template;

    }

    @Bean
    public BloomFilter<Integer> initBloomFilterHelper() {

        return new BloomFilter<Integer>(new Funnel<Integer>(){
            @Override
            public void funnel(Integer integer, PrimitiveSink primitiveSink) {
                primitiveSink.putInt(integer);
            }
        },1000000,0.01);
    }

}
