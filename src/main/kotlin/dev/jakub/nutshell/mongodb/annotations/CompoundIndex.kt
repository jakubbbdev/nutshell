package dev.jakub.nutshell.mongodb.annotations

/**
 * Defines a compound index on multiple fields.
 * 
 * @param def The index definition as a string (e.g., "name:1, age:-1")
 * @param unique Whether the compound index should be unique
 * @param sparse Whether the index should be sparse
 * @param background Whether the index should be created in the background
 * @param name The name of the index
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CompoundIndex(
    val def: String,
    val unique: Boolean = false,
    val sparse: Boolean = false,
    val background: Boolean = true,
    val name: String = ""
)
