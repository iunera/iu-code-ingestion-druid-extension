/*
 * Copyright 2023 Tim Frey
 * This is a project for useful Druid extensions in the Fahrbar project. The project is not to be
 * distributed or for commercial use. It is in the current state for evaluation purposes only. This
 * software has no warranties and no special use rights are granted other than evaluation.
 */

package com.iunera.druid.extensions;

import java.util.List;
import org.apache.druid.initialization.DruidModule;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.iunera.druid.extensions.aggregator.ArrayCountAggregatorFactory;
import com.iunera.druid.extensions.transform.PyScriptTransform;
import com.iunera.druid.extensions.transform.SimpleJavaTransform;


public class IuneraExtensionModule implements DruidModule {
  @Override
  public List<? extends Module> getJacksonModules() {
    return ImmutableList.of(new SimpleModule(getClass().getSimpleName()).registerSubtypes(
        new NamedType(ArrayCountAggregatorFactory.class, ArrayCountAggregatorFactory.TYPE_NAME),
        new NamedType(PyScriptTransform.class, PyScriptTransform.TYPE_NAME),
        new NamedType(SimpleJavaTransform.class, SimpleJavaTransform.TYPE_NAME)));
  }

  @Override
  public void configure(Binder binder) {}
}
