/*
 * Copyright 2023 Tim Frey
 * This is a project for useful Druid extensions in the Fahrbar project. The project is not to be
 * distributed or for commercial use. It is in the current state for evaluation purposes only. This
 * software has no warranties and no special use rights are granted other than evaluation.
 */

package com.iunera.druid.extensions.aggregator;

import java.util.List;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.segment.BaseFloatColumnValueSelector;
import org.apache.druid.segment.ColumnValueSelector;

public class ArrayCountAggregator implements Aggregator
{
  private final ColumnValueSelector<List<Long>>  selector;

  private List<Long> sum;

  public ArrayCountAggregator(ColumnValueSelector<List<Long>>  selector)
  {
    this.selector = selector;

    this.sum = null;
  }

  @Override
  public void aggregate()
  {
   sum= (List<Long>) selector.getObject();
 
  }

  @Override
  public Object get()
  {
    return sum;
  }

  @Override
  public float getFloat()
  {
    throw new UnsupportedOperationException("Not implemented");
    //return (float) sum;
  }

  @Override
  public long getLong()
  {
    throw new UnsupportedOperationException("Not implemented");
    //return (long) sum;
  }

  @Override
  public Aggregator clone()
  {
    return new ArrayCountAggregator(selector);
  }

  @Override
  public void close()
  {
    // no resources to cleanup
  }
}
