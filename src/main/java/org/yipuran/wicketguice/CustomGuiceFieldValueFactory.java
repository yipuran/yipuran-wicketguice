package org.yipuran.wicketguice;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;

//import org.apache.wicket.util.lang.WicketObjects;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
/**
 * CustomGuiceFieldValueFactory. for JSR-330 and guice-Inject
 */
class CustomGuiceFieldValueFactory implements IFieldValueFactory, Serializable{
	private final boolean wrapInProxies;
	CustomGuiceFieldValueFactory(final boolean wrapInProxies){
		this.wrapInProxies = wrapInProxies;
	}
	@Override
	public Object getFieldValue(final Field field, final Object fieldOwner){
		Object target = null;
		if (supportsField(field)){
			if (!Modifier.isStatic(field.getModifiers())){
				com.google.inject.Inject injectAnnotation = field.getAnnotation(com.google.inject.Inject.class);
				if (injectAnnotation != null){
					try{
						Annotation bindingAnnotation = findBindingAnnotation(field.getAnnotations());
						final IProxyTargetLocator locator = new GuiceProxyTargetLocator(field,bindingAnnotation,injectAnnotation.optional());
						if (this.wrapInProxies){
							target = LazyInitProxyFactory.createProxy(field.getType(), locator);
						}else{
							target = locator.locateProxyTarget();
						}
						if (!field.isAccessible()){
							field.setAccessible(true);
						}
						field.set(fieldOwner, target);
					}catch(IllegalAccessException e){
						throw new WicketRuntimeException("Error Guice-injecting field "+field.getName()+" in "+fieldOwner, e);
					}catch (MoreThanOneBindingException e){
						throw new RuntimeException("Can't have more than one BindingAnnotation on field "+field.getName()+" of class "+fieldOwner.getClass().getName());
					}
				}else{
					javax.inject.Inject injectJsr330 = field.getAnnotation(javax.inject.Inject.class);
					if (injectJsr330 != null){
						try{
							Annotation bindingAnnotation = findBindingAnnotation(field.getAnnotations());
							final IProxyTargetLocator locator = new GuiceProxyTargetLocator(field,bindingAnnotation,false);
							if (this.wrapInProxies){
								target = LazyInitProxyFactory.createProxy(field.getType(), locator);
							}else{
								target = locator.locateProxyTarget();
							}
							if (!field.isAccessible()){
								field.setAccessible(true);
							}
							field.set(fieldOwner, target);
						}catch(IllegalAccessException e){
							throw new WicketRuntimeException("Error Guice-injecting field "+field.getName()+" in "+fieldOwner, e);
						}catch (MoreThanOneBindingException e){
							throw new RuntimeException("Can't have more than one BindingAnnotation on field "+field.getName()+" of class "+fieldOwner.getClass().getName());
						}
					}
				}
			}
		}
		return target;
	}
	@Override
	public boolean supportsField(final Field field){
		return field.isAnnotationPresent(com.google.inject.Inject.class) || field.isAnnotationPresent(javax.inject.Inject.class);
	}
	private Annotation findBindingAnnotation(final Annotation[] annotations)	throws MoreThanOneBindingException{
		Annotation bindingAnnotation = null;
		// Work out if we have a BindingAnnotation on this parameter.
		for (Annotation annotation : annotations){
			if (annotation.annotationType().getAnnotation(BindingAnnotation.class) != null){
				if (bindingAnnotation != null){
					throw new MoreThanOneBindingException();
				}
				bindingAnnotation = annotation;
			}else if(annotation.annotationType().equals(javax.inject.Named.class)){
				bindingAnnotation = annotation;
			}
		}
		return bindingAnnotation;
	}
	public class MoreThanOneBindingException extends Exception{
		private static final long serialVersionUID = 1L;
	}

	class GuiceProxyTargetLocator implements IProxyTargetLocator{
		private static final long serialVersionUID = 1L;
		private final Annotation bindingAnnotation;
		private final boolean optional;
		private final String className;
		private final String fieldName;
		public GuiceProxyTargetLocator(final Field field, final Annotation bindingAnnotation,final boolean optional){
			this.bindingAnnotation = bindingAnnotation;
			this.optional = optional;
			this.className = field.getDeclaringClass().getName();
			this.fieldName = field.getName();
		}
		@Override
		public Object locateProxyTarget(){
			final GuiceInjectorHolder holder = Application.get().getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
			final Type type;
			try{
				Class<?> clazz = WicketObjects.resolveClass(this.className);
				final Field field = clazz.getDeclaredField(this.fieldName);
				type = field.getGenericType();
			}catch(Exception e){
				throw new WicketRuntimeException("Error accessing member: "+this.fieldName+" of class: "+this.className, e);
			}
			// using TypeLiteral to retrieve the key gives us automatic support for
			// Providers and other injectable TypeLiterals
			final Key<?> key;
			if (this.bindingAnnotation==null){
				key = Key.get(TypeLiteral.get(type));
			}else{
				key = Key.get(TypeLiteral.get(type), this.bindingAnnotation);
			}
			Injector injector = holder.getInjector();
			// if the Inject annotation is marked optional and no binding is found
			// then skip this injection (WICKET-2241)
			if (this.optional){
				// Guice 2.0 throws a ConfigurationException if no binding is find while 1.0 simply
				// returns null.
				try{
					if (injector.getBinding(key)==null){
						return null;
					}
				}catch(RuntimeException e){
					return null;
				}
			}
			return injector.getInstance(key);
		}
	}
}
