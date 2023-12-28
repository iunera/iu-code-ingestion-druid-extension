/*
 * Copyright 2023 Tim Frey This is a project for useful Druid extensions in the Fahrbar project. The
 * project is not to be distributed or for commercial use. It is in the current state for evaluation
 * purposes only. This software has no warranties and no special use rights are granted other than
 * evaluation.
 */
package com.iunera.druid.extensions.transform;

import java.util.ArrayList;
import java.util.List;
import org.apache.druid.data.input.Row;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.segment.transform.RowFunction;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

class PyRowFunction implements RowFunction {
  private final String name;
  private final PyFunction pyrowfunction;
  private final List<String> source;
  private final int sourceArgsSize;
  private final boolean returnraw;
  private final List<String> addTransformFunctionArgs;
  private final int addTransformFunctionArgsSize;

  PyRowFunction(final String name, final List<String> source, PyFunction pyrowfunction,
      boolean returnRawValues, List<String> addTransformFunctionArgs) {
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

  // metric transform
  @Override
  public Object eval(final Row row) {
    try {
      List<String> transformedlist = evalDimension(row);
      if (transformedlist.size() == 0)
        return null;
      if (transformedlist.size() == 1 && this.returnraw == false)
        return transformedlist.get(0);

      return transformedlist;
    } catch (Throwable t) {
      throw new ISE(t, "Could not transform value for %s reason: %s", name, t.getStackTrace());
    }
  }

  @Override
  public List<String> evalDimension(Row row) {
    try {
      int allargssize=sourceArgsSize + addTransformFunctionArgsSize;
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
      throw new ISE(t, "Could not transform dimension value for %s reason: %s", name,
          t.getStackTrace().toString());
    }
  }
}
