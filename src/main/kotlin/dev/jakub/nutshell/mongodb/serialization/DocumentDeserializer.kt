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
 * Deserializes MongoDB BSON documents to Kotlin objects using annotations.
 */
class DocumentDeserializer {
    
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
    
    /**
     * Deserializes a BSON document to an object of the specified type.
     */
    fun <T : Any> deserialize(document: Document, clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} must have a primary constructor")
        
        val parameters = mutableMapOf<String, Any?>()
        
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
            
            val value = document[fieldName]
            val deserializedValue = when {
                idAnnotation != null -> deserializeId(value, property.returnType.classifier as KClass<*>)
                embeddedAnnotation != null -> deserializeEmbedded(value, property.returnType.classifier as KClass<*>)
                referenceAnnotation != null -> deserializeReference(value, referenceAnnotation)
                else -> deserializeValue(value, property.returnType.classifier as KClass<*>)
            }
            
            parameters[property.name] = deserializedValue
        }
        
        val constructorParams = constructor.parameters.associateWith { param ->
            parameters[param.name] ?: param.type.classifier?.let { 
                if (it == kotlin.Boolean::class) false
                else if (it == kotlin.Int::class) 0
                else if (it == kotlin.Long::class) 0L
                else if (it == kotlin.Double::class) 0.0
                else if (it == kotlin.Float::class) 0.0f
                else if (it == kotlin.String::class) ""
                else null
            }
        }
        return constructor.callBy(constructorParams)
    }
    
    private fun deserializeId(value: Any?, targetType: KClass<*>): Any? {
        return when {
            value == null -> null
            targetType == String::class -> value.toString()
            targetType == ObjectId::class -> value
            else -> value
        }
    }
    
    private fun deserializeEmbedded(value: Any?, targetType: KClass<*>): Any? {
        return when {
            value == null -> null
            value is Document -> deserialize(value, targetType)
            value is List<*> -> {
                val elementType = getGenericType(targetType)
                value.mapNotNull { 
                    when (it) {
                        is Document -> deserializeEmbedded(it, elementType)
                        else -> it
                    }
                }
            }
            else -> deserializeValue(value, targetType)
        }
    }
    
    private fun deserializeReference(value: Any?, annotation: Reference): Any? {
        return when {
            value == null -> null
            value is Document -> deserialize(value, annotation.target)
            value is List<*> -> {
                value.mapNotNull { 
                    when (it) {
                        is Document -> deserializeReference(it, annotation)
                        else -> it
                    }
                }
            }
            else -> value
        }
    }
    
    private fun deserializeValue(value: Any?, targetType: KClass<*>): Any? {
        return when {
            value == null -> null
            targetType == String::class -> value.toString()
            targetType == Boolean::class -> value as? Boolean ?: value.toString().toBoolean()
            targetType == Int::class -> (value as? Number)?.toInt() ?: value.toString().toIntOrNull()
            targetType == Long::class -> (value as? Number)?.toLong() ?: value.toString().toLongOrNull()
            targetType == Double::class -> (value as? Number)?.toDouble() ?: value.toString().toDoubleOrNull()
            targetType == Float::class -> (value as? Number)?.toFloat() ?: value.toString().toFloatOrNull()
            targetType == Date::class -> value as? Date
            targetType == LocalDateTime::class -> {
                when (value) {
                    is Date -> LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC)
                    is String -> LocalDateTime.parse(value)
                    else -> null
                }
            }
            targetType == ObjectId::class -> value as? ObjectId
            targetType == UUID::class -> {
                when (value) {
                    is String -> UUID.fromString(value)
                    is UUID -> value
                    else -> null
                }
            }
            isSimpleType(targetType) -> value
            else -> {
                when (value) {
                    is Document -> deserialize(value, targetType)
                    is Map<*, *> -> {
                        val doc = Document()
                        (value as Map<String, Any?>).forEach { (k, v) -> doc[k] = v }
                        deserialize(doc, targetType)
                    }
                    else -> value
                }
            }
        }
    }
    
    private fun getGenericType(type: KClass<*>): KClass<*> {
        return Any::class
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
