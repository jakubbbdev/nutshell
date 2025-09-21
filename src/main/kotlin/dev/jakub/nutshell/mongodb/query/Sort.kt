package dev.jakub.nutshell.mongodb.query

import org.bson.Document

/**
 * MongoDB sort builder for ordering query results.
 */
class Sort private constructor(
    private val sortFields: MutableMap<String, Int> = mutableMapOf()
) {
    
    companion object {
        fun create(): Sort = Sort()
        fun empty(): Sort = Sort()
    }
    
    /**
     * Adds ascending sort for a field.
     */
    fun asc(field: String): Sort {
        sortFields[field] = 1
        return this
    }
    
    /**
     * Adds descending sort for a field.
     */
    fun desc(field: String): Sort {
        sortFields[field] = -1
        return this
    }
    
    /**
     * Adds natural sort order.
     */
    fun natural(): Sort {
        sortFields["\$natural"] = 1
        return this
    }
    
    /**
     * Converts the sort to a BSON document.
     */
    fun toBsonDocument(): Document {
        return Document(sortFields)
    }
    
    /**
     * Creates a copy of this sort.
     */
    fun copy(): Sort {
        return Sort(sortFields.toMutableMap())
    }
}
