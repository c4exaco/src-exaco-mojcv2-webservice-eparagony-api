package pl.exaco.receiptApi.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
open class CacheConfig {

    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()

        val cardsCache = CaffeineCache(
            "cardsCache", Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .recordStats()
                .build(),
            false
        )

        val detailsCache = CaffeineCache(
            "detailsCache", Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(5000)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .recordStats()
                .build(),
            false
        )

        cacheManager.setCaches(
            mutableListOf<Cache>(
                cardsCache,
                detailsCache
            )
        )
        return cacheManager
    }

}

