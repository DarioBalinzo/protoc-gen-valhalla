package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuilderGeneratorTest {

    @Test
    void testGenerateBuilder() {
        DescriptorProto message = DescriptorProto.newBuilder()
                .setName("Person")
                .addField(field(1, "name", FieldDescriptorProto.Type.TYPE_STRING))
                .addField(field(2, "age", FieldDescriptorProto.Type.TYPE_INT32))
                .build();

        String generatedCode = BuilderGenerator.generate(message, "com.example");

        assertThat(generatedCode).contains("public class PersonBuilder");
        assertThat(generatedCode).contains("private String name = \"\";");
        assertThat(generatedCode).contains("private int age = 0;");
        assertThat(generatedCode).contains("public PersonBuilder() {}");
        assertThat(generatedCode).contains("public PersonBuilder(Person original)");
        assertThat(generatedCode).contains("public PersonBuilder setName(String value)");
        assertThat(generatedCode).contains("public PersonBuilder setAge(int value)");
        assertThat(generatedCode).contains("public String getName()");
        assertThat(generatedCode).contains("public int getAge()");
        assertThat(generatedCode).contains("public Person build()");
    }

    private FieldDescriptorProto field(int number, String name, FieldDescriptorProto.Type type) {
        return FieldDescriptorProto.newBuilder()
                .setNumber(number)
                .setName(name)
                .setType(type)
                .build();
    }
}