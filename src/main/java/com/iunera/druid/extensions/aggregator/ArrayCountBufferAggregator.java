package com.iunera.druid.extensions.aggregator;

/*-
 * #%L
 * iu-code-ingestion-druid-extension
 * %%
 * Copyright (C) 2024 Tim Frey, Christian Schmitt
 * %%
 * Licensed under the OPEN COMPENSATION TOKEN LICENSE (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * <https://github.com/open-compensation-token-license/license/blob/main/LICENSE.md>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @octl.sid: 1b6f7a5d-8dcf-44f1-b03a-77af04433496
 * #L%
 */

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.druid.query.aggregation.BufferAggregator;
import org.apache.druid.segment.ColumnValueSelector;
import org.apache.druid.segment.DimensionHandlerUtils;

/** The Class ArrayCountBufferAggregator. */
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

  /** Close. */
  @Override
  public void close() {
    // no resources to cleanup
  }
}
