package com.hexing.libhexbase.application;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by long
 * on 2017/12/22.
 */

public class ReflectUtil {
	static Object getMember(Class clazz, Object target, String memberName){
		try {
			Field field = clazz.getDeclaredField(memberName);
			if(!Modifier.isPublic(field.getModifiers())){
				field.setAccessible(true);
			}
			return field.get(target);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	static void setMember(Class clazz, Object target, String memberName,Object newValue){
		try {
			Field field = clazz.getDeclaredField(memberName);
			if(!Modifier.isPublic(field.getModifiers())){
				field.setAccessible(true);
			}
			field.set(target,newValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	static Object invoke(Class clazz, Object target, String methodName, Class[] parameterTypes, Object[] parameters){
		try {
			Method method = clazz.getDeclaredMethod(methodName,parameterTypes);
			if(!Modifier.isPublic(method.getModifiers())){
				method.setAccessible(true);
			}
			return method.invoke(target, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
