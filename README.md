# Nutshell MongoDB

A powerful MongoDB library for Kotlin with annotations and repository pattern.

## Features

- 🚀 **Annotation-based mapping** - Use `@Document`, `@Field`, `@Id` annotations
- 🔄 **Repository pattern** - Clean CRUD operations with `MongoRepository`
- 🔍 **Query builder** - Fluent API for complex MongoDB queries
- 📄 **Pagination support** - Built-in pagination for large datasets
- 🔄 **Automatic serialization** - Kotlin objects to BSON and vice versa
- ⚡ **Async support** - Coroutine-based API
- 🏗️ **Type-safe** - Full Kotlin type safety

## Quick Start

### Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/jakubbbdev/nutshell")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("dev.jakub.nutshell:mongodb:1.0.0")
}
```

### Usage

```kotlin
import dev.jakub.nutshell.mongodb.NutshellMongo
import dev.jakub.nutshell.mongodb.annotations.*

@Document("users")
data class User(
    @Id var id: ObjectId? = null,
    @Field("username") val username: String,
    @Field("email") val email: String,
    @Field("age") val age: Int,
    @Field("isActive") val isActive: Boolean = true
)

// Initialize
val config = MongoConfig(
    connectionString = "mongodb://localhost:27017",
    databaseName = "myapp"
)
NutshellMongo.initialize(config)

// Use repository
val userRepository = NutshellMongo.repository(User::class)

// CRUD operations
val user = User(username = "john", email = "john@example.com", age = 30)
val savedUser = userRepository.save(user)

val foundUser = userRepository.findById(savedUser.id!!)
val allUsers = userRepository.findAll()

// Complex queries
val activeUsers = userRepository.find(
    Query.create().eq("isActive", true)
)

// Pagination
val page = userRepository.findWithPagination(
    query = Query.create().eq("isActive", true),
    sort = Sort.create().asc("username"),
    page = 0,
    size = 10
)
```

## Annotations

- `@Document(collection)` - Maps class to MongoDB collection
- `@Field(name, required, unique)` - Maps property to document field
- `@Id` - Marks primary key field
- `@Index(fields, unique, sparse)` - Creates single-field indexes
- `@CompoundIndex(def, unique, sparse)` - Creates compound indexes
- `@Embedded` - Embeds documents
- `@Reference(target, lazy)` - References other documents

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
