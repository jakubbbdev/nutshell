package dev.jakub.nutshell.mongodb.query

import org.bson.Document
import org.bson.conversions.Bson

/**
 * MongoDB query builder providing a fluent API for building complex queries.
 */
class Query private constructor(
    private val conditions: MutableList<Bson> = mutableListOf()
) {
    
    companion object {
        fun create(): Query = Query()
        fun empty(): Query = Query()
    }
    
    /**
     * Adds an equality condition.
     */
    fun eq(field: String, value: Any?): Query {
        conditions.add(Document(field, value))
        return this
    }
    
    /**
     * Adds a not equal condition.
     */
    fun ne(field: String, value: Any?): Query {
        conditions.add(Document(field, Document("\$ne", value)))
        return this
    }
    
    /**
     * Adds a greater than condition.
     */
    fun gt(field: String, value: Any): Query {
        conditions.add(Document(field, Document("\$gt", value)))
        return this
    }
    
    /**
     * Adds a greater than or equal condition.
     */
    fun gte(field: String, value: Any): Query {
        conditions.add(Document(field, Document("\$gte", value)))
        return this
    }
    
    /**
     * Adds a less than condition.
     */
    fun lt(field: String, value: Any): Query {
        conditions.add(Document(field, Document("\$lt", value)))
        return this
    }
    
    /**
     * Adds a less than or equal condition.
     */
    fun lte(field: String, value: Any): Query {
        conditions.add(Document(field, Document("\$lte", value)))
        return this
    }
    
    /**
     * Adds an "in" condition.
     */
    fun `in`(field: String, values: List<Any>): Query {
        conditions.add(Document(field, Document("\$in", values)))
        return this
    }
    
    /**
     * Adds a "not in" condition.
     */
    fun nin(field: String, values: List<Any>): Query {
        conditions.add(Document(field, Document("\$nin", values)))
        return this
    }
    
    /**
     * Adds a "exists" condition.
     */
    fun exists(field: String, exists: Boolean = true): Query {
        conditions.add(Document(field, Document("\$exists", exists)))
        return this
    }
    
    /**
     * Adds a regex condition.
     */
    fun regex(field: String, pattern: String, options: String = ""): Query {
        val regexDoc = Document("\$regex", pattern)
        if (options.isNotEmpty()) {
            regexDoc.append("\$options", options)
        }
        conditions.add(Document(field, regexDoc))
        return this
    }
    
    /**
     * Adds a text search condition.
     */
    fun text(search: String): Query {
        conditions.add(Document("\$text", Document("\$search", search)))
        return this
    }
    
    /**
     * Adds a "where" condition using JavaScript.
     */
    fun where(expression: String): Query {
        conditions.add(Document("\$where", expression))
        return this
    }
    
    /**
     * Adds a size condition for arrays.
     */
    fun size(field: String, size: Int): Query {
        conditions.add(Document(field, Document("\$size", size)))
        return this
    }
    
    /**
     * Adds an "all" condition for arrays.
     */
    fun all(field: String, values: List<Any>): Query {
        conditions.add(Document(field, Document("\$all", values)))
        return this
    }
    
    /**
     * Adds an "elemMatch" condition for array elements.
     */
    fun elemMatch(field: String, query: Query): Query {
        conditions.add(Document(field, Document("\$elemMatch", query.toBsonDocument())))
        return this
    }
    
    /**
     * Adds a "near" condition for geospatial queries.
     */
    fun near(field: String, longitude: Double, latitude: Double, maxDistance: Double? = null): Query {
        val nearDoc = Document("\$near", Document("\$geometry", Document("type", "Point").append("coordinates", listOf(longitude, latitude))))
        maxDistance?.let { nearDoc.append("\$maxDistance", it) }
        conditions.add(Document(field, nearDoc))
        return this
    }
    
    /**
     * Adds a "within" condition for geospatial queries.
     */
    fun within(field: String, coordinates: List<List<Double>>): Query {
        conditions.add(Document(field, Document("\$geoWithin", Document("\$geometry", Document("type", "Polygon").append("coordinates", listOf(coordinates))))))
        return this
    }
    
    /**
     * Combines conditions with AND logic.
     */
    fun and(vararg queries: Query): Query {
        val andConditions = mutableListOf<Bson>()
        andConditions.addAll(this.conditions)
        queries.forEach { andConditions.addAll(it.conditions) }
        conditions.clear()
        conditions.add(Document("\$and", andConditions))
        return this
    }
    
    /**
     * Combines conditions with OR logic.
     */
    fun or(vararg queries: Query): Query {
        val orConditions = mutableListOf<Bson>()
        orConditions.addAll(this.conditions)
        queries.forEach { orConditions.addAll(it.conditions) }
        conditions.clear()
        conditions.add(Document("\$or", orConditions))
        return this
    }
    
    /**
     * Negates the query.
     */
    fun not(): Query {
        conditions.add(Document("\$not", toBsonDocument()))
        return this
    }
    
    /**
     * Converts the query to a BSON document.
     */
    fun toBsonDocument(): Document {
        return if (conditions.size == 1) {
            conditions.first() as Document
        } else {
            Document().apply {
                conditions.forEach { condition ->
                    if (condition is Document) {
                        putAll(condition)
                    }
                }
            }
        }
    }
    
    /**
     * Creates a copy of this query.
     */
    fun copy(): Query {
        return Query(conditions.toMutableList())
    }
}
