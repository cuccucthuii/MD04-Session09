package ra.inventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ra.inventoryservice.pubsub.PharmacyAlertPublisher;
import ra.inventoryservice.pubsub.PharmacyAlertSubscriber;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("ra.inventoryservice.entity")
                .build();

        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .enableSpringCacheNullValueSupport()
                .build();

        RedisCacheConfiguration medicinesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("medicines", medicinesCacheConfig)
                .build();
    }

    @Bean
    public PharmacyAlertPublisher pharmacyAlertPublisher(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            @Value("${inventory.redis.channel.pharmacy-alerts:pharmacy-alerts}") String channel) {
        return new PharmacyAlertPublisher(stringRedisTemplate, objectMapper, channel);
    }

    @Bean
    public PharmacyAlertSubscriber pharmacyAlertSubscriber(
            ObjectMapper objectMapper,
            @Value("${inventory.redis.channel.pharmacy-alerts:pharmacy-alerts}") String channel) {
        PharmacyAlertSubscriber subscriber = new PharmacyAlertSubscriber(objectMapper, channel);
        subscriber.logSubscription();
        return subscriber;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PharmacyAlertSubscriber pharmacyAlertSubscriber,
            @Value("${inventory.redis.channel.pharmacy-alerts:pharmacy-alerts}") String channel) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(pharmacyAlertSubscriber, new ChannelTopic(channel));
        return container;
    }
}
