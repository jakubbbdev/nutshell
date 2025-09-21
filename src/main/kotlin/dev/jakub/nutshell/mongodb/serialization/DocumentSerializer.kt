package dev.jakub.nutshell.mongodb.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.jakub.nutshell.mongodb.annotations.*
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Serializes Kotlin objects to MongoDB BSON documents using annotations.
 */
class DocumentSerializer {
    
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
    
    /**
     * Serializes an object to a BSON document.
     */
    fun <T : Any> serialize(obj: T): Document {
        val document = Document()
        val clazz = obj::class
        
        clazz.memberProperties.forEach { property ->
            val fieldAnnotation = property.findAnnotation<Field>()
            val idAnnotation = property.findAnnotation<Id>()
            val embeddedAnnotation = property.findAnnotation<Embedded>()
            val referenceAnnotation = property.findAnnotation<Reference>()
            
            val fieldName = when {
                idAnnotation != null -> "_id"
                fieldAnnotation != null && fieldAnnotation.name.isNotEmpty() -> fieldAnnotation.name
                else -> property.name
            }
            
            val value = (property as kotlin.reflect.KProperty1<T, *>).get(obj)
            val serializedValue = when {
                idAnnotation != null -> serializeId(value)
                embeddedAnnotation != null -> serializeEmbedded(value)
                referenceAnnotation != null -> serializeReference(value, referenceAnnotation)
                else -> serializeValue(value)
            }
            
            if (serializedValue != null) {
                document[fieldName] = serializedValue
            }
        }
        
        return document
    }
    
    private fun serializeId(value: Any?): Any? {
        return when (value) {
            null -> ObjectId()
            is String -> if (ObjectId.isValid(value)) ObjectId(value) else value
            is ObjectId -> value
            else -> value
        }
    }
    
    private fun serializeEmbedded(value: Any?): Any? {
        return when (value) {
            null -> null
            is Collection<*> -> value.mapNotNull { serializeEmbedded(it) }
            is Map<*, *> -> value.mapValues { serializeEmbedded(it.value) }
            else -> if (isSimpleType(value::class)) serializeValue(value) else serialize(value)
        }
    }
    
    private fun serializeReference(value: Any?, annotation: Reference): Any? {
        return when (value) {
            null -> null
            is Collection<*> -> value.mapNotNull { serializeReference(it, annotation) }
            else -> {
                val id = getIdFromObject(value)
                id ?: serialize(value)
            }
        }
    }
    
    private fun getIdFromObject(obj: Any?): Any? {
        if (obj == null) return null
        
        val clazz = obj::class
        val idProperty = clazz.memberProperties.find { it.findAnnotation<Id>() != null }
        return idProperty?.let { (it as kotlin.reflect.KProperty1<Any, *>).get(obj) }
    }
    
    private fun serializeValue(value: Any?): Any? {
        return when (value) {
            null -> null
            is String -> value
            is Number -> value
            is Boolean -> value
            is Date -> value
            is LocalDateTime -> Date.from(value.toInstant(ZoneOffset.UTC))
            is ObjectId -> value
            is Collection<*> -> value.map { serializeValue(it) }
            is Map<*, *> -> value.mapValues { serializeValue(it.value) }
            is Enum<*> -> value.name
            else -> if (isSimpleType(value::class)) value else serialize(value)
        }
    }
    
    private fun isSimpleType(clazz: KClass<*>): Boolean {
        return when (clazz) {
            String::class,
            Number::class,
            Boolean::class,
            Date::class,
            LocalDateTime::class,
            ObjectId::class,
            UUID::class -> true
            else -> clazz.java.isPrimitive || clazz.java.isEnum
        }
    }
}
