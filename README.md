# protoc-gen-valhalla

An experimental Protocol Buffer compiler plugin that generates [JEP 401 Value Classes](https://openjdk.org/jeps/401) for high-performance serialization.

> ⚠️ **Experimental Project**: Built for fun and experimentation with JEP 401 ahead of Java 26. Use with caution in production.

## Why This Exists

JEP 401 Value Classes will likely become a cornerstone for frameworks requiring high-performance, memory-efficient data structures. This project explores what Protocol Buffer code generation could look like with value classes.

The standard `protobuf-java` library generates classes extending `com.google.protobuf.GeneratedMessage`, which cannot be value classes. This plugin bypasses that limitation by generating standalone value classes with custom Protocol Buffer serialization.

## Performance Highlights

## 🧭 Summary of Results: Valhalla Protobuf vs Standard Java Protobuf (Average & Percentile Performance)
For benchmarks I used an **early access build of Java 26** with `--enable-preview` to run both the generated value class code and the standard protobuf code. 
The benchmarks focus on parsing, field access, sorting, and array allocation for messages with repeated fields.

The benchmarks focused on deserialization and on some array operations show a mix of performance improvements and regressions:
- **Field Access**: Slightly faster on large arrays due to reduced indirection and better cache locality.
- **Sorting**: Slower due to the overhead of copying value

| Operation | Small Arrays (≈10 K) | Large Arrays (≈100 K) | Latency Distribution |
|------------|----------------------|------------------------|----------------------|
| **Field Access** | ≈ same | ⚡ **faster (~15%)** | ✅ tighter tail |
| **Parsing** | ⚡ **slightly faster (~3–5%)** | ⚡ **faster (~6–8%)** | ✅ tighter tail |
| **Sorting** | 🚫 **slower (~2–3×)** | 🚫 **slower (~2×)** | ✅ fewer outliers |
| **Parsing + Sorting** | ≈ same | ⚡ **faster (~6%)** | ✅ tighter tail |
| **Array Allocation** | ≈ same | ⚡ **slightly faster (~2–4%)** | ✅ smoother |

## Requirements

- **Java 21+**: To compile the plugin
- **Java 26+ with `--enable-preview`**: To run generated code and benchmarks
- Protocol Buffers compiler (`protoc`)

## Quick Start

```bash
# Build the plugin
mvn clean package

# Generate value classes from your .proto file
protoc --plugin=protoc-gen-value=./target/protoc-gen-value \
       --value_out=./generated \
       your_message.proto
```

## Generated Code

Creates two files per message:

**1. Immutable Value Class** (identity-free, custom serialization)
```java
public value class Person {
    private final String name;
    private final int age;
    
    // Direct wire format parsing (no intermediate objects)
    public static Person parseFrom(byte[] data) { ... }
    
    // Direct wire format serialization
    public byte[] toByteArray() { ... }
    
    // Immutable updates
    public Person withName(String value) { ... }
}
```

**2. Mutable Builder**
```java
public class PersonBuilder {
    public PersonBuilder setName(String value) { ... }
    public Person build() { ... }
}
```

## Usage Example

```java
// Build
Person person = Person.newBuilder()
    .setName("Alice")
    .setAge(30)
    .build();

// Serialize
byte[] bytes = person.toByteArray();

// Deserialize (direct construction, no builder)
Person parsed = Person.parseFrom(bytes);

// Immutable update
Person updated = person.withAge(31);
```

## Supported Types

| Proto | Java |
|-------|------|
| int32, int64, uint32, uint64 | int, long |
| float, double | float, double |
| bool | boolean |
| string | String |
| bytes | ByteString |
| message | Generated value class |
| repeated | List\<T\> |

## Limitations

- **Not production-ready**: Missing features like maps, oneofs, and comprehensive protobuf options
- **No standard protobuf API compatibility**: Cannot be used as drop-in replacement
- **Sorting performance**: Value class copying makes sorting operations slower
- **JEP 401 is in preview**: Requires `--enable-preview` flag

## When to Use

✅ **Good for:**
- Parsing-heavy workloads (deserialization-focused services)
- Memory-constrained environments (flattened arrays, no object headers)
- Latency-sensitive systems (better tail latencies)
- Experimenting with JEP 401

❌ **Avoid for:**
- Frequent sorting or reordering operations
- Need for full protobuf API compatibility
- Production systems (for now)

## Running Benchmarks

```bash
# Requires Java 26+ with --enable-preview
cd benchmarks
mvn clean package
java --enable-preview -jar target/benchmarks.jar
```

## References

- [JEP 401: Value Classes and Objects](https://openjdk.org/jeps/401)
- [Protocol Buffers Plugin Guide](https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.compiler.plugin)

## License

MIT

---

**Note**: This is a research project exploring future Java capabilities. Feedback and experimentation welcome, but use caution before deploying to production systems.
