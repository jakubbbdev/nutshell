package dev.jakub.nutshell.mongodb.query

/**
 * Fluent query builder providing a more intuitive API for building MongoDB queries.
 */
class QueryBuilder {
    
    /**
     * Creates a new query.
     */
    fun query(): Query = Query.create()
    
    /**
     * Creates a query with equality condition.
     */
    fun where(field: String, value: Any?): Query = Query.create().eq(field, value)
    
    /**
     * Creates a query with multiple equality conditions.
     */
    fun where(vararg conditions: Pair<String, Any?>): Query {
        val query = Query.create()
        conditions.forEach { (field, value) ->
            query.eq(field, value)
        }
        return query
    }
    
    /**
     * Creates a query for text search.
     */
    fun search(text: String): Query = Query.create().text(text)
    
    /**
     * Creates a query for geospatial near search.
     */
    fun near(field: String, longitude: Double, latitude: Double, maxDistance: Double? = null): Query {
        return Query.create().near(field, longitude, latitude, maxDistance)
    }
    
    /**
     * Creates a query for array contains all elements.
     */
    fun containsAll(field: String, values: List<Any>): Query = Query.create().all(field, values)
    
    /**
     * Creates a query for array size.
     */
    fun arraySize(field: String, size: Int): Query = Query.create().size(field, size)
    
    /**
     * Creates a query for date range.
     */
    fun dateRange(field: String, from: java.time.LocalDateTime, to: java.time.LocalDateTime): Query {
        return Query.create()
            .gte(field, from)
            .lt(field, to)
    }
    
    /**
     * Creates a query for numeric range.
     */
    fun range(field: String, min: Number, max: Number): Query {
        return Query.create()
            .gte(field, min)
            .lte(field, max)
    }
    
    /**
     * Creates a query for existence check.
     */
    fun exists(field: String, exists: Boolean = true): Query = Query.create().exists(field, exists)
    
    /**
     * Creates a query for regex pattern matching.
     */
    fun matches(field: String, pattern: String, caseInsensitive: Boolean = false): Query {
        val options = if (caseInsensitive) "i" else ""
        return Query.create().regex(field, pattern, options)
    }
}
