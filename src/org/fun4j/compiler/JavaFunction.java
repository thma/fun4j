package org.fun4j.compiler;

public class JavaFunction extends BaseFunction {
	
	Class<?> clazz;
	
	String functionName;
	
	boolean isStatic;
 
	public JavaFunction(String name, Class<?> c, boolean isStatic) {
		clazz = c;
		functionName = name;
		this.isStatic = isStatic;
	}
	
	

	@Override
	public Object apply(Object... args) {
		if (isStatic) {
			
		}
		// TODO Auto-generated method stub
		return null;
	}

}
