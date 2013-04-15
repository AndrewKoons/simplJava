package simpl.descriptions;

import simpl.annotations.dbal.simpl_scalar;

public class ParameterDescriptorImpl implements ParameterDescriptor {	
	@simpl_scalar
	String name;
	Class<?> type;
	@simpl_scalar
	Object value;
	
	public ParameterDescriptorImpl(String paramName,Class<?> paramType, Object value)
	{
		this.name = paramName;
		this.type = paramType;
		this.value = value;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}
	
}