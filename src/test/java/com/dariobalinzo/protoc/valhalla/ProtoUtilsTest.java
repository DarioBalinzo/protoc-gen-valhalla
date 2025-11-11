package com.dariobalinzo.protoc.valhalla;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProtoUtilsTest {

    @Test
    void testGetJavaType() {
        FieldDescriptorProto singularField = FieldDescriptorProto.newBuilder()
                .setType(FieldDescriptorProto.Type.TYPE_STRING)
                .build();
        assertThat(ProtoUtils.getJavaType(singularField)).isEqualTo("String");

        FieldDescriptorProto repeatedField = FieldDescriptorProto.newBuilder()
                .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                .setType(FieldDescriptorProto.Type.TYPE_INT32)
                .build();
        assertThat(ProtoUtils.getJavaType(repeatedField)).isEqualTo("List<Integer>");
    }

    @Test
    void testGetBaseJavaType() {
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_INT32))).isEqualTo("int");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_STRING))).isEqualTo("String");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_BYTES))).isEqualTo("ByteString");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_DOUBLE))).isEqualTo("double");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_FLOAT))).isEqualTo("float");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_BOOL))).isEqualTo("boolean");
        assertThat(ProtoUtils.getBaseJavaType(field(FieldDescriptorProto.Type.TYPE_INT64))).isEqualTo("long");
        FieldDescriptorProto messageField = FieldDescriptorProto.newBuilder()
                .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                .setTypeName(".com.dariobalinzo.MyMessage")
                .build();
        assertThat(ProtoUtils.getBaseJavaType(messageField)).isEqualTo("MyMessage");
    }

    @Test
    void testGetDefaultValue() {
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_INT32))).isEqualTo("0");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_INT64))).isEqualTo("0L");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_FLOAT))).isEqualTo("0.0f");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_DOUBLE))).isEqualTo("0.0");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_BOOL))).isEqualTo("false");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_STRING))).isEqualTo("\"\"");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_BYTES))).isEqualTo("ByteString.EMPTY");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_MESSAGE))).isEqualTo("null");
        assertThat(ProtoUtils.getDefaultValue(field(FieldDescriptorProto.Type.TYPE_ENUM))).isEqualTo("null");

        FieldDescriptorProto repeatedField = FieldDescriptorProto.newBuilder()
                .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                .setType(FieldDescriptorProto.Type.TYPE_INT32)
                .build();
        assertThat(ProtoUtils.getDefaultValue(repeatedField)).isEqualTo("List.of()");
    }

    @Test
    void testGetReadExpression() {
        assertThat(ProtoUtils.getReadExpression(field(FieldDescriptorProto.Type.TYPE_INT32))).isEqualTo("input.readInt32()");
        FieldDescriptorProto messageField = FieldDescriptorProto.newBuilder()
                .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
                .setTypeName(".com.dariobalinzo.MyMessage")
                .build();
        assertThat(ProtoUtils.getReadExpression(messageField)).isEqualTo("MyMessage.parseFrom(input.readBytes().toByteArray())");
    }

    @Test
    void testGetWriteMethod() {
        assertThat(ProtoUtils.getWriteMethod(field(FieldDescriptorProto.Type.TYPE_INT32))).isEqualTo("Int32");
        assertThat(ProtoUtils.getWriteMethod(field(FieldDescriptorProto.Type.TYPE_MESSAGE))).isEqualTo("Message");
    }

    @Test
    void testGetWireType() {
        assertThat(ProtoUtils.getWireType(field(FieldDescriptorProto.Type.TYPE_INT32))).isEqualTo(0); // VARINT
        assertThat(ProtoUtils.getWireType(field(FieldDescriptorProto.Type.TYPE_FIXED64))).isEqualTo(1); // FIXED64
        assertThat(ProtoUtils.getWireType(field(FieldDescriptorProto.Type.TYPE_STRING))).isEqualTo(2); // LENGTH_DELIMITED
        assertThat(ProtoUtils.getWireType(field(FieldDescriptorProto.Type.TYPE_FIXED32))).isEqualTo(5); // FIXED32
    }

    @Test
    void testGetDefaultCheck() {
        assertThat(ProtoUtils.getDefaultCheck(field(FieldDescriptorProto.Type.TYPE_INT32, "myInt"))).isEqualTo("myInt != 0");
        assertThat(ProtoUtils.getDefaultCheck(field(FieldDescriptorProto.Type.TYPE_BOOL, "myBool"))).isEqualTo("myBool");
        assertThat(ProtoUtils.getDefaultCheck(field(FieldDescriptorProto.Type.TYPE_STRING, "myString"))).isEqualTo("!myString.isEmpty()");
        assertThat(ProtoUtils.getDefaultCheck(field(FieldDescriptorProto.Type.TYPE_MESSAGE, "myMessage"))).isEqualTo("myMessage != null");
    }

    @Test
    void testIsRepeatedField() {
        FieldDescriptorProto singularField = FieldDescriptorProto.newBuilder().build();
        assertThat(ProtoUtils.isRepeatedField(singularField)).isFalse();

        FieldDescriptorProto repeatedField = FieldDescriptorProto.newBuilder()
                .setLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
                .build();
        assertThat(ProtoUtils.isRepeatedField(repeatedField)).isTrue();
    }

    @Test
    void testCapitalize() {
        assertThat(ProtoUtils.capitalize("hello")).isEqualTo("Hello");
        assertThat(ProtoUtils.capitalize("")).isEqualTo("");
        assertThat(ProtoUtils.capitalize(null)).isNull();
    }

    @Test
    void testGetSingularName() {
        assertThat(ProtoUtils.getSingularName("items")).isEqualTo("item");
        assertThat(ProtoUtils.getSingularName("cities")).isEqualTo("city");
        assertThat(ProtoUtils.getSingularName("address")).isEqualTo("address");
    }

    @Test
    void testGetJavaPackage() {
        FileDescriptorProto fileWithOption = FileDescriptorProto.newBuilder()
                .setOptions(FileOptions.newBuilder().setJavaPackage("com.example.test").build())
                .build();
        assertThat(ProtoUtils.getJavaPackage(fileWithOption)).isEqualTo("com.example.test");

        FileDescriptorProto fileWithPackage = FileDescriptorProto.newBuilder()
                .setPackage("com.example.pkg")
                .build();
        assertThat(ProtoUtils.getJavaPackage(fileWithPackage)).isEqualTo("com.example.pkg");

        FileDescriptorProto fileWithoutPackage = FileDescriptorProto.newBuilder().build();
        assertThat(ProtoUtils.getJavaPackage(fileWithoutPackage)).isEqualTo("generated");
    }


    private FieldDescriptorProto field(FieldDescriptorProto.Type type) {
        return FieldDescriptorProto.newBuilder().setType(type).build();
    }

    private FieldDescriptorProto field(FieldDescriptorProto.Type type, String name) {
        return FieldDescriptorProto.newBuilder().setType(type).setName(name).build();
    }
}
