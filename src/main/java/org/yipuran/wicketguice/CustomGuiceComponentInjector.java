/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yipuran.wicketguice;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.wicket.injection.IFieldValueFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
/**
 * CustomGuiceComponentInjector.  for JSR-330 and guice-Inject
 * Injects field members of components using Guice.
 */
public class CustomGuiceComponentInjector extends org.apache.wicket.injection.Injector implements IComponentInstantiationListener{
	private final IFieldValueFactory fieldValueFactory;
	/**
	 * Creates a new Wicket CustomGuiceComponentInjector instance.
	 */
	public CustomGuiceComponentInjector(final Application app){
		this(app, new Module[0]);
	}
	/**
	 * Creates a new Wicket CustomGuiceComponentInjector instance, using the supplied Guice {@link Module}
	 * instances to create a new Guice {@link Injector} instance internally.
	 */
	public CustomGuiceComponentInjector(final Application app, final Module... modules){
		this(app, Guice.createInjector(app.usesDeploymentConfig() ? Stage.PRODUCTION : Stage.DEVELOPMENT, modules), true);
	}
	/**
	 * Constructor
	 */
	public CustomGuiceComponentInjector(final Application app, final Injector injector){
		this(app, injector, true);
	}
	/**
	 * Creates a new Wicket CustomGuiceComponentInjector instance, using the provided Guice
	 * {@link Injector} instance.
	 */
	public CustomGuiceComponentInjector(final Application app, final Injector injector,final boolean wrapInProxies){
		app.setMetaData(GuiceInjectorHolder.INJECTOR_KEY, new GuiceInjectorHolder(injector));
		this.fieldValueFactory = new CustomGuiceFieldValueFactory(wrapInProxies);
		bind(app);
	}
	@Override
	public void inject(final Object object){
		inject(object,this.fieldValueFactory);
	}
	@Override
	public void onInstantiation(final Component component){
		inject(component);
	}
}
