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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.query.aggregation.AggregatorFactory;
import org.apache.druid.query.aggregation.AggregatorFactoryNotMergeableException;
import org.apache.druid.query.aggregation.BufferAggregator;
import org.apache.druid.segment.ColumnSelectorFactory;
import org.apache.druid.segment.column.ColumnType;

/** A factory for creating ArrayCountAggregator objects. */
public class ArrayCountAggregatorFactory extends AggregatorFactory {
  // Built-in aggregators use 1-byte codes starting with 0x00 for cache keys.
  /** The Constant CACHE_KEY_PREFIX. */
  // Extension aggregators can use 0xFF + our own site-specific byte.
  private static final byte[] CACHE_KEY_PREFIX = new byte[] {(byte) 0xFF, (byte) 0x00};

  /** The Constant TYPE_NAME. */
  // Type name for JSON.
  public static final String TYPE_NAME = "exampleSum";

  /** The Constant COMPARATOR. */
  private static final Comparator COMPARATOR =
      new Ordering() {
        @Override
        public int compare(Object o, Object o1) {
          return 1;
        }
      }.nullsFirst();

  /** The name. */
  private final String name;

  /** The field name. */
  private final String fieldName;

  /**
   * Instantiates a new array count aggregator factory.
   *
   * @param name the name
   * @param fieldName the field name
   */
  @JsonCreator
  public ArrayCountAggregatorFactory(
      @JsonProperty("name") final String name, @JsonProperty("fieldName") final String fieldName) {
    this.name = Preconditions.checkNotNull(name, "name");
    this.fieldName = Preconditions.checkNotNull(fieldName, "fieldName");
  }

  /**
   * Factorize.
   *
   * @param metricFactory the metric factory
   * @return the aggregator
   */
  @Override
  public Aggregator factorize(ColumnSelectorFactory metricFactory) {
    return new ArrayCountAggregator(metricFactory.makeColumnValueSelector(fieldName));
  }

  /**
   * Factorize buffered.
   *
   * @param metricFactory the metric factory
   * @return the buffer aggregator
   */
  @Override
  public BufferAggregator factorizeBuffered(ColumnSelectorFactory metricFactory) {
    return new ArrayCountBufferAggregator(metricFactory.makeColumnValueSelector(fieldName));
  }

  /**
   * Gets the comparator.
   *
   * @return the comparator
   */
  @Override
  public Comparator getComparator() {
    return COMPARATOR;
  }

  /**
   * Combine.
   *
   * @param lhs the lhs
   * @param rhs the rhs
   * @return the object
   */
  @Override
  public Object combine(Object lhs, Object rhs) {
    List<Long> lhsl = (List<Long>) lhs;
    List<Long> lrhs = (List<Long>) rhs;
    List<Long> ret = new ArrayList<>(lhsl.size());
    for (int i = 0; i < lhsl.size(); i++) {
      ret.add(lhsl.get(i) + lrhs.get(i));
    }
    return ret;
  }

  /**
   * Gets the combining factory.
   *
   * @return the combining factory
   */
  @Override
  public AggregatorFactory getCombiningFactory() {
    return new ArrayCountAggregatorFactory(name, name);
  }

  /**
   * Gets the merging factory.
   *
   * @param other the other
   * @return the merging factory
   * @throws AggregatorFactoryNotMergeableException the aggregator factory not mergeable exception
   */
  @Override
  public AggregatorFactory getMergingFactory(AggregatorFactory other)
      throws AggregatorFactoryNotMergeableException {
    if (other.getName().equals(this.getName()) && this.getClass() == other.getClass()) {
      return getCombiningFactory();
    } else {
      throw new AggregatorFactoryNotMergeableException(this, other);
    }
  }

  /**
   * Gets the required columns.
   *
   * @return the required columns
   */
  @Override
  public List<AggregatorFactory> getRequiredColumns() {
    return ImmutableList.of(new ArrayCountAggregatorFactory(fieldName, fieldName));
  }

  /**
   * Deserialize.
   *
   * @param object the object
   * @return the object
   */
  @Override
  public Object deserialize(Object object) {

    return object;
  }

  /**
   * Finalize computation.
   *
   * @param object the object
   * @return the object
   */
  @Override
  public Object finalizeComputation(Object object) {
    return object;
  }

  /**
   * Gets the field name.
   *
   * @return the field name
   */
  @JsonProperty
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @JsonProperty
  public String getName() {
    return name;
  }

  /**
   * Required fields.
   *
   * @return the list
   */
  @Override
  public List<String> requiredFields() {
    return ImmutableList.of(fieldName);
  }

  /**
   * Gets the cache key.
   *
   * @return the cache key
   */
  @Override
  public byte[] getCacheKey() {
    byte[] fieldNameBytes = StringUtils.toUtf8WithNullToEmpty(fieldName);
    return ByteBuffer.allocate(2 + fieldNameBytes.length)
        .put(CACHE_KEY_PREFIX)
        .put(fieldNameBytes)
        .array();
  }

  /**
   * Gets the intermediate type.
   *
   * @return the intermediate type
   */
  @Override
  public ColumnType getIntermediateType() {
    return ColumnType.FLOAT;
  }

  /**
   * Gets the result type.
   *
   * @return the result type
   */
  @Override
  public ColumnType getResultType() {
    return this.getIntermediateType();
  }

  /**
   * Gets the max intermediate size.
   *
   * @return the max intermediate size
   */
  @Override
  public int getMaxIntermediateSize() {
    return Doubles.BYTES;
  }

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ArrayCountAggregatorFactory that = (ArrayCountAggregatorFactory) o;
    return Objects.equals(name, that.name) && Objects.equals(fieldName, that.fieldName);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, fieldName);
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "ExampleSumAggregatorFactory{"
        + "name='"
        + name
        + '\''
        + ", fieldName='"
        + fieldName
        + '\''
        + '}';
  }
}
