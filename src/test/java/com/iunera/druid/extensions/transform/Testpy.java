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
import java.util.Properties;
import org.python.core.PyArray;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/** The Class Testpy. */
public class Testpy {

  /** The defpycode. */
  static String defpycode =
      "def returnvalue(str) :\n"
          + "    if str == \"hi\" :\n"
          + "        return [1,2]\n"
          + "    else :\n"
          + "        return [2]";

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
