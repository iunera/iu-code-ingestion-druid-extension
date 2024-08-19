package com.iunera.druid.extensions;

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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.iunera.druid.extensions.aggregator.ArrayCountAggregatorFactory;
import com.iunera.druid.extensions.transform.PyScriptTransform;
import com.iunera.druid.extensions.transform.SimpleJavaTransform;
import java.util.List;
import org.apache.druid.initialization.DruidModule;

/** The Class IuneraExtensionModule. */
public class IuneraExtensionModule implements DruidModule {

  /**
   * Gets the jackson modules.
   *
   * @return the jackson modules
   */
  @Override
  public List<? extends Module> getJacksonModules() {
    return ImmutableList.of(
        new SimpleModule(getClass().getSimpleName())
            .registerSubtypes(
                new NamedType(
                    ArrayCountAggregatorFactory.class, ArrayCountAggregatorFactory.TYPE_NAME),
                new NamedType(PyScriptTransform.class, PyScriptTransform.TYPE_NAME),
                new NamedType(SimpleJavaTransform.class, SimpleJavaTransform.TYPE_NAME)));
  }

  /**
   * Configure.
   *
   * @param binder the binder
   */
  @Override
  public void configure(Binder binder) {}
}
