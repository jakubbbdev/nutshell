package dev.jakub.nutshell.mongodb.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import dev.jakub.nutshell.mongodb.annotations.Document
import dev.jakub.nutshell.mongodb.annotations.Id
import dev.jakub.nutshell.mongodb.query.Query
import dev.jakub.nutshell.mongodb.query.Sort
import dev.jakub.nutshell.mongodb.serialization.DocumentSerializer
import dev.jakub.nutshell.mongodb.serialization.DocumentDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document as BsonDocument
import org.bson.types.ObjectId
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.math.ceil

/**
 * Base implementation of MongoRepository providing common CRUD operations.
 */
class BaseMongoRepository<T : Any, ID>(
    protected val database: MongoDatabase,
    protected val documentClass: KClass<T>
) : MongoRepository<T, ID> {
    
    protected val collection: MongoCollection<BsonDocument> by lazy {
        val documentAnnotation = documentClass.findAnnotation<Document>()
        val collectionName = documentAnnotation?.collection?.takeIf { it.isNotEmpty() }
            ?: documentClass.simpleName?.lowercase() ?: "documents"
        database.getCollection(collectionName)
    }
    
    protected val serializer = DocumentSerializer()
    protected val deserializer = DocumentDeserializer()
    
    override suspend fun save(entity: T): T = withContext(Dispatchers.IO) {
        val document = serializer.serialize(entity)
        val idField = getIdField(entity)
        
        if (idField != null && idField != ObjectId()) {

            collection.replaceOne(
                BsonDocument("_id", idField),
                document,
                com.mongodb.client.model.ReplaceOptions().upsert(true)
            )
        } else {
            collection.insertOne(document)
            val newId = document.getObjectId("_id")
            if (newId != null) {
                return@withContext createEntityWithId(entity, newId)
            }
        }
        
        entity
    }
    
    override suspend fun saveAll(entities: List<T>): List<T> = withContext(Dispatchers.IO) {
        if (entities.isEmpty()) return@withContext emptyList()
        
        val documents = entities.map { entity ->
            val doc = serializer.serialize(entity)
            val idField = getIdField(entity)
            if (idField == null || idField == ObjectId()) {
                doc.append("_id", ObjectId())
            }
            doc
        }
        
        collection.insertMany(documents)

        entities.forEachIndexed { index, entity ->
            val doc = documents[index]
            val newId = doc.getObjectId("_id")
            setIdField(entity, newId)
        }
        
        entities
    }
    
    override suspend fun findById(id: ID): T? = withContext(Dispatchers.IO) {
        val query = BsonDocument("_id", id as Any)
        val document = collection.find(query).first()
        document?.let { deserializer.deserialize(it, documentClass) }
    }
    
    override suspend fun findAll(): List<T> = withContext(Dispatchers.IO) {
        collection.find().toList().map { deserializer.deserialize(it, documentClass) }
    }
    
    override suspend fun find(query: Query): List<T> = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        collection.find(mongoQuery).toList().map { deserializer.deserialize(it, documentClass) }
    }
    
    override suspend fun findOne(query: Query): T? = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        val document = collection.find(mongoQuery).first()
        document?.let { deserializer.deserialize(it, documentClass) }
    }
    
    override suspend fun count(query: Query): Long = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        collection.countDocuments(mongoQuery)
    }
    
    override suspend fun existsById(id: ID): Boolean = withContext(Dispatchers.IO) {
        val query = BsonDocument("_id", id as Any)
        collection.countDocuments(query) > 0
    }
    
    override suspend fun deleteById(id: ID): Boolean = withContext(Dispatchers.IO) {
        val query = BsonDocument("_id", id as Any)
        val result = collection.deleteOne(query)
        result.deletedCount > 0
    }
    
    override suspend fun delete(entity: T): Boolean = withContext(Dispatchers.IO) {
        val id = getIdField(entity) ?: return@withContext false
        deleteById(id as ID)
    }
    
    override suspend fun delete(query: Query): Long = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        val result = collection.deleteMany(mongoQuery)
        result.deletedCount
    }
    
    override suspend fun deleteAll(): Long = withContext(Dispatchers.IO) {
        val result = collection.deleteMany(BsonDocument())
        result.deletedCount
    }
    
    override suspend fun update(query: Query, update: BsonDocument): Long = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        val result = collection.updateMany(mongoQuery, BsonDocument("\$set", update))
        result.modifiedCount
    }
    
    override suspend fun findWithPagination(
        query: Query,
        sort: Sort,
        page: Int,
        size: Int
    ): PaginatedResult<T> = withContext(Dispatchers.IO) {
        val mongoQuery = query.toBsonDocument()
        val totalElements = collection.countDocuments(mongoQuery)
        val totalPages = ceil(totalElements.toDouble() / size).toInt()
        
        val skip = page * size
        val documents = collection.find(mongoQuery)
            .sort(sort.toBsonDocument())
            .skip(skip)
            .limit(size)
            .toList()
        
        val content = documents.map { deserializer.deserialize(it, documentClass) }
        
        PaginatedResult(
            content = content,
            totalElements = totalElements,
            totalPages = totalPages,
            currentPage = page,
            size = size,
            hasNext = page < totalPages - 1,
            hasPrevious = page > 0
        )
    }
    
    protected fun getIdField(entity: T): Any? {
        val idProperty = documentClass.memberProperties.find { 
            it.findAnnotation<Id>() != null 
        }
        return idProperty?.get(entity)
    }
    
    protected fun createEntityWithId(entity: T, id: Any): T {
        try {
            val constructor = documentClass.primaryConstructor
            if (constructor != null) {
                val parameters = mutableMapOf<kotlin.reflect.KParameter, Any?>()

                constructor.parameters.forEach { param ->
                    val property = documentClass.memberProperties.find { it.name == param.name }
                    val value = if (property?.findAnnotation<Id>() != null) {
                        id
                    } else if (property != null) {
                        (property as kotlin.reflect.KProperty1<T, *>).get(entity)
                    } else {
                        null
                    }
                    parameters[param] = value
                }
                
                return constructor.callBy(parameters)
            }
        } catch (e: Exception) {
            println("Warning: Could not create entity with ID: ${e.message}")
        }
        
        return entity
    }
    
    protected fun setIdField(entity: T, id: Any) {
        try {
            val idProperty = documentClass.memberProperties.find { 
                it.findAnnotation<Id>() != null 
            }
            if (idProperty != null && idProperty is kotlin.reflect.KMutableProperty1<*, *>) {
                (idProperty as kotlin.reflect.KMutableProperty1<T, Any?>).set(entity, id)
            }
        } catch (e: Exception) {
            println("Warning: Could not set ID on entity: ${e.message}")
        }
    }
}
