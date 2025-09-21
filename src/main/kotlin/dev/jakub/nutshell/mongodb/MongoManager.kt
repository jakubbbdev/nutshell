package dev.jakub.nutshell.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import dev.jakub.nutshell.mongodb.config.MongoConfig
import dev.jakub.nutshell.mongodb.repository.BaseMongoRepository
import dev.jakub.nutshell.mongodb.repository.MongoRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Main manager class for MongoDB operations.
 * Provides a centralized way to manage connections and repositories.
 */
class MongoManager private constructor(
    private val config: MongoConfig,
    private val client: MongoClient,
    private val database: MongoDatabase
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(MongoManager::class.java)
        private var instance: MongoManager? = null
        
        /**
         * Initializes the MongoManager with the given configuration.
         */
        fun initialize(config: MongoConfig = MongoConfig.local()): MongoManager {
            if (instance != null) {
                logger.warn("MongoManager is already initialized. Returning existing instance.")
                return instance!!
            }
            
            val client = config.createMongoClient()
            val database = config.getDatabase(client)
            instance = MongoManager(config, client, database)
            
            logger.info("MongoManager initialized successfully")
            return instance!!
        }
        
        /**
         * Gets the current instance of MongoManager.
         * Throws an exception if not initialized.
         */
        fun getInstance(): MongoManager {
            return instance ?: throw IllegalStateException("MongoManager not initialized. Call initialize() first.")
        }
        
        /**
         * Closes the current instance and cleans up resources.
         */
        fun close() {
            instance?.let { manager ->
                manager.client.close()
                instance = null
                logger.info("MongoManager closed")
            }
        }
    }
    
    /**
     * Gets the MongoDB database instance.
     */
    fun getDatabase(): MongoDatabase = database
    
    /**
     * Gets the MongoDB client instance.
     */
    fun getClient(): MongoClient = client
    
    /**
     * Gets the configuration used by this manager.
     */
    fun getConfig(): MongoConfig = config
    
    /**
     * Creates a repository for the given document class.
     */
    fun <T : Any> getRepository(documentClass: KClass<T>): MongoRepository<T, ObjectId> {
        return BaseMongoRepository(database, documentClass)
    }
    
    /**
     * Creates a repository for the given document class with a custom ID type.
     */
    fun <T : Any, ID : Any> getRepository(documentClass: KClass<T>, idClass: KClass<ID>): MongoRepository<T, ID> {
        return BaseMongoRepository<T, ID>(database, documentClass) as MongoRepository<T, ID>
    }
    
    /**
     * Checks if the MongoDB connection is healthy.
     */
    fun isHealthy(): Boolean {
        return try {
            database.runCommand(org.bson.Document("ping", 1))
            true
        } catch (e: Exception) {
            logger.error("Health check failed", e)
            false
        }
    }
    
    /**
     * Gets information about the MongoDB server.
     */
    fun getServerInfo(): Map<String, Any> {
        return try {
            val result = database.runCommand(org.bson.Document("buildInfo", 1))
            result.toMap()
        } catch (e: Exception) {
            logger.error("Failed to get server info", e)
            emptyMap()
        }
    }
}
