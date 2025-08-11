/*
 *
 * Copyright (c) 2025, iot-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.onenet.protocol.onenet.mq;

public final class OnenetMq {

  private OnenetMq() {
  }

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public interface MsgOrBuilder
      extends
      // @@protoc_insertion_point(interface_extends:com.onenet.mq.Msg)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 msgid = 1;</code>
     */
    long getMsgid();

    /**
     * <code>bytes data = 2;</code>
     */
    com.google.protobuf.ByteString getData();

    /**
     * <code>uint64 timestamp = 3;</code>
     */
    long getTimestamp();
  }

  /**
   * Protobuf type {@code com.onenet.mq.Msg}
   */
  public static final class Msg extends com.google.protobuf.GeneratedMessageV3
      implements
      // @@protoc_insertion_point(message_implements:com.onenet.mq.Msg)
      MsgOrBuilder {

    private static final long serialVersionUID = 0L;

    // Use Msg.newBuilder() to construct.
    private Msg(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }

    private Msg() {
      data_ = com.google.protobuf.ByteString.EMPTY;
    }

    @Override
    @SuppressWarnings({"unused"})
    protected Object newInstance(UnusedPrivateParameter unused) {
      return new Msg();
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
    }

    private Msg(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              msgid_ = input.readUInt64();
              break;
            }
            case 18: {
              data_ = input.readBytes();
              break;
            }
            case 24: {
              timestamp_ = input.readUInt64();
              break;
            }
            default: {
              if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }

    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return OnenetMq.internal_static_com_onenet_mq_Msg_descriptor;
    }

    @Override
    protected FieldAccessorTable internalGetFieldAccessorTable() {
      return OnenetMq.internal_static_com_onenet_mq_Msg_fieldAccessorTable
          .ensureFieldAccessorsInitialized(OnenetMq.Msg.class, OnenetMq.Msg.Builder.class);
    }

    public static final int MSGID_FIELD_NUMBER = 1;
    private long msgid_;

    /**
     * <code>uint64 msgid = 1;</code>
     */
    public long getMsgid() {
      return msgid_;
    }

    public static final int DATA_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString data_;

    /**
     * <code>bytes data = 2;</code>
     */
    public com.google.protobuf.ByteString getData() {
      return data_;
    }

    public static final int TIMESTAMP_FIELD_NUMBER = 3;
    private long timestamp_;

    /**
     * <code>uint64 timestamp = 3;</code>
     */
    public long getTimestamp() {
      return timestamp_;
    }

    private byte memoizedIsInitialized = -1;

    @Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) {
        return true;
      }
      if (isInitialized == 0) {
        return false;
      }

      memoizedIsInitialized = 1;
      return true;
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
      if (msgid_ != 0L) {
        output.writeUInt64(1, msgid_);
      }
      if (!data_.isEmpty()) {
        output.writeBytes(2, data_);
      }
      if (timestamp_ != 0L) {
        output.writeUInt64(3, timestamp_);
      }
      unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) {
        return size;
      }

      size = 0;
      if (msgid_ != 0L) {
        size += com.google.protobuf.CodedOutputStream.computeUInt64Size(1, msgid_);
      }
      if (!data_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, data_);
      }
      if (timestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream.computeUInt64Size(3, timestamp_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof OnenetMq.Msg)) {
        return super.equals(obj);
      }
      OnenetMq.Msg other = (OnenetMq.Msg) obj;

      if (getMsgid() != other.getMsgid()) {
        return false;
      }
      if (!getData().equals(other.getData())) {
        return false;
      }
      if (getTimestamp() != other.getTimestamp()) {
        return false;
      }
      if (!unknownFields.equals(other.unknownFields)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + MSGID_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getMsgid());
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + getData().hashCode();
      hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getTimestamp());
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static OnenetMq.Msg parseFrom(java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }

    public static OnenetMq.Msg parseFrom(
        java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }

    public static OnenetMq.Msg parseFrom(com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }

    public static OnenetMq.Msg parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }

    public static OnenetMq.Msg parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }

    public static OnenetMq.Msg parseFrom(
        byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }

    public static OnenetMq.Msg parseFrom(java.io.InputStream input) throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }

    public static OnenetMq.Msg parseFrom(
        java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
          PARSER, input, extensionRegistry);
    }

    public static OnenetMq.Msg parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
    }

    public static OnenetMq.Msg parseDelimitedFrom(
        java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
          PARSER, input, extensionRegistry);
    }

    public static OnenetMq.Msg parseFrom(com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }

    public static OnenetMq.Msg parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
          PARSER, input, extensionRegistry);
    }

    @Override
    public Builder newBuilderForType() {
      return newBuilder();
    }

    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(OnenetMq.Msg prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    @Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }

    /**
     * Protobuf type {@code com.onenet.mq.Msg}
     */
    public static final class Builder
        extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
        implements
        // @@protoc_insertion_point(builder_implements:com.onenet.mq.Msg)
        OnenetMq.MsgOrBuilder {

      public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return OnenetMq.internal_static_com_onenet_mq_Msg_descriptor;
      }

      @Override
      protected FieldAccessorTable internalGetFieldAccessorTable() {
        return OnenetMq.internal_static_com_onenet_mq_Msg_fieldAccessorTable
            .ensureFieldAccessorsInitialized(OnenetMq.Msg.class, OnenetMq.Msg.Builder.class);
      }

      // Construct using com.onenet.mq.OnenetMq.Msg.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }

      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
        }
      }

      @Override
      public Builder clear() {
        super.clear();
        msgid_ = 0L;

        data_ = com.google.protobuf.ByteString.EMPTY;

        timestamp_ = 0L;

        return this;
      }

      @Override
      public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
        return OnenetMq.internal_static_com_onenet_mq_Msg_descriptor;
      }

      @Override
      public OnenetMq.Msg getDefaultInstanceForType() {
        return OnenetMq.Msg.getDefaultInstance();
      }

      @Override
      public OnenetMq.Msg build() {
        OnenetMq.Msg result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @Override
      public OnenetMq.Msg buildPartial() {
        OnenetMq.Msg result = new OnenetMq.Msg(this);
        result.msgid_ = msgid_;
        result.data_ = data_;
        result.timestamp_ = timestamp_;
        onBuilt();
        return result;
      }

      @Override
      public Builder clone() {
        return super.clone();
      }

      @Override
      public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
        return super.setField(field, value);
      }

      @Override
      public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }

      @Override
      public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }

      @Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field, int index, Object value) {
        return super.setRepeatedField(field, index, value);
      }

      @Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
        return super.addRepeatedField(field, value);
      }

      @Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof OnenetMq.Msg) {
          return mergeFrom((OnenetMq.Msg) other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(OnenetMq.Msg other) {
        if (other == OnenetMq.Msg.getDefaultInstance()) {
          return this;
        }
        if (other.getMsgid() != 0L) {
          setMsgid(other.getMsgid());
        }
        if (other.getData() != com.google.protobuf.ByteString.EMPTY) {
          setData(other.getData());
        }
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @Override
      public final boolean isInitialized() {
        return true;
      }

      @Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        OnenetMq.Msg parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (OnenetMq.Msg) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long msgid_;

      /**
       * <code>uint64 msgid = 1;</code>
       */
      public long getMsgid() {
        return msgid_;
      }

      /**
       * <code>uint64 msgid = 1;</code>
       */
      public Builder setMsgid(long value) {

        msgid_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>uint64 msgid = 1;</code>
       */
      public Builder clearMsgid() {

        msgid_ = 0L;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

      /**
       * <code>bytes data = 2;</code>
       */
      public com.google.protobuf.ByteString getData() {
        return data_;
      }

      /**
       * <code>bytes data = 2;</code>
       */
      public Builder setData(com.google.protobuf.ByteString value) {
        if (value == null) {
          throw new NullPointerException();
        }

        data_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>bytes data = 2;</code>
       */
      public Builder clearData() {

        data_ = getDefaultInstance().getData();
        onChanged();
        return this;
      }

      private long timestamp_;

      /**
       * <code>uint64 timestamp = 3;</code>
       */
      public long getTimestamp() {
        return timestamp_;
      }

      /**
       * <code>uint64 timestamp = 3;</code>
       */
      public Builder setTimestamp(long value) {

        timestamp_ = value;
        onChanged();
        return this;
      }

      /**
       * <code>uint64 timestamp = 3;</code>
       */
      public Builder clearTimestamp() {

        timestamp_ = 0L;
        onChanged();
        return this;
      }

      @Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }

      // @@protoc_insertion_point(builder_scope:com.onenet.mq.Msg)
    }

    // @@protoc_insertion_point(class_scope:com.onenet.mq.Msg)
    private static final OnenetMq.Msg DEFAULT_INSTANCE;

    static {
      DEFAULT_INSTANCE = new OnenetMq.Msg();
    }

    public static OnenetMq.Msg getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Msg> PARSER =
        new com.google.protobuf.AbstractParser<Msg>() {
          @Override
          public Msg parsePartialFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws com.google.protobuf.InvalidProtocolBufferException {
            return new Msg(input, extensionRegistry);
          }
        };

    public static com.google.protobuf.Parser<Msg> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Msg> getParserForType() {
      return PARSER;
    }

    @Override
    public OnenetMq.Msg getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }
  }

  private static final com.google.protobuf.Descriptors.Descriptor
      internal_static_com_onenet_mq_Msg_descriptor;
  private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_onenet_mq_Msg_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    String[] descriptorData = {
        "\n\017onenet-mq.proto\022\rcom.onenet.mq\"5\n\003Msg\022"
            + "\r\n\005msgid\030\001 \001(\004\022\014\n\004data\030\002 \001(\014\022\021\n\ttimestam"
            + "p\030\003 \001(\004b\006proto3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[]{});
    internal_static_com_onenet_mq_Msg_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_com_onenet_mq_Msg_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_com_onenet_mq_Msg_descriptor,
            new String[]{
                "Msgid", "Data", "Timestamp",
            });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
