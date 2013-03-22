package fr.labri.ragel.ragel;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class Ragel extends AbstractMojo {
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/ragel", property = "outputDir", required = true)
	protected File outputDirectory;

	@Parameter(defaultValue = "${basedir}/src/main/ragel", property = "sourceDir", required = true)
	protected File sourceDirectory;

	public final static String RAGEL_EXT = ".rl";
	public final static String RAGELX_EXT = ".rlx";

	public final static int LANG_NAME = 0;
	public final static int LANG_COMMAND = 1;
	public final static int LANG_EXT = 2;

	public static final String languages[][] = { { "java", "-J", "java" } };

	protected String basename(File fname) {
		String asString = fname.getName().toString();
		int pos = asString.lastIndexOf('.');

		if (pos != -1)
			asString = asString.substring(0, pos);
		return asString;
	}
	protected File outputFile(File gramar, String ext) {
		File targetDir = new File(outputDirectory, gramar.getParent().substring(
				sourceDirectory.toString().length()));
		
		return new File(targetDir, new StringBuilder(basename(gramar)).append('.')
				.append(ext).toString());
	}
}
