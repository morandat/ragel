package fr.labri.ragel.ragel;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

@Mojo(name = "ragel", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class RagelCompile extends Ragel {
	@Parameter(defaultValue = "ragel", property = "ragel", required = true)
	private String ragelCommand;

	@Parameter(defaultValue = "java", property = "targetLanguage", required = true)
	private String targetLanguage;

	@Parameter
	private boolean force;
	
	public void execute() throws MojoExecutionException {
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		String lang[] = selectLanguage(targetLanguage);

		for (File file : sourceDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(RAGEL_EXT);
			}
		}))
			callRagel(file, lang);
	}

	private void callRagel(File gramar, String[] lang) {
		File outFile = outputFile(gramar, lang[LANG_EXT], outputDirectory);
		if (force || !outFile.exists() || outFile.lastModified() < gramar.lastModified())
			try {
				String args[] = new String[] { ragelCommand,
						lang[LANG_COMMAND], "-o", outFile.toString(),
						gramar.toString() };
				getLog().info(Arrays.toString(args));
				Process proc = Runtime.getRuntime().exec(args);
				if (proc.waitFor() != 0)
					getLog().error("Unable to compile [" + proc.exitValue() + "]: " + gramar);
			} catch (IOException | InterruptedException e) {
				getLog().error("Unable to compile: " + gramar, e);
			}
		else
			getLog().info("Skipping grammar: "+gramar);
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
