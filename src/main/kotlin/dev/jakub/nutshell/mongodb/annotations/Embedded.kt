package dev.jakub.nutshell.mongodb.annotations

/**
 * Marks a property as an embedded document.
 * The property will be stored as a nested object in MongoDB.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Embedded
