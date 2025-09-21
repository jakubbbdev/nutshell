package dev.jakub.nutshell.mongodb.annotations

import kotlin.reflect.KClass

/**
 * Marks a property as a reference to another document.
 * 
 * @param target The target document class
 * @param lazy Whether to use lazy loading for the reference
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reference(
    val target: KClass<*>,
    val lazy: Boolean = false
)
