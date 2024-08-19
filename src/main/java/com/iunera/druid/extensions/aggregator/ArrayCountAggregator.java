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

import java.util.List;
import org.apache.druid.query.aggregation.Aggregator;
import org.apache.druid.segment.ColumnValueSelector;

/** The Class ArrayCountAggregator. */
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

  /** Aggregate. */
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

  /** Close. */
  @Override
  public void close() {
    // no resources to cleanup
  }
}
