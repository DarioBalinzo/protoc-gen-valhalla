package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.compiler.PluginProtos.*;
import com.google.protobuf.DescriptorProtos.*;
import java.io.*;

/**
 * Protoc Plugin for JEP 401 Value Classes with Serialization
 *
 * Generates:
 * 1. Value class (immutable, identity-free) with custom serialization
 * 2. Separate Builder class (mutable, builder pattern)
 * 3. Custom parser (parseFrom without intermediate objects)
 * 4. Custom serialization (toByteArray, writeTo)
 *
 * Usage:
 *   mvn clean package
 *   protoc --plugin=protoc-gen-value=./target/protoc-gen-value.jar --value_out=. message.proto
 */
public class ValueClassPlugin {

    public static void main(String[] args) throws IOException {
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);

        CodeGeneratorResponse.Builder response = CodeGeneratorResponse.newBuilder();
        response.setSupportedFeatures(
                CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL.getNumber()
        );

        for (FileDescriptorProto file : request.getProtoFileList()) {
            if (request.getFileToGenerateList().contains(file.getName())) {
                generateFiles(file, response);
            }
        }

        response.build().writeTo(System.out);
    }

    private static void generateFiles(FileDescriptorProto file,
                                      CodeGeneratorResponse.Builder response) {
        String javaPackage = ProtoUtils.getJavaPackage(file);

        for (DescriptorProto message : file.getMessageTypeList()) {
            // Generate value class
            ValueClassGenerator valueClassGenerator = new ValueClassGenerator(javaPackage, message);
            String valueClass = valueClassGenerator.generate();
            addFile(response, javaPackage, message.getName() + ".java", valueClass);

            // Generate builder class
            BuilderGenerator builderGenerator = new BuilderGenerator(javaPackage, message);
            String builderClass = builderGenerator.generate();
            addFile(response, javaPackage, message.getName() + "Builder.java", builderClass);
        }
    }

    private static void addFile(CodeGeneratorResponse.Builder response,
                                String pkg, String className, String content) {
        String fileName = pkg.replace('.', '/') + "/" + className;
        response.addFile(CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent(content)
                .build());
    }
}