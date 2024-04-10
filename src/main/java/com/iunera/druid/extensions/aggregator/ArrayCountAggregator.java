/*
 * Copyright 2023 Tim Frey This is a project for useful Druid extensions in the Fahrbar project. The
 * project is not to be distributed or for commercial use. It is in the current state for evaluation
 * purposes only. This software has no warranties and no special use rights are granted other than
 * evaluation.
 */

package com.iunera.druid.extensions.aggregator;

import java.util.List;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.segment.BaseFloatColumnValueSelector;
import org.apache.druid.segment.ColumnValueSelector;


/**
 * The Class ArrayCountAggregator.
 */
public class ArrayCountAggregator implements Aggregator {

  /** The selector. */
  private final ColumnValueSelector<List<Long>> selector;

  /** The sum. */
  private List<Long> sum;

  /**
   * Instantiates a new array count aggregator.
   *
   * @param selector the selector
   */
  public ArrayCountAggregator(ColumnValueSelector<List<Long>> selector) {
    this.selector = selector;

    this.sum = null;
  }

  /**
   * Aggregate.
   */
  @Override
  public void aggregate() {
    sum = (List<Long>) selector.getObject();

  }

  /**
   * Gets the.
   *
   * @return the object
   */
  @Override
  public Object get() {
    return sum;
  }

  /**
   * Gets the float.
   *
   * @return the float
   */
  @Override
  public float getFloat() {
    throw new UnsupportedOperationException("Not implemented");
    // return (float) sum;
  }

  /**
   * Gets the long.
   *
   * @return the long
   */
  @Override
  public long getLong() {
    throw new UnsupportedOperationException("Not implemented");
    // return (long) sum;
  }

  /**
   * Clone.
   *
   * @return the aggregator
   */
  @Override
  public Aggregator clone() {
    return new ArrayCountAggregator(selector);
  }

  /**
   * Close.
   */
  @Override
  public void close() {
    // no resources to cleanup
  }
}
