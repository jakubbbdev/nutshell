package dev.jakub.nutshell.mongodb.annotations

/**
 * Maps a property to a MongoDB field.
 * 
 * @param name The name of the field in MongoDB. If not specified, uses the property name.
 * @param required Whether this field is required (not null).
 * @param unique Whether this field should have a unique index.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(
    val name: String = "",
    val required: Boolean = false,
    val unique: Boolean = false
)
