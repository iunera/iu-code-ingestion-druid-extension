/*
 * Copyright 2023 Tim Frey This is a project for useful Druid extensions in the Fahrbar project. The
 * project is not to be distributed or for commercial use. It is in the current state for evaluation
 * purposes only. This software has no warranties and no special use rights are granted other than
 * evaluation.
 */
package com.iunera.druid.extensions.aggregator;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.query.aggregation.BufferAggregator;
import org.apache.druid.segment.BaseFloatColumnValueSelector;
import org.apache.druid.segment.ColumnValueSelector;
import org.apache.druid.segment.DimensionHandlerUtils;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;


/**
 * The Class ArrayCountBufferAggregator.
 */
// check also import org.apache.druid.java.util.common.StringUtils;
public class ArrayCountBufferAggregator implements BufferAggregator {

  /** The selector. */
  private final ColumnValueSelector<List<Long>> selector;

  /** The Constant FOUND_AND_NULL_FLAG_VALUE. */
  private static final int FOUND_AND_NULL_FLAG_VALUE = -1;

  /** The Constant NOT_FOUND_FLAG_VALUE. */
  private static final int NOT_FOUND_FLAG_VALUE = -2;

  /** The Constant FOUND_VALUE_OFFSET. */
  private static final int FOUND_VALUE_OFFSET = Integer.BYTES;

  /** The max string bytes. */
  int maxStringBytes = 10000000;

  /**
   * Instantiates a new array count buffer aggregator.
   *
   * @param selector the selector
   */
  ArrayCountBufferAggregator(ColumnValueSelector<List<Long>> selector) {
    this.selector = selector;
  }

  /**
   * Inits the.
   *
   * @param buf the buf
   * @param position the position
   */
  @Override
  public void init(final ByteBuffer buf, final int position) {
    buf.putInt(position, NOT_FOUND_FLAG_VALUE);
  }


  /**
   * Aggregate.
   *
   * @param buf the buf
   * @param position the position
   */
  @Override
  public void aggregate(ByteBuffer buf, int position) {
    if (buf.getInt(position) == NOT_FOUND_FLAG_VALUE) {
      final Object object = this.selector.getObject();
      byte[] foundValue = SerializationUtils.serialize((Serializable) object);

      DimensionHandlerUtils.convertObjectToString(object);
      if (foundValue != null) {
        ByteBuffer mutationBuffer = buf.duplicate();
        mutationBuffer.position(position + FOUND_VALUE_OFFSET);

        mutationBuffer.limit(position + FOUND_VALUE_OFFSET + maxStringBytes);
        final int len = foundValue.length;
        mutationBuffer.putInt(position, len);
      } else {
        buf.putInt(position, FOUND_AND_NULL_FLAG_VALUE);
      }
    }
  }


  /**
   * Gets the.
   *
   * @param buf the buf
   * @param position the position
   * @return the object
   */
  @Override
  public Object get(ByteBuffer buf, int position) {
    ByteBuffer copyBuffer = buf.duplicate();
    copyBuffer.position(position);
    int stringSizeBytes = copyBuffer.getInt();
    if (stringSizeBytes >= 0) {
      byte[] valueBytes = new byte[stringSizeBytes];
      copyBuffer.get(valueBytes, 0, stringSizeBytes);
      return SerializationUtils.deserialize(valueBytes);
    } else {
      return null;
    }
  }

  /**
   * Gets the float.
   *
   * @param buf the buf
   * @param position the position
   * @return the float
   */
  @Override
  public final float getFloat(ByteBuffer buf, int position) {
    throw new UnsupportedOperationException("Not implemented");
    // return (float) buf.getDouble(position);
  }

  /**
   * Gets the long.
   *
   * @param buf the buf
   * @param position the position
   * @return the long
   */
  @Override
  public final long getLong(ByteBuffer buf, int position) {
    throw new UnsupportedOperationException("Not implemented");
    // return (long) buf.getDouble(position);
  }

  /**
   * Close.
   */
  @Override
  public void close() {
    // no resources to cleanup
  }
}
