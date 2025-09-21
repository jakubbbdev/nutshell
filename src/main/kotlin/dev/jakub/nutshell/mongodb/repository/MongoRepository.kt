package dev.jakub.nutshell.mongodb.repository

import dev.jakub.nutshell.mongodb.query.Query
import dev.jakub.nutshell.mongodb.query.Sort
import org.bson.Document
import org.bson.types.ObjectId

/**
 * Base repository interface providing common CRUD operations for MongoDB documents.
 * 
 * @param T The document type
 * @param ID The ID type (usually ObjectId or String)
 */
interface MongoRepository<T : Any, ID> {
    
    /**
     * Saves a document to the database.
     * If the document has an ID, it will be updated; otherwise, it will be inserted.
     */
    suspend fun save(entity: T): T
    
    /**
     * Saves multiple documents to the database.
     */
    suspend fun saveAll(entities: List<T>): List<T>
    
    /**
     * Finds a document by its ID.
     */
    suspend fun findById(id: ID): T?
    
    /**
     * Finds all documents.
     */
    suspend fun findAll(): List<T>
    
    /**
     * Finds documents matching the given query.
     */
    suspend fun find(query: Query): List<T>
    
    /**
     * Finds a single document matching the given query.
     */
    suspend fun findOne(query: Query): T?
    
    /**
     * Counts documents matching the given query.
     */
    suspend fun count(query: Query = Query.empty()): Long
    
    /**
     * Checks if a document with the given ID exists.
     */
    suspend fun existsById(id: ID): Boolean
    
    /**
     * Deletes a document by its ID.
     */
    suspend fun deleteById(id: ID): Boolean
    
    /**
     * Deletes a document.
     */
    suspend fun delete(entity: T): Boolean
    
    /**
     * Deletes all documents matching the given query.
     */
    suspend fun delete(query: Query): Long
    
    /**
     * Deletes all documents.
     */
    suspend fun deleteAll(): Long
    
    /**
     * Updates documents matching the given query.
     */
    suspend fun update(query: Query, update: Document): Long
    
    /**
     * Finds documents with pagination.
     */
    suspend fun findWithPagination(
        query: Query = Query.empty(),
        sort: Sort = Sort.empty(),
        page: Int = 0,
        size: Int = 20
    ): PaginatedResult<T>
}

/**
 * Result wrapper for paginated queries.
 */
data class PaginatedResult<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
