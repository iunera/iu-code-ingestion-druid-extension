package com.iunera.druid.extensions.transform;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.druid.data.input.Row;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.segment.transform.RowFunction;
import org.apache.druid.segment.transform.Transform;

/** The Class SimpleJavaTransform. */
public class SimpleJavaTransform implements Transform {

  /** The Constant TYPE_NAME. */
  public static final String TYPE_NAME = "SimpleJavaTransform";

  /** The name. */
  private final String name;

  /** The source. */
  private final String source;

  /**
   * Instantiates a new simple java transform.
   *
   * @param name the name
   * @param source the source
   */
  @JsonCreator
  public SimpleJavaTransform(
      @JsonProperty("name") final String name, @JsonProperty("source") final String source) {
    this.name = Preconditions.checkNotNull(name, "name");
    this.source = Preconditions.checkNotNull(source, "source");
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @JsonProperty
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the row function.
   *
   * @return the row function
   */
  @Override
  public RowFunction getRowFunction() {
    return new JavaRowFunction(this.name, this.source);
  }

  /**
   * Gets the required columns.
   *
   * @return the required columns
   */
  @Override
  public Set<String> getRequiredColumns() {
    Set<String> sourceColumns = new HashSet<>();
    sourceColumns.add(this.source);
    return sourceColumns;
  }

  /** The Class JavaRowFunction. */
  static class JavaRowFunction implements RowFunction {

    /** The name. */
    private final String name;

    /** The source column. */
    private final String sourceColumn;

    /**
     * Instantiates a new java row function.
     *
     * @param name the name
     * @param sourceColumn the source column
     */
    JavaRowFunction(final String name, final String sourceColumn) {
      this.name = name;
      this.sourceColumn = sourceColumn;
    }

    /**
     * Eval.
     *
     * @param row the row
     * @return the object
     */
    // for metrics
    @Override
    public Object eval(final Row row) {
      try {
        return row.getRaw(this.sourceColumn) + "testrow Object eval(final Row row)";
      } catch (Throwable t) {
        throw new ISE(t, "Could not transform value for %s reason: %s", name, t.getMessage());
      }
    }

    /**
     * Eval dimension.
     *
     * @param row the row
     * @return the list
     */
    @Override
    public List<String> evalDimension(Row row) {
      try {
        List<String> result = new ArrayList<>();
        result.add(row.toString() + " List<String> evalDimension(Row row)");
        return result;
      } catch (Throwable t) {
        throw new ISE(
            t, "Could not transform dimension value for %s reason: %s", name, t.getMessage());
      }
    }
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

    return true;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "JavaRowFunction";
  }
}
