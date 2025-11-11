package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ValueClassPluginTest {

    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    @Test
    void testMain() throws IOException {
        DescriptorProtos.FileDescriptorProto file = DescriptorProtos.FileDescriptorProto.newBuilder()
                .setName("person.proto")
                .setPackage("com.example")
                .addMessageType(
                        DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("Person")
                                .addField(
                                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                                .setName("name")
                                                .setNumber(1)
                                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                                .build()
                                )
                                .addField(
                                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                                .setName("age")
                                                .setNumber(2)
                                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32)
                                                .build()
                                )
                                .build()
                )
                .build();

        PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.newBuilder()
                .addFileToGenerate("person.proto")
                .addProtoFile(file)
                .build();

        testIn = new ByteArrayInputStream(request.toByteArray());
        System.setIn(testIn);

        ValueClassPlugin.main(new String[]{});

        PluginProtos.CodeGeneratorResponse response = PluginProtos.CodeGeneratorResponse.parseFrom(testOut.toByteArray());

        assertThat(response.getFileCount()).isEqualTo(2);

        PluginProtos.CodeGeneratorResponse.File personClass = response.getFile(0);
        assertThat(personClass.getName()).isEqualTo("com/example/Person.java");
        assertThat(personClass.getContent()).contains("public value class Person");

        PluginProtos.CodeGeneratorResponse.File personBuilderClass = response.getFile(1);
        assertThat(personBuilderClass.getName()).isEqualTo("com/example/PersonBuilder.java");
        assertThat(personBuilderClass.getContent()).contains("public class PersonBuilder");
    }
}
