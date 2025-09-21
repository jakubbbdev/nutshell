package dev.jakub.nutshell.mongodb.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.slf4j.LoggerFactory

/**
 * Configuration class for MongoDB connection settings.
 */
data class MongoConfig(
    val connectionString: String = "mongodb://localhost:27017",
    val databaseName: String = "nutshell",
    val maxPoolSize: Int = 100,
    val minPoolSize: Int = 0,
    val connectTimeout: Int = 10000,
    val socketTimeout: Int = 0,
    val serverSelectionTimeout: Int = 30000,
    val retryWrites: Boolean = true,
    val retryReads: Boolean = true
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(MongoConfig::class.java)

        fun local(): MongoConfig = MongoConfig()
        

        fun fromEnvironment(): MongoConfig {
            val connectionString = System.getenv("MONGODB_URI") ?: "mongodb://localhost:27017"
            val databaseName = System.getenv("MONGODB_DATABASE") ?: "nutshell"
            
            return MongoConfig(
                connectionString = connectionString,
                databaseName = databaseName,
                maxPoolSize = System.getenv("MONGODB_MAX_POOL_SIZE")?.toIntOrNull() ?: 100,
                minPoolSize = System.getenv("MONGODB_MIN_POOL_SIZE")?.toIntOrNull() ?: 0,
                connectTimeout = System.getenv("MONGODB_CONNECT_TIMEOUT")?.toIntOrNull() ?: 10000,
                socketTimeout = System.getenv("MONGODB_SOCKET_TIMEOUT")?.toIntOrNull() ?: 0
            )
        }
    }
    
    /**
     * Creates a MongoClient with the current configuration.
     */
    fun createMongoClient(): MongoClient {
        logger.info("Creating MongoDB client with connection string: $connectionString")
        
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .applyToConnectionPoolSettings { builder ->
                builder
                    .maxSize(maxPoolSize)
                    .minSize(minPoolSize)
            }
            .retryWrites(retryWrites)
            .retryReads(retryReads)
            .build()
        
        return MongoClients.create(settings)
    }
    
    /**
     * Gets the database with the configured name.
     */
    fun getDatabase(client: MongoClient): MongoDatabase {
        return client.getDatabase(databaseName)
    }
}