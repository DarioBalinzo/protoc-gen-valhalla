package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos.*;
import static com.dariobalinzo.protoc.valhalla.ProtoUtils.*;

/**
 * Generates Builder class code for constructing Value Classes
 */
public class BuilderGenerator {

    public static String generate(DescriptorProto message, String pkg) {
        StringBuilder sb = new StringBuilder();
        String className = message.getName();
        String builderName = className + "Builder";

        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import java.util.*;\n\n");

        sb.append("""
            /**
             * Separate builder class for %s
             * Mutable builder pattern - construct then build immutable value class
             */
            public class %s {
            
            """.formatted(className, builderName));

        // Mutable fields
        generateFields(sb, message);

        // Default constructor
        sb.append("    public ").append(builderName).append("() {}\n\n");

        // Copy constructor
        generateCopyConstructor(sb, message, className, builderName);

        // Setters
        generateSetters(sb, message, builderName);

        // Getters
        generateGetters(sb, message);

        // Build method
        generateBuildMethod(sb, message, className);

        sb.append("}\n");
        return sb.toString();
    }

    private static void generateFields(StringBuilder sb, DescriptorProto message) {
        for (FieldDescriptorProto field : message.getFieldList()) {
            sb.append("    private ").append(getJavaType(field));
            sb.append(" ").append(field.getName());
            sb.append(" = ").append(getDefaultValue(field)).append(";\n");
        }
        sb.append("\n");
    }

    private static void generateCopyConstructor(StringBuilder sb, DescriptorProto message,
                                                String className, String builderName) {
        sb.append("    public ").append(builderName).append("(").append(className);
        sb.append(" original) {\n");
        for (FieldDescriptorProto field : message.getFieldList()) {
            String name = field.getName();
            sb.append("        this.").append(name).append(" = original.get");
            sb.append(capitalize(name)).append("();\n");
        }
        sb.append("    }\n\n");
    }

    private static void generateSetters(StringBuilder sb, DescriptorProto message, String builderName) {
        for (FieldDescriptorProto field : message.getFieldList()) {
            String name = field.getName();
            String methodName = "set" + capitalize(name);

            sb.append("    public ").append(builderName).append(" ");
            sb.append(methodName).append("(").append(getJavaType(field));
            sb.append(" value) {\n");
            sb.append("        this.").append(name).append(" = value;\n");
            sb.append("        return this;\n");
            sb.append("    }\n\n");

            // For repeated fields, add adder
            if (isRepeatedField(field)) {
                String singularName = getSingularName(name);
                sb.append("    public ").append(builderName).append(" add");
                sb.append(capitalize(singularName)).append("(");
                sb.append(getBaseJavaType(field)).append(" value) {\n");
                sb.append("        if (this.").append(name).append(".isEmpty()) {\n");
                sb.append("            this.").append(name).append(" = new ArrayList<>();\n");
                sb.append("        }\n");
                sb.append("        this.").append(name).append(".add(value);\n");
                sb.append("        return this;\n");
                sb.append("    }\n\n");
            }
        }
    }

    private static void generateGetters(StringBuilder sb, DescriptorProto message) {
        for (FieldDescriptorProto field : message.getFieldList()) {
            String methodName = "get" + capitalize(field.getName());
            sb.append("    public ").append(getJavaType(field));
            sb.append(" ").append(methodName).append("() {\n");
            sb.append("        return ").append(field.getName()).append(";\n");
            sb.append("    }\n\n");
        }
    }

    private static void generateBuildMethod(StringBuilder sb, DescriptorProto message, String className) {
        sb.append("    public ").append(className).append(" build() {\n");
        sb.append("        return new ").append(className).append("(");
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(field.getName());
            first = false;
        }
        sb.append(");\n");
        sb.append("    }\n");
    }
}