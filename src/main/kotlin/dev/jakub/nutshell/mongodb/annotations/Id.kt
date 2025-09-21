package dev.jakub.nutshell.mongodb.annotations

/**
 * Marks a property as the MongoDB document ID.
 * The property will be automatically mapped to the "_id" field.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id
