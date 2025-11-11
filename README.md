# protoc-gen-valhalla

A Protocol Buffer compiler plugin that generates JEP 401 Value Classes with custom serialization.

## Features

- **Value Classes**: Generates immutable, identity-free JEP 401 value classes
- **Custom Parsing**: Direct construction from wire format without intermediate builder objects
- **Custom Serialization**: Direct serialization to wire format for maximum performance
- **Separate Builders**: Mutable builder pattern for constructing value classes
- **Immutable Updates**: `with*` methods for creating modified copies

## Requirements

- Java 21+ to run this plugin and generate the code. Java 26+ with `--enable-preview` to run the generated code.
- Maven 3.8+
- Protocol Buffers compiler (protoc)

## Building

```bash
# Clone the repository
git clone https://github.com/yourusername/protoc-gen-valhalla.git
cd protoc-gen-valhalla

# Build the project
mvn clean package

# This creates target/protoc-gen-value.jar
```

## Installation

### Option 1: Using the JAR directly

```bash
# Create a wrapper script (protoc-gen-value.sh)
cat > protoc-gen-value.sh << 'EOF'
#!/bin/bash
java --enable-preview -jar /path/to/target/protoc-gen-value.jar
EOF

chmod +x protoc-gen-value.sh
```

### Option 2: Install to local bin

```bash
# Copy to local bin directory
mkdir -p ~/.local/bin
cp target/protoc-gen-value.jar ~/.local/bin/
cat > ~/.local/bin/protoc-gen-value << 'EOF'
#!/bin/bash
java --enable-preview -jar ~/.local/bin/protoc-gen-value.jar
EOF

chmod +x ~/.local/bin/protoc-gen-value
```

## Usage

### Basic Example

Create a proto file (`example.proto`):

```protobuf
syntax = "proto3";

package example;

option java_package = "com.example.proto";

message Person {
  string name = 1;
  int32 age = 2;
  repeated string emails = 3;
}
```

Generate value classes:

```bash
protoc --plugin=protoc-gen-value=./protoc-gen-value.sh \
       --value_out=./generated \
       example.proto
```

This generates:
- `com/example/proto/Person.java` - Immutable value class
- `com/example/proto/PersonBuilder.java` - Mutable builder

### Using Generated Classes

```java
// Using the builder
Person person = Person.newBuilder()
    .setName("Alice")
    .setAge(30)
    .addEmail("alice@example.com")
    .build();

// Serialization
byte[] bytes = person.toByteArray();

// Deserialization
Person parsed = Person.parseFrom(bytes);

// Immutable updates
Person updated = person
    .withName("Bob")
    .withAge(25);

// Convert to builder for complex updates
Person modified = person.toBuilder()
    .setAge(31)
    .addEmail("alice2@example.com")
    .build();
```

## Generated Code Structure

### Value Class

```java
public value class Person {
    private final String name;
    private final int age;
    private final List<String> emails;
    
    // Constructor
    public Person(String name, int age, List<String> emails) { ... }
    
    // Getters
    public String getName() { ... }
    public int getAge() { ... }
    public List<String> getEmails() { ... }
    
    // Immutable updates
    public Person withName(String value) { ... }
    public Person withAge(int value) { ... }
    
    // Factories
    public static Person getDefaultInstance() { ... }
    public static PersonBuilder newBuilder() { ... }
    public PersonBuilder toBuilder() { ... }
    
    // Custom parsing (no intermediate objects)
    public static Person parseFrom(byte[] data) { ... }
    
    // Custom serialization
    public byte[] toByteArray() { ... }
    public void writeTo(CodedOutputStream output) { ... }
    public int getSerializedSize() { ... }
}
```

### Builder Class

```java
public class PersonBuilder {
    private String name = "";
    private int age = 0;
    private List<String> emails = List.of();
    
    // Setters (fluent API)
    public PersonBuilder setName(String value) { ... }
    public PersonBuilder setAge(int value) { ... }
    public PersonBuilder setEmails(List<String> value) { ... }
    
    // Adders for repeated fields
    public PersonBuilder addEmail(String value) { ... }
    
    // Build immutable value class
    public Person build() { ... }
}
```

## Supported Types

| Proto Type | Java Type |
|------------|-----------|
| int32, sint32, sfixed32 | int |
| int64, sint64, sfixed64 | long |
| uint32, fixed32 | int |
| uint64, fixed64 | long |
| float | float |
| double | double |
| bool | boolean |
| string | String |
| bytes | ByteString |
| message | Generated value class |
| repeated | List<T> |

## Performance Benefits

The generated code avoids intermediate objects during parsing and serialization:

1. **Direct Construction**: Value classes are constructed directly from the wire format
2. **No Builder Allocation**: Parsing doesn't allocate temporary builder objects
3. **Optimized Serialization**: Direct write to output stream without intermediate buffers

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ProtoUtilsTest
```
## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Add your license here]

## References

- [JEP 401: Value Classes and Objects](https://openjdk.org/jeps/401)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)
- [Protoc Plugin Guide](https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.compiler.plugin)