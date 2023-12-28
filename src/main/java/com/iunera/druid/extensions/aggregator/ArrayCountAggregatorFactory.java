/*
 * Copyright 2023 Tim Frey
 * This is a project for useful Druid extensions in the Fahrbar project. The project is not to be
 * distributed or for commercial use. It is in the current state for evaluation purposes only. This
 * software has no warranties and no special use rights are granted other than evaluation.
 */
package com.iunera.druid.extensions.aggregator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.query.aggregation.AggregatorFactory;
import org.apache.druid.query.aggregation.AggregatorFactoryNotMergeableException;
import org.apache.druid.query.aggregation.BufferAggregator;
import org.apache.druid.segment.ColumnSelectorFactory;
import org.apache.druid.segment.column.ColumnType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ArrayCountAggregatorFactory extends AggregatorFactory
{
  // Built-in aggregators use 1-byte codes starting with 0x00 for cache keys.
  // Extension aggregators can use 0xFF + our own site-specific byte.
  private static final byte[] CACHE_KEY_PREFIX = new byte[]{(byte) 0xFF, (byte) 0x00};

  // Type name for JSON.
  public static final String TYPE_NAME = "exampleSum";

  private static final Comparator COMPARATOR = new Ordering()
  {
    @Override
    public int compare(Object o, Object o1)
    {
      return 1;
    }
  }.nullsFirst();

 

  private final String name;
  private final String fieldName;

  @JsonCreator
  public ArrayCountAggregatorFactory(
      @JsonProperty("name") final String name,
      @JsonProperty("fieldName") final String fieldName
  )
  {
    this.name = Preconditions.checkNotNull(name, "name");
    this.fieldName = Preconditions.checkNotNull(fieldName, "fieldName");
  }

  @Override
  public Aggregator factorize(ColumnSelectorFactory metricFactory)
  {
    return new ArrayCountAggregator(metricFactory.makeColumnValueSelector(fieldName));
  }

  @Override
  public BufferAggregator factorizeBuffered(ColumnSelectorFactory metricFactory)
  {
    return new ArrayCountBufferAggregator(metricFactory.makeColumnValueSelector(fieldName));
  }

  @Override
  public Comparator getComparator()
  {
    return COMPARATOR;
  }

  @Override
  public Object combine(Object lhs, Object rhs)
  {
    List<Long> lhsl=(List<Long> )lhs;
    List<Long> lrhs=(List<Long> )rhs;
    List<Long> ret= new ArrayList<>(lhsl.size());
    for (int i=0; i<lhsl.size();i++) {
      ret.add(lhsl.get(i)+lrhs.get(i));
    }
    return ret;
  }

  @Override
  public AggregatorFactory getCombiningFactory()
  {
    return new ArrayCountAggregatorFactory(name, name);
  }

  @Override
  public AggregatorFactory getMergingFactory(AggregatorFactory other) throws AggregatorFactoryNotMergeableException
  {
    if (other.getName().equals(this.getName()) && this.getClass() == other.getClass()) {
      return getCombiningFactory();
    } else {
      throw new AggregatorFactoryNotMergeableException(this, other);
    }
  }

  @Override
  public List<AggregatorFactory> getRequiredColumns()
  {
    return ImmutableList.of(new ArrayCountAggregatorFactory(fieldName, fieldName));
  }

  @Override
  public Object deserialize(Object object)
  {
   
    return object;
  }

  @Override
  public Object finalizeComputation(Object object)
  {
    return object;
  }

  @JsonProperty
  public String getFieldName()
  {
    return fieldName;
  }

  @Override
  @JsonProperty
  public String getName()
  {
    return name;
  }

  @Override
  public List<String> requiredFields()
  {
    return ImmutableList.of(fieldName);
  }

  @Override
  public byte[] getCacheKey()
  {
    byte[] fieldNameBytes = StringUtils.toUtf8WithNullToEmpty(fieldName);
    return ByteBuffer.allocate(2 + fieldNameBytes.length)
                     .put(CACHE_KEY_PREFIX)
                     .put(fieldNameBytes)
                     .array();
  }

  @Override
  public ColumnType getIntermediateType()
  {
    return ColumnType.FLOAT;
  }

  @Override
  public ColumnType getResultType() {
    return this.getIntermediateType();
  }

  @Override
  public int getMaxIntermediateSize()
  {
    return Doubles.BYTES;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ArrayCountAggregatorFactory that = (ArrayCountAggregatorFactory) o;
    return Objects.equals(name, that.name) &&
           Objects.equals(fieldName, that.fieldName);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(name, fieldName);
  }

  @Override
  public String toString()
  {
    return "ExampleSumAggregatorFactory{" +
           "name='" + name + '\'' +
           ", fieldName='" + fieldName + '\'' +
           '}';
  }
}
