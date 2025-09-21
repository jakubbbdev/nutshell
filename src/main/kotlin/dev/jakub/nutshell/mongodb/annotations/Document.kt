package dev.jakub.nutshell.mongodb.annotations

/**
 * Marks a class as a MongoDB document.
 * 
 * @param collection The name of the MongoDB collection. If not specified, uses the class name in lowercase.
 * @param database The name of the database. If not specified, uses the default database.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Document(
    val collection: String = "",
    val database: String = ""
)
