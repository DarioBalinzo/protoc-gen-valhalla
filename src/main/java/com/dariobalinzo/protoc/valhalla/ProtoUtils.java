package com.dariobalinzo.protoc.valhalla;


import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

class ProtoUtils {


    static String getJavaType(FieldDescriptorProto field) {
        if (isRepeatedField(field)) {
            return "List<" + getBaseJavaType(field) + ">";
        }
        return getBaseJavaType(field);
    }

    static String getBaseJavaType(FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_INT32:
            case TYPE_SINT32:
            case TYPE_SFIXED32:
                return "int";
            case TYPE_INT64:
            case TYPE_SINT64:
            case TYPE_SFIXED64:
                return "long";
            case TYPE_UINT32:
            case TYPE_FIXED32:
                return "int";
            case TYPE_UINT64:
            case TYPE_FIXED64:
                return "long";
            case TYPE_FLOAT:
                return "float";
            case TYPE_DOUBLE:
                return "double";
            case TYPE_BOOL:
                return "boolean";
            case TYPE_STRING:
                return "String";
            case TYPE_BYTES:
                return "ByteString";
            case TYPE_MESSAGE:
                String typeName = field.getTypeName();
                return typeName.substring(typeName.lastIndexOf('.') + 1);
            case TYPE_ENUM:
                String enumName = field.getTypeName();
                return enumName.substring(enumName.lastIndexOf('.') + 1);
            default:
                return "Object";
        }
    }

    static String getDefaultValue(FieldDescriptorProto field) {
        if (isRepeatedField(field)) {
            return "List.of()";
        }
        switch (field.getType()) {
            case TYPE_INT32:
            case TYPE_SINT32:
            case TYPE_SFIXED32:
            case TYPE_UINT32:
            case TYPE_FIXED32:
                return "0";
            case TYPE_INT64:
            case TYPE_SINT64:
            case TYPE_SFIXED64:
            case TYPE_UINT64:
            case TYPE_FIXED64:
                return "0L";
            case TYPE_FLOAT:
                return "0.0f";
            case TYPE_DOUBLE:
                return "0.0";
            case TYPE_BOOL:
                return "false";
            case TYPE_STRING:
                return "\"\"";
            case TYPE_BYTES:
                return "ByteString.EMPTY";
            case TYPE_MESSAGE:
            case TYPE_ENUM:
                return "null";
            default:
                return "null";
        }
    }

    static String getReadExpression(FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_INT32:
                return "input.readInt32()";
            case TYPE_INT64:
                return "input.readInt64()";
            case TYPE_UINT32:
                return "input.readUInt32()";
            case TYPE_UINT64:
                return "input.readUInt64()";
            case TYPE_SINT32:
                return "input.readSInt32()";
            case TYPE_SINT64:
                return "input.readSInt64()";
            case TYPE_FIXED32:
                return "input.readFixed32()";
            case TYPE_FIXED64:
                return "input.readFixed64()";
            case TYPE_SFIXED32:
                return "input.readSFixed32()";
            case TYPE_SFIXED64:
                return "input.readSFixed64()";
            case TYPE_FLOAT:
                return "input.readFloat()";
            case TYPE_DOUBLE:
                return "input.readDouble()";
            case TYPE_BOOL:
                return "input.readBool()";
            case TYPE_STRING:
                return "input.readString()";
            case TYPE_BYTES:
                return "input.readBytes()";
            case TYPE_MESSAGE:
                String typeName = field.getTypeName();
                String className = typeName.substring(typeName.lastIndexOf('.') + 1);
                return className + ".parseFrom(input.readBytes().toByteArray())";
            default:
                return "null";
        }
    }

    static String getWriteMethod(FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_INT32:
                return "Int32";
            case TYPE_INT64:
                return "Int64";
            case TYPE_UINT32:
                return "UInt32";
            case TYPE_UINT64:
                return "UInt64";
            case TYPE_SINT32:
                return "SInt32";
            case TYPE_SINT64:
                return "SInt64";
            case TYPE_FIXED32:
                return "Fixed32";
            case TYPE_FIXED64:
                return "Fixed64";
            case TYPE_SFIXED32:
                return "SFixed32";
            case TYPE_SFIXED64:
                return "SFixed64";
            case TYPE_FLOAT:
                return "Float";
            case TYPE_DOUBLE:
                return "Double";
            case TYPE_BOOL:
                return "Bool";
            case TYPE_STRING:
                return "String";
            case TYPE_BYTES:
                return "Bytes";
            case TYPE_MESSAGE:
                return "Message";
            default:
                return "Unknown";
        }
    }

    static int getWireType(FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_INT32:
            case TYPE_INT64:
            case TYPE_UINT32:
            case TYPE_UINT64:
            case TYPE_SINT32:
            case TYPE_SINT64:
            case TYPE_BOOL:
            case TYPE_ENUM:
                return 0; // VARINT
            case TYPE_FIXED64:
            case TYPE_SFIXED64:
            case TYPE_DOUBLE:
                return 1; // FIXED64
            case TYPE_STRING:
            case TYPE_BYTES:
            case TYPE_MESSAGE:
                return 2; // LENGTH_DELIMITED
            case TYPE_FIXED32:
            case TYPE_SFIXED32:
            case TYPE_FLOAT:
                return 5; // FIXED32
            default:
                return 0;
        }
    }

    static String getDefaultCheck(FieldDescriptorProto field) {
        String name = field.getName();
        switch (field.getType()) {
            case TYPE_INT32:
            case TYPE_UINT32:
            case TYPE_SINT32:
            case TYPE_FIXED32:
            case TYPE_SFIXED32:
                return name + " != 0";
            case TYPE_INT64:
            case TYPE_UINT64:
            case TYPE_SINT64:
            case TYPE_FIXED64:
            case TYPE_SFIXED64:
                return name + " != 0L";
            case TYPE_FLOAT:
                return name + " != 0.0f";
            case TYPE_DOUBLE:
                return name + " != 0.0";
            case TYPE_BOOL:
                return name;
            case TYPE_STRING:
                return "!" + name + ".isEmpty()";
            case TYPE_BYTES:
                return "!" + name + ".isEmpty()";
            case TYPE_MESSAGE:
            case TYPE_ENUM:
                return name + " != null";
            default:
                return "true";
        }
    }

    static boolean isRepeatedField(FieldDescriptorProto field) {
        return field.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED;
    }

    static void appendParameters(StringBuilder sb, DescriptorProto message) {
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(getJavaType(field)).append(" ").append(field.getName());
            first = false;
        }
    }

    static void appendDefaults(StringBuilder sb, DescriptorProto message) {
        boolean first = true;
        for (FieldDescriptorProto field : message.getFieldList()) {
            if (!first) sb.append(", ");
            sb.append(getDefaultValue(field));
            first = false;
        }
    }

    static String getJavaPackage(FileDescriptorProto file) {
        if (file.hasOptions() && file.getOptions().hasJavaPackage()) {
            return file.getOptions().getJavaPackage();
        }
        return file.getPackage().isEmpty() ? "generated" : file.getPackage();
    }

     static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static String getSingularName(String plural) {
        if (plural.endsWith("ies")) {
            return plural.substring(0, plural.length() - 3) + "y";
        } else if (plural.endsWith("s")) {
            return plural.substring(0, plural.length() - 1);
        }
        return plural;
    }
}