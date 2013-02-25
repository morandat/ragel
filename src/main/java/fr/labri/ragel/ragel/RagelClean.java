package fr.labri.ragel.ragel;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class RagelClean extends Ragel {

		public void execute() throws MojoExecutionException {
			for (File file : sourceDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(RAGEL_EXT);
				}
			}))
				clean(file);
		}

		void clean(File file) {
			File out;
			for(String[] lang : languages)
				if ((out = outputFile(file, lang[LANG_EXT])).exists()) {
					getLog().info("Unlink: " + out);
					out.delete();
				}
		}
}
