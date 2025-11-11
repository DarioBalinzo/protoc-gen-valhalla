package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos.*;
import static com.dariobalinzo.protoc.valhalla.ProtoUtils.*;

/**
 * Generates JEP 401 Value Class code from Protocol Buffer descriptors
 */
public class ValueClassGenerator {

    private final StringBuilder sb = new StringBuilder();
    private final String pkg;
    private final DescriptorProto message;

    public ValueClassGenerator(String pkg, DescriptorProto message) {
        this.pkg = pkg;
        this.message = message;
    }


    public String generate() {
        String className = message.getName();

        // Package and imports
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import com.google.protobuf.*;\n");
        sb.append("import java.io.IOException;\n");
        sb.append("import java.io.OutputStream;\n");
        sb.append("import java.io.ByteArrayOutputStream;\n");
        sb.append("import java.util.*;\n\n");

        sb.append("""
            /**
             * Value class for %s
             * JEP 401: Value Classes - immutable, identity-free data carrier
             * 
             * Features:
             * - Custom parsing (no intermediate objects)
             * - Custom serialization (direct wire format)
             * - Immutable with 'with' methods for updates
             */
            """.formatted(className));

        // Value class declaration
        sb.append("public value class ").append(className).append(" {\n\n");

        // Fields (all final)
        for (FieldDescriptorProto field : message.getFieldList()) {
            sb.append("    private final ").append(getJavaType(field));
            sb.append(" ").append(field.getName()).append(";\n");
        }
        sb.append("\n");

        // Constructor
        generateConstructor(className);

        // Getters
        generateGetters();

        // With methods (immutable updates)
        generateWithMethods(className);

        // Static factory
        generateStaticFactory(className);

        // Builder factories
        generateBuilderFactories(className);

        // Custom parser
        generateParser(className);

        // Custom serialization
        generateSerialization();

        sb.append("}\n");
        return sb.toString();
    }

    private void generateConstructor(String className) {
        sb.append("    public ").append(className).append("(");
        appendParameters();
        sb.append(") {\n");
        for (FieldDescriptorProto field : message.getFieldList()) {
            String name = field.getName();
            if (isRepeatedField(field)) {
                sb.append("        this.").append(name);
                sb.append(" = ").append(name).append(" == null ? List.of() : List.copyOf(");
                sb.append(name).append(");\n");
            } else {
                sb.append("        this.").append(name).append(" = ").append(name).append(";\n");
            }
        }
        sb.append("    }\n\n");
    }

    private void generateGetters() {
        for (FieldDescriptorProto field : message.getFieldList()) {
            String methodName = "get" + capitalize(field.getName());
            sb.append("    public ").append(getJavaType(field));
            sb.append(" ").append(methodName).append("() {\n");
            sb.append("        return ").append(field.getName()).append(";\n");
            sb.append("    }\n\n");
        }
    }

    private void generateWithMethods(String className) {
        for (FieldDescriptorProto field : message.getFieldList()) {
            String methodName = "with" + capitalize(field.getName());
            sb.append("    public ").append(className);
            sb.append(" ").append(methodName).append("(");
            sb.append(getJavaType(field)).append(" value) {\n");
            sb.append("        return new ").append(className).append("(");

            boolean first = true;
            for (FieldDescriptorProto f : message.getFieldList()) {
                if (!first) sb.append(", ");
                sb.append(f.getName().equals(field.getName()) ? "value" : "this." + f.getName());
                first = false;
            }
            sb.append(");\n    }\n\n");
        }
    }

    private void generateStaticFactory(String className) {
        sb.append("    public static ").append(className).append(" getDefaultInstance() {\n");
        sb.append("        return new ").append(className).append("(");
        appendDefaults();
        sb.append(");\n    }\n\n");
    }

    private void generateBuilderFactories(String className) {
        sb.append("    public static ").append(className).append("Builder newBuilder() {\n");
        sb.append("        return new ").append(className).append("Builder();\n");
        sb.append("    }\n\n");

        sb.append("    public ").append(className).append("Builder toBuilder() {\n");
        sb.append("        return new ").append(className).append("Builder(this);\n");
        sb.append("    }\n\n");
    }

    private void generateParser(String className) {
        sb.append("""
                /**
                 * Custom parser - constructs value class directly from wire format
                 * No intermediate builder objects for maximum performance!
                 */
                public static %s parseFrom(byte[] data) throws IOException {
                    CodedInputStream input = CodedInputStream.newInstance(data);
                    return parseFrom(input);
                }
            
                public static %s parseFrom(CodedInputStream input) throws IOException {
            """.formatted(className, className));

        // Initialize local variables with defaults
        for (FieldDescriptorProto field : message.getFieldList()) {
            sb.append("        ").append(getJavaType(field)).append(" ");
            sb.append(field.getName()).append(" = ").append(getDefaultValue(field)).append(";\n");
        }

        sb.append("""
            
                    while (!input.isAtEnd()) {
                        int tag = input.readTag();
                        switch (tag) {
            """);

        // Generate case for each field
        for (FieldDescriptorProto field : message.getFieldList()) {
            int tag = (field.getNumber() << 3) | getWireType(field);
            sb.append("                case ").append(tag).append(": // field ").append(field.getNumber());
            sb.append(": ").append(field.getName()).append("\n");

            if (isRepeatedField(field)) {
                sb.append("""
                                    if (%s.isEmpty()) {
                                        %s = new ArrayList<>();
                                    }
                                    %s.add(%s);
                                    break;
                    """.formatted(
                        field.getName(),
                        field.getName(),
                        field.getName(),
                        getReadExpression(field)
                ));
            } else {
                sb.append("                    ").append(field.getName()).append(" = ");
                sb.append(getReadExpression(field));
                sb.append(";\n");
                sb.append("                    break;\n");
            }
        }

        sb.append("""
                            default:
                                input.skipField(tag);
                                break;
                        }
                    }
            
            """);

        // Construct and return value class
        sb.append("        return new ").append(className).append("(");
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(field.getName());
            first = false;
        }
        sb.append(");\n");
        sb.append("    }\n\n");
    }

    private void generateSerialization() {
        sb.append("""
                /**
                 * Custom serialization - writes value class directly to wire format
                 * No intermediate objects for maximum performance!
                 */
                public byte[] toByteArray() throws IOException {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    CodedOutputStream output = CodedOutputStream.newInstance(baos);
                    writeTo(output);
                    output.flush();
                    return baos.toByteArray();
                }
            
                public void writeTo(CodedOutputStream output) throws IOException {
            """);

        // Write each field
        for (FieldDescriptorProto field : message.getFieldList()) {
            String name = field.getName();
            int fieldNumber = field.getNumber();

            if (isRepeatedField(field)) {
                // Handle repeated fields
                sb.append("        for (").append(getBaseJavaType(field)).append(" item : ");
                sb.append("this.").append(name).append(") {\n");
                sb.append("            ");
                generateWriteStatement(field, "item", fieldNumber);
                sb.append("        }\n");
            } else {
                // Handle singular fields - only write if not default
                String defaultCheck = getDefaultCheck(field).replace(name, "this." + name);
                sb.append("        if (").append(defaultCheck).append(") {\n");
                sb.append("            ");
                generateWriteStatement(field, "this." + name, fieldNumber);
                sb.append("        }\n");
            }
        }

        sb.append("    }\n\n");

        // Add getSerializedSize method
        generateGetSerializedSize();
    }

    private void generateWriteStatement(FieldDescriptorProto field,
                                               String varName, int fieldNumber) {
        String writeMethod = getWriteMethod(field);

        switch (field.getType()) {
            case TYPE_MESSAGE:
                sb.append("""
                    output.writeTag(%d, WireFormat.WIRETYPE_LENGTH_DELIMITED);
                                byte[] messageBytes = %s.toByteArray();
                                output.writeUInt32NoTag(messageBytes.length);
                                output.writeRawBytes(messageBytes);
                    """.formatted(fieldNumber, varName));
                break;
            case TYPE_STRING:
                sb.append("output.writeString(").append(fieldNumber).append(", ").append(varName).append(");\n");
                break;
            case TYPE_BYTES:
                sb.append("output.writeBytes(").append(fieldNumber).append(", ").append(varName).append(");\n");
                break;
            default:
                sb.append("output.write").append(writeMethod).append("(");
                sb.append(fieldNumber).append(", ").append(varName).append(");\n");
                break;
        }
    }

    private void generateGetSerializedSize() {
        sb.append("""
                /**
                 * Computes the serialized size of this message
                 */
                public int getSerializedSize() {
                    int size = 0;
            """);

        for (FieldDescriptorProto field : message.getFieldList()) {
            String name = field.getName();
            int fieldNumber = field.getNumber();

            if (isRepeatedField(field)) {
                sb.append("        for (").append(getBaseJavaType(field)).append(" item : ");
                sb.append("this.").append(name).append(") {\n");
                sb.append("            size += ");
                generateSizeExpression(field, "item", fieldNumber);
                sb.append(";\n        }\n");
            } else {
                String defaultCheck = getDefaultCheck(field).replace(name, "this." + name);
                sb.append("        if (").append(defaultCheck).append(") {\n");
                sb.append("            size += ");
                generateSizeExpression(field, "this." + name, fieldNumber);
                sb.append(";\n        }\n");
            }
        }

        sb.append("        return size;\n");
        sb.append("    }\n\n");
    }

    private void generateSizeExpression(FieldDescriptorProto field,
                                               String varName, int fieldNumber) {
        switch (field.getType()) {
            case TYPE_MESSAGE:
                sb.append("CodedOutputStream.computeTagSize(").append(fieldNumber).append(") + ");
                sb.append("CodedOutputStream.computeUInt32SizeNoTag(").append(varName);
                sb.append(".getSerializedSize()) + ").append(varName).append(".getSerializedSize()");
                break;
            case TYPE_STRING:
                sb.append("CodedOutputStream.computeStringSize(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_BYTES:
                sb.append("CodedOutputStream.computeBytesSize(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_INT32:
                sb.append("CodedOutputStream.computeInt32Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_INT64:
                sb.append("CodedOutputStream.computeInt64Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_UINT32:
                sb.append("CodedOutputStream.computeUInt32Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_UINT64:
                sb.append("CodedOutputStream.computeUInt64Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_SINT32:
                sb.append("CodedOutputStream.computeSInt32Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_SINT64:
                sb.append("CodedOutputStream.computeSInt64Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_FIXED32:
                sb.append("CodedOutputStream.computeFixed32Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_FIXED64:
                sb.append("CodedOutputStream.computeFixed64Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_SFIXED32:
                sb.append("CodedOutputStream.computeSFixed32Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_SFIXED64:
                sb.append("CodedOutputStream.computeSFixed64Size(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_FLOAT:
                sb.append("CodedOutputStream.computeFloatSize(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_DOUBLE:
                sb.append("CodedOutputStream.computeDoubleSize(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            case TYPE_BOOL:
                sb.append("CodedOutputStream.computeBoolSize(").append(fieldNumber);
                sb.append(", ").append(varName).append(")");
                break;
            default:
                sb.append("0");
        }
    }

    private void appendParameters() {
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(getJavaType(field)).append(" ").append(field.getName());
            first = false;
        }
    }

    private void appendDefaults() {
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(getDefaultValue(field));
            first = false;
        }
    }
}