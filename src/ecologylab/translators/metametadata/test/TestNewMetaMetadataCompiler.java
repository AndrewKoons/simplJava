package ecologylab.translators.metametadata.test;

import java.io.File;
import java.io.IOException;

import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.translators.java.JavaTranslationException;
import ecologylab.translators.java.JavaTranslator;

public class TestNewMetaMetadataCompiler
{
	
	private static void doTest(File testingRepository, File outputDir) throws IOException, SIMPLTranslationException, JavaTranslationException
	{
		MetaMetadataRepository repository = MetaMetadataRepository.readRepository(testingRepository);
		TranslationScope tscope = repository.traverseAndGenerateTranslationScope();
		TranslationScope.setGraphSwitch();
		JavaTranslator jt = new JavaTranslator();
		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
		for (ClassDescriptor cd : metadataBuiltInTScope.getClassDescriptors())
			jt.excludeClassFromTranslation(cd);
		jt.translateToJava(outputDir, tscope);
	}
	
	public static void testGeneratingBasicTScope() throws IOException, SIMPLTranslationException, JavaTranslationException
	{
		doTest(new File("data/testRepository/testGeneratingBasicTScope.xml"), new File("c:/tmp/testbasictscope/"));
	}
	
	public static void testTypeGraphs() throws IOException, SIMPLTranslationException, JavaTranslationException
	{
		doTest(new File("data/testRepository/testTypeGraphs.xml"), new File("c:/tmp/testtypegraphs/"));
	}

	public static void testInlineMmd() throws IOException, SIMPLTranslationException, JavaTranslationException
	{
		doTest(new File("data/testRepository/testInlineMmd.xml"), new File("c:/tmp/testinlinemmd/"));
	}

	public static void main(String[] args) throws IOException, SIMPLTranslationException, JavaTranslationException
	{
//		testGeneratingBasicTScope();
//		testTypeGraphs();
		testInlineMmd();
	}
	
}
