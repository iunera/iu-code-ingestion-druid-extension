/*
 * Copyright 2023 Tim Frey
 * This is a project for useful Druid extensions in the Fahrbar project. The project is not to be
 * distributed or for commercial use. It is in the current state for evaluation purposes only. This
 * software has no warranties and no special use rights are granted other than evaluation.
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


// check also import org.apache.druid.java.util.common.StringUtils;
public class ArrayCountBufferAggregator implements BufferAggregator
{
  private final ColumnValueSelector<List<Long>> selector;
  private static final int FOUND_AND_NULL_FLAG_VALUE = -1;
  private static final int NOT_FOUND_FLAG_VALUE = -2;
  private static final int FOUND_VALUE_OFFSET = Integer.BYTES;
  int maxStringBytes=10000000;
  ArrayCountBufferAggregator(ColumnValueSelector<List<Long>> selector)
  {
    this.selector = selector;
  }

  @Override
  public void init(final ByteBuffer buf, final int position)
  {
    buf.putInt(position, NOT_FOUND_FLAG_VALUE);
  }


  @Override
  public void aggregate(ByteBuffer buf, int position)
  {
    if (buf.getInt(position) == NOT_FOUND_FLAG_VALUE) {
      final Object object = this.selector.getObject();
      byte[] foundValue =   SerializationUtils.serialize((Serializable) object);
          
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

  
  @Override
  public Object get(ByteBuffer buf, int position)
  {
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

  @Override
  public final float getFloat(ByteBuffer buf, int position)
  {  throw new UnsupportedOperationException("Not implemented");
    //return (float) buf.getDouble(position);
  }

  @Override
  public final long getLong(ByteBuffer buf, int position)
  {  throw new UnsupportedOperationException("Not implemented");
   // return (long) buf.getDouble(position);
  }

  @Override
  public void close()
  {
    // no resources to cleanup
  }
}
