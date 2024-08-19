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

import java.util.ArrayList;
import java.util.List;
import org.apache.druid.data.input.Row;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.segment.transform.RowFunction;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

/** The Class PyRowFunction. */
class PyRowFunction implements RowFunction {

  /** The name. */
  private final String name;

  /** The pyrowfunction. */
  private final PyFunction pyrowfunction;

  /** The source. */
  private final List<String> source;

  /** The source args size. */
  private final int sourceArgsSize;

  /** The returnraw. */
  private final boolean returnraw;

  /** The add transform function args. */
  private final List<String> addTransformFunctionArgs;

  /** The add transform function args size. */
  private final int addTransformFunctionArgsSize;

  /**
   * Instantiates a new py row function.
   *
   * @param name the name
   * @param source the source
   * @param pyrowfunction the pyrowfunction
   * @param returnRawValues the return raw values
   * @param addTransformFunctionArgs the add transform function args
   */
  PyRowFunction(
      final String name,
      final List<String> source,
      PyFunction pyrowfunction,
      boolean returnRawValues,
      List<String> addTransformFunctionArgs) {
    this.name = name;
    this.pyrowfunction = pyrowfunction;
    this.source = source;
    this.returnraw = returnRawValues;
    this.addTransformFunctionArgs = addTransformFunctionArgs;

    // just that it is not computed each row new
    if (this.source == null) {
      sourceArgsSize = 0;
    } else {
      sourceArgsSize = source.size();
    }
    if (this.addTransformFunctionArgs == null) {
      addTransformFunctionArgsSize = 0;
    } else {
      addTransformFunctionArgsSize = addTransformFunctionArgs.size();
    }
  }

  /**
   * Eval.
   *
   * @param row the row
   * @return the object
   */
  // metric transform
  @Override
  public Object eval(final Row row) {
    try {
      List<String> transformedlist = evalDimension(row);
      if (transformedlist.size() == 0) return null;
      if (transformedlist.size() == 1 && this.returnraw == false) return transformedlist.get(0);

      return transformedlist;
    } catch (Throwable t) {
      throw new ISE(t, "Could not transform value for %s reason: %s", name, t.getStackTrace());
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
      int allargssize = sourceArgsSize + addTransformFunctionArgsSize;
      PyObject[] args = new PyObject[allargssize];
      int i = 0;
      for (; i < sourceArgsSize; i++) {
        args[i] = new PyString(row.getRaw(this.source.get(i)).toString());
      }
      for (int j = 0; i < allargssize; i++, j++) {
        args[i] = new PyString(addTransformFunctionArgs.get(j));
      }
      PyObject transformed = this.pyrowfunction.__call__(args);
      List<String> retlist = new ArrayList<>(1);
      if (this.returnraw) {
        retlist.add(transformed.toString());
        return retlist;
      }
      // used to preserve multi-value dimensions
      if (transformed instanceof PyList) {
        PyList ret = (PyList) transformed;
        retlist = new ArrayList<>(ret.size());
        for (Object o : ret) {
          retlist.add(o.toString());
        }
      } else {
        retlist.add(transformed.toString());
      }
      return retlist;
    } catch (Throwable t) {
      throw new ISE(
          t,
          "Could not transform dimension value for %s reason: %s",
          name,
          t.getStackTrace().toString());
    }
  }
}
