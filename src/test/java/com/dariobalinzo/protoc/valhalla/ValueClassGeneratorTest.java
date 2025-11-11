package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueClassGeneratorTest {

    @Test
    void testGenerateValueClass() {
        DescriptorProto message = DescriptorProto.newBuilder()
                .setName("Person")
                .addField(field(1, "name", FieldDescriptorProto.Type.TYPE_STRING))
                .addField(field(2, "age", FieldDescriptorProto.Type.TYPE_INT32))
                .build();

        var valueClassGenerator = new ValueClassGenerator("com.example", message);
        String generatedCode = valueClassGenerator.generate();

        assertThat(generatedCode).contains("public value class Person");
        assertThat(generatedCode).contains("private final String name;");
        assertThat(generatedCode).contains("private final int age;");
        assertThat(generatedCode).contains("public Person(String name, int age)");
        assertThat(generatedCode).contains("public String getName()");
        assertThat(generatedCode).contains("public int getAge()");
        assertThat(generatedCode).contains("public Person withName(String value)");
        assertThat(generatedCode).contains("public Person withAge(int value)");
        assertThat(generatedCode).contains("public static Person getDefaultInstance()");
        assertThat(generatedCode).contains("public static PersonBuilder newBuilder()");
        assertThat(generatedCode).contains("public PersonBuilder toBuilder()");
        assertThat(generatedCode).contains("public static Person parseFrom(byte[] data)");
        assertThat(generatedCode).contains("public byte[] toByteArray()");
        assertThat(generatedCode).contains("public void writeTo(CodedOutputStream output)");
        assertThat(generatedCode).contains("public int getSerializedSize()");
    }

    private FieldDescriptorProto field(int number, String name, FieldDescriptorProto.Type type) {
        return FieldDescriptorProto.newBuilder()
                .setNumber(number)
                .setName(name)
                .setType(type)
                .build();
    }
}
