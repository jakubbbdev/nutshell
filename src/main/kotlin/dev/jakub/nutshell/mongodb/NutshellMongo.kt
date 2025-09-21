package dev.jakub.nutshell.mongodb

import dev.jakub.nutshell.mongodb.config.MongoConfig
import dev.jakub.nutshell.mongodb.query.QueryBuilder
import dev.jakub.nutshell.mongodb.repository.MongoRepository
import org.bson.types.ObjectId
import kotlin.reflect.KClass

/**
 * Main entry point for the Nutshell MongoDB library.
 * Provides a simple and intuitive API for MongoDB operations.
 */
object NutshellMongo {
    
    private val queryBuilder = QueryBuilder()
    
    /**
     * Initializes the MongoDB connection with the given configuration.
     */
    fun initialize(config: MongoConfig = MongoConfig.local()): MongoManager {
        return MongoManager.initialize(config)
    }
    
    /**
     * Gets the current MongoManager instance.
     */
    fun manager(): MongoManager = MongoManager.getInstance()
    
    /**
     * Gets a repository for the given document class.
     */
    fun <T : Any> repository(documentClass: KClass<T>): MongoRepository<T, ObjectId> {
        return manager().getRepository(documentClass)
    }
    
    /**
     * Gets a repository for the given document class with a custom ID type.
     */
    fun <T : Any, ID : Any> repository(documentClass: KClass<T>, idClass: KClass<ID>): MongoRepository<T, ID> {
        return manager().getRepository(documentClass, idClass)
    }
    
    /**
     * Gets the query builder for creating complex queries.
     */
    fun query(): QueryBuilder = queryBuilder
    
    /**
     * Closes the MongoDB connection and cleans up resources.
     */
    fun close() {
        MongoManager.close()
    }
    
    /**
     * Checks if the MongoDB connection is healthy.
     */
    fun isHealthy(): Boolean = manager().isHealthy()
    
    /**
     * Gets information about the MongoDB server.
     */
    fun getServerInfo(): Map<String, Any> = manager().getServerInfo()
}
