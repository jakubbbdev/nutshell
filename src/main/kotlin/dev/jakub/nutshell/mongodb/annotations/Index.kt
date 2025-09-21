package dev.jakub.nutshell.mongodb.annotations

/**
 * Defines an index on a MongoDB collection.
 * 
 * @param fields The fields to include in the index
 * @param unique Whether the index should be unique
 * @param sparse Whether the index should be sparse (ignore documents without the indexed field)
 * @param background Whether the index should be created in the background
 * @param name The name of the index. If not specified, auto-generated.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Index(
    val fields: Array<String>,
    val unique: Boolean = false,
    val sparse: Boolean = false,
    val background: Boolean = true,
    val name: String = ""
)
