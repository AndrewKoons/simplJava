package ecologylab.semantics.compiler;

import java.io.IOException;

import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.types.ScalarType;
import ecologylab.translators.java.JavaTranslator;

public class MetaMetadataJavaTranslator extends JavaTranslator
{
	
	public static final String	SCALAR_GETTER_SETTER_SUFFIX	= "Metadata";

	private static String[]			metaMetadataDefaultImports	= {
		MetaMetadataCompositeField.class.getName(),
	};

	public MetaMetadataJavaTranslator()
	{
		for (String importTarget : metaMetadataDefaultImports)
			this.addGlobalImportDependency(importTarget);
	}

	@Override
	protected void appendFieldAnnotationsHook(Appendable appendable, ClassDescriptor contextCd, FieldDescriptor fieldDesc, String spacing) throws IOException
	{
		super.appendFieldAnnotationsHook(appendable, contextCd, fieldDesc, spacing);
		
		MetadataFieldDescriptor fd = (MetadataFieldDescriptor) fieldDesc;
		MetaMetadataField f = fd.getDefiningMmdField();
		String annotations = f.getAdditionalAnnotationsInJava();
		appendable.append(spacing).append(annotations).append(SINGLE_LINE_BREAK);
	}

	@Override
	protected void appendConstructorHook(String className, Appendable appendable) throws IOException
	{
		appendable.append("\n");
		appendable.append("\tpublic ").append(className).append("(MetaMetadataCompositeField mmd) {\n");
		appendable.append("\t\tsuper(mmd);\n");
		appendable.append("\t}\n");
		appendable.append("\n");
	}
	
	protected void appendLazyEvaluation(String fieldName, String typeName, Appendable appendable) throws IOException
	{
		appendable.append("\n");
		
		// TODO write comments?
		
		// first line. Start of method name
		appendable.append("\tpublic ").append(typeName).append("\t").append(fieldName).append("()\n\t{\n");
	
		// second line. Declaration of result variable.
		appendable.append("\t\t").append(typeName).append("\t").append("result = this.").append(fieldName).append(";\n");
	
		// third line. Start of if statement
		appendable.append("\t\tif (result == null)\n\t\t{\n");
	
		// fourth line. creation of new result object.
		appendable.append("\t\t\tresult = new ").append(typeName).append("();\n");
	
		// fifth line. end of if statement
		appendable.append("\t\t\tthis.").append(fieldName).append(" = result;\n\t\t}\n");
	
		// sixth line. return statement and end of method.
		appendable.append("\t\treturn result;\n\t}\n");
	}

	@Override
	protected void appendGetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix)
			throws IOException
	{
		if (fieldDescriptor.getType() == FieldTypes.SCALAR)
		{
			appendValueGetter(fieldDescriptor, appendable);
			super.appendGetters(fieldDescriptor, appendable, SCALAR_GETTER_SETTER_SUFFIX);
		}
		else
		{
			super.appendGetters(fieldDescriptor, appendable, suffix);
		}
	}

	private void appendValueGetter(FieldDescriptor fieldDescriptor, Appendable appendable) throws IOException
	{
		MetaMetadataScalarField scalar = (MetaMetadataScalarField) ((MetadataFieldDescriptor) fieldDescriptor).getDefiningMmdField();
		
		// metadata scalar types!
		ScalarType<?> scalarType = fieldDescriptor.getScalarType();
		if (scalarType == null)
			throw new MetaMetadataException("scalar type not specified!");
		
		String fieldName = fieldDescriptor.getName();
		String capFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		String typeName = scalarType.getSimpleName();
		
		appendLazyEvaluation(fieldName, typeName, appendable);
		
		ScalarType operativeScalarType = scalarType.operativeScalarType();
		String javaPrimitiveTypeName = operativeScalarType.getSimpleName();
		appendable.append("\n");
		appendable.append("\tpublic ").append(javaPrimitiveTypeName).append(" get").append(capFieldName).append("()\n");
		appendable.append("\t{\n");
		appendable.append("\t\treturn this.").append(fieldName).append(" == null ? ").append(operativeScalarType.defaultValueString()).append(" : ").append(fieldName).append("().getValue();\n");
		appendable.append("\t}\n");
	}

	@Override
	protected void appendSetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix)
			throws IOException
	{
		if (fieldDescriptor.getType() == FieldTypes.SCALAR)
		{
			appendValueSetter(fieldDescriptor, appendable);
			super.appendSetters(fieldDescriptor, appendable, SCALAR_GETTER_SETTER_SUFFIX);
		}
		else
		{
			super.appendSetters(fieldDescriptor, appendable, suffix);
		}
	}

	private void appendValueSetter(FieldDescriptor fieldDescriptor, Appendable appendable) throws IOException
	{
		// metadata scalar types!
		ScalarType<?> scalarType = fieldDescriptor.getScalarType();
		if (scalarType == null)
			throw new MetaMetadataException("scalar type not specified!");
		
		String fieldName = fieldDescriptor.getName();
		String capFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		
		ScalarType operativeScalarType = scalarType.operativeScalarType();
		String javaPrimitiveTypeName = operativeScalarType.getSimpleName();
		appendable.append("\n");
		appendable.append("\tpublic void set").append(capFieldName).append("(").append(javaPrimitiveTypeName).append(" ").append(fieldName).append(")\n");
		appendable.append("\t{\n");
		appendable.append("\t\tif (").append(fieldName).append(" != ").append(operativeScalarType.defaultValueString()).append(")\n");
		appendable.append("\t\t\tthis.").append(fieldName).append("().setValue(").append(fieldName).append(");\n");
		appendable.append("\t}\n");
	}
	
}
