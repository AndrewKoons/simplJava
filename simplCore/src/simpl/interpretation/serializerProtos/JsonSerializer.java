package simpl.interpretation.serializerProtos;

import java.util.List;

import simpl.interpretation.ScalarInterpretation;
import simpl.interpretation.SimplInterpreter;

public class JsonSerializer {
	public String testInterps(Object obj)
	{
		/*
		SimplInterpreter si = new SimplInterpreter();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		int num = 0;
		List<ScalarInterpretation> lists = si.interpretInstance(obj);
		int constr = lists.size() - 1; 
		
		for(ScalarInterpretation scalar : si.interpretInstance(obj))
		{
			sb.append(scalar.fieldName);
			sb.append("=");
			sb.append("\"");
			sb.append(scalar.fieldValue);
			sb.append("\"");
			if(num < constr)
			{
				sb.append(",");
			}
			num++;
		}
		sb.append("}");
		return sb.toString();*/
		return ""; 
	}
}