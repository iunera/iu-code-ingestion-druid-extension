/*
 * Copyright 2023 Tim Frey This is a project for useful Druid extensions in the Fahrbar project. The
 * project is not to be distributed or for commercial use. It is in the current state for evaluation
 * purposes only. This software has no warranties and no special use rights are granted other than
 * evaluation.
 */
package com.iunera.druid.extensions.transform;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.python.core.PyArray;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;


/**
 * The Class Testpy.
 */
public class Testpy {

  /** The defpycode. */
  static String defpycode = "def returnvalue(str) :\n" + "    if str == \"hi\" :\n"
      + "        return [1,2]\n" + "    else :\n" + "        return [2]";

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

    Properties props = new Properties();
    props.put("python.home", "classpath:/");
    props.put("python.console.encoding", "UTF-8");
    props.put("python.security.respectJavaAccessibility", "false");
    props.put("python.import.site", "false");
    Properties preprops = System.getProperties();

    PythonInterpreter.initialize(preprops, props, new String[0]);
    PythonInterpreter interp = new PythonInterpreter();
    org.python.core.PyCode code = interp.compile(defpycode);
    interp.exec(code);
    PyFunction pf = (PyFunction) interp.get("returnvalue");
    PyObject transformed = pf.__call__(new PyString("fdfdf"));


    List<Object> retlist = new ArrayList<>(1);
    if (transformed instanceof PyList) {
      PyList ret = (PyList) transformed;
      retlist = new ArrayList<>(ret.size());
      for (Object o : ret) {
        System.out.println(o.getClass());
        retlist.add(o.toString());
      }
    } else if (transformed instanceof PyArray) {
      PyList ret = (PyList) (((PyArray) transformed).tolist());
      retlist = new ArrayList<>(ret.__len__());
      for (Object o : ret) {
        retlist.add(((PyString) o).getString());
      }
    }

    // code.invoke("returnvalue");
    // PyObject ret=code.invoke("returnvalue()");

    System.out.println(transformed.toString());
  }

}
