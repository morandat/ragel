package fr.labri.ragel.ragel;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class RagelCompile extends Ragel {
	@Parameter(defaultValue = "ragel", property = "ragelCommand", required = true)
	private String ragelCommand;

	@Parameter(defaultValue = "java", property = "targetLanguage", required = true)
	private String targetLanguage;

	@Parameter
	private boolean force;

	public void execute() throws MojoExecutionException {
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		String lang[] = selectLanguage(targetLanguage);

		recursiveCallRagel(sourceDirectory, lang);
	}

	private void recursiveCallRagel(final File dir, final String[] lang) {
		for (File file : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile())
					return pathname.toString().endsWith(RAGEL_EXT);
				if (pathname.isDirectory()) 
					recursiveCallRagel(new File(dir, pathname.getName()), lang);
				return false;
			}
		}))
			callRagel(file, lang);
	}

	private void callRagel(File gramar, String[] lang) {
		File outFile = outputFile(gramar, lang[LANG_EXT]);
		if (force || !outFile.exists()
				|| outFile.lastModified() < gramar.lastModified())
			try {
				String args[] = new String[] { ragelCommand,
						lang[LANG_COMMAND], "-o", outFile.toString(),
						gramar.toString() };
				File outPath = outFile.getParentFile(); 
				if (!outPath.exists())
					outPath.mkdirs();

				getLog().info(Arrays.toString(args));
				Process proc = Runtime.getRuntime().exec(args);
				if (proc.waitFor() != 0)
					getLog().error(
							"Unable to compile [" + proc.exitValue() + "]: "
									+ gramar);
			} catch (IOException | InterruptedException e) {
				getLog().error("Unable to compile: " + gramar, e);
			}
		else
			getLog().info("Skipping grammar: " + gramar);
	}

	private String[] selectLanguage(String lang) {
		for (String[] candidate : languages) {
			if (candidate[LANG_NAME].equalsIgnoreCase(lang))
				return candidate;
		}

		getLog().warn("No such language: " + lang);
		return languages[0];
	}
}
