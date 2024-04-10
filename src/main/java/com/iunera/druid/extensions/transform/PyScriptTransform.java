/*
 * Copyright 2023 Tim Frey This is a project for useful Druid extensions in the Fahrbar project. The
 * project is not to be distributed or for commercial use. It is in the current state for evaluation
 * purposes only. This software has no warranties and no special use rights are granted other than
 * evaluation.
 */

package com.iunera.druid.extensions.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.druid.segment.transform.RowFunction;
import org.apache.druid.segment.transform.Transform;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.python.core.PyCode;


/**
 * The Class PyScriptTransform.
 */
public class PyScriptTransform implements Transform {

  /** The Constant TYPE_NAME. */
  public static final String TYPE_NAME = "PyScriptTransform";

  /** The name. */
  // configurable parameters
  private final String name;

  /** The source columns. */
  private final List<String> sourceColumns;

  /** The code. */
  private final String code;

  /** The raw result. */
  private final boolean rawResult;

  /** The transform function. */
  private final String transformFunction;

  /** Additional transformation parameters. */
  private final List<String> addTransformFunctionArgs;

  /** The Constant defaulTransformFunctionName. */
  // default values
  private static final String defaulTransformFunctionName = "transform";

  /** The Constant defaultHome. */
  private static final String defaultHome = "classpath:/";

  /** The pyrowfunction. */
  // internal helper variables
  private final PyFunction pyrowfunction;

  /** The interpreter. */
  private final PythonInterpreter interpreter;

  /** The compiled pycode. */
  private final PyCode compiledPycode;

  /** The Constant simpleBase64Pattern. */
  private final static Pattern simpleBase64Pattern = Pattern
      .compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$");



  /**
   * Instantiates a new py script transform.
   *
   * @param name the name of the result column. Required
   * @param sourceColumns the source columns that are passed into the scripting function. Optional
   * @param code the code
   * @param transformFunction the name of the transform function in the code. Default: transform.
   *        Optional
   * @param home environment home of the runtime, e.g. pyhome. Default: classpath:/. Optional.
   * @param rawResult used that returned arrays with one element are not taken as normal object.
   *        Default: false. Optional.
   * @param addTransformFunctionArgs static arguments that are passed into each function call.
   *        Optional.
   */
  @JsonCreator
  public PyScriptTransform(@JsonProperty("name") String name,
      @JsonProperty("sourceColumns") @JsonFormat(
          with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) String[] sourceColumns,
      @JsonProperty("code") String code,
      @JsonProperty("transformFunction") String transformFunction,
      @JsonProperty("home") String home, @JsonProperty("rawResult") Boolean rawResult,
      @JsonProperty("addTransformFunctionArgs") @JsonFormat(
          with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) String[] addTransformFunctionArgs) {
    this.name = Preconditions.checkNotNull(name, "name");
    if (sourceColumns != null) {
      this.sourceColumns = Arrays.asList(sourceColumns);
    } else {
      this.sourceColumns = new ArrayList<>(0);
    }
    // TODO: change the defaults to Jackson annotations and check if that works with Druid
    if (rawResult != null) {
      this.rawResult = rawResult;
    } else {
      this.rawResult = false;
    }
    if (transformFunction == null)
      transformFunction = PyScriptTransform.defaulTransformFunctionName;
    this.transformFunction = transformFunction;
    if (home == null)
      home = PyScriptTransform.defaultHome;

    if (addTransformFunctionArgs != null) {
      this.addTransformFunctionArgs = Arrays.asList(addTransformFunctionArgs);
    } else {
      this.addTransformFunctionArgs = null;
    }

    Preconditions.checkNotNull(code, "code");
    Matcher isBase64 = PyScriptTransform.simpleBase64Pattern.matcher(code);
    if (isBase64.matches())
      this.code = new String(Base64.getDecoder().decode(code));
    else
      this.code = code;


    Properties props = new Properties();
    // props.put("python.home", home);
    // props.put("python.console.encoding", "UTF-8");
    // props.put("python.security.respectJavaAccessibility", "false");
    // props.put("python.import.site", "false");
    // props.put("python.path","/home/modules:scripts");
    Properties preprops = System.getProperties();

    PythonInterpreter.initialize(preprops, props, new String[0]);
    interpreter = new PythonInterpreter();
    compiledPycode = interpreter.compile(this.code);
    interpreter.exec(compiledPycode);
    pyrowfunction = (PyFunction) interpreter.get(this.transformFunction);


  }

  /**
   * Gets the name.
   *
   * @return the name of the resulting column
   */
  @JsonProperty
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the source columns.
   *
   * @return the source columns
   */
  @JsonProperty
  public List<String> getSourceColumns() {
    return sourceColumns;
  }

  /**
   * Gets the transform function.
   *
   * @return the transform function name
   */
  @JsonProperty
  public String getTransformFunction() {
    return transformFunction;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @JsonProperty
  public String getCode() {
    return code;
  }

  /**
   * Checks if is raw result.
   *
   * @return true, if is raw result
   */
  @JsonProperty
  public boolean isRawResult() {
    return rawResult;
  }

  /**
   * Gets the adds the transform function args.
   *
   * @return the adds the transform function args
   */
  @JsonProperty
  public List<String> getAddTransformFunctionArgs() {
    return this.addTransformFunctionArgs;
  }

  /**
   * Gets the row function.
   *
   * @return the row function
   */
  @Override
  public RowFunction getRowFunction() {
    return new PyRowFunction(this.name, this.sourceColumns, this.pyrowfunction, this.rawResult,
        this.addTransformFunctionArgs);
  }


  /**
   * Gets the required columns.
   *
   * @return the required columns
   */
  @Override
  public Set<String> getRequiredColumns() {
    Set<String> retsourcecolumns = new HashSet<>(this.sourceColumns);
    return retsourcecolumns;
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
    final PyScriptTransform that = (PyScriptTransform) o;
    return Objects.equals(name, that.name) && Objects.equals(pyrowfunction, that.pyrowfunction)
        && Objects.equals(this.sourceColumns, that.sourceColumns)
        && Objects.equals(this.rawResult, that.rawResult);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, pyrowfunction);
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "ExpressionTransform{" + "name='" + name + '\'' + ", expression='"
        + pyrowfunction.__name__ + '\'' + '}';
  }
}
