package fr.labri.ragel.ragel;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class RagelCompile extends Ragel {
	@Parameter(property = "project", required = true, readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "ragel", property = "ragelCommand", required = true)
	private String ragelCommand;

	@Parameter(defaultValue = "java", property = "targetLanguage", required = true)
	private String targetLanguage;

	@Parameter
	private boolean force;
	
	@Parameter(property = "args", required = false)
	private String[] args;
	
	private RagelXFactory rxFactory = getRagelX();
	
	public void execute() throws MojoExecutionException {
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		project.addCompileSourceRoot(outputDirectory.getPath());

		String lang[] = selectLanguage(targetLanguage);

		recursiveCallRagel(sourceDirectory, lang);
	}

	private void recursiveCallRagel(final File dir, final String[] lang) {
		for (File file : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					String f = pathname.toString();
					return f.endsWith(RAGEL_EXT) || f.endsWith(RAGELX_EXT);
				}
				if (pathname.isDirectory())
					recursiveCallRagel(new File(dir, pathname.getName()), lang);
				return false;
			}
		})) {
			String fname = file.toString();
			if (fname.endsWith(RAGELX_EXT)) {
				callRagelX(file, lang);
			} else
				callRagel(file, file, lang);
		}
	}

	private void callRagelX(File file, final String[] lang) {
		File tmp = null;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			tmp = File.createTempFile("rglx", RAGEL_EXT);

			fin = new FileInputStream(file);
			fout = new FileOutputStream(tmp);
			List<String> rxargs = new ArrayList<>(Arrays.asList(args));
			rxargs.add("-I"+file.getParent());
			rxargs.add("-D__PACKAGE__="+packagePath(file));
			rxFactory.compile(basename(file), lang[LANG_NAME],  fin, fout, rxargs);
			fout.close();
			callRagel(file, tmp, lang);
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		} finally {
//			if (tmp != null)
//				tmp.delete();
			if (fin != null)
				try {
					fin.close();
				} catch (IOException e) {
				}
			if (fout != null)
				try {
					fout.close();
				} catch (IOException e) {
				}
		}
	}

	private void callRagel(File gramar, File input, String[] lang) {
		File outFile = outputFile(gramar, lang[LANG_EXT]);
		if (force || !outFile.exists()
				|| outFile.lastModified() < gramar.lastModified())
			try {
				String args[] = new String[] { ragelCommand,
						lang[LANG_COMMAND], "-o", outFile.toString(),
						input.toString() };
				File outPath = outFile.getParentFile();
				if (!outPath.exists())
					outPath.mkdirs();
				Process proc = Runtime.getRuntime().exec(args);
				if (proc.waitFor() != 0)
					getLog().error(
							"Unable to compile [" + proc.exitValue() + "]: "
									+ gramar + ( gramar == input ? "" : " ("+input+")"));
			} catch (IOException | InterruptedException e) {
				getLog().error("Unable to compile: " + gramar+ ( gramar == input ? "" : " ("+input+")"), e);
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
	
	interface RagelXFactory {
		void compile(String machine, String language, InputStream in, OutputStream out, Collection<String> opts) throws Exception;
	}
	
	RagelXFactory getRagelX() {
		if(rxFactory != null)
			return rxFactory;
		try {
			final Class<?> clazz = Class.forName(RAGELX_CLASS_NAME);
			final Constructor<?> ctor = clazz.getConstructor(String.class, String.class, String.class);
			final Method method = clazz.getMethod(RAGELX_COMPILE_METHOD, InputStream.class, OutputStream.class, Collection.class);

			return new RagelXFactory() {
				@Override
				public void compile(String machine, String language, InputStream in, OutputStream out, Collection<String> opts) throws Exception {
					getLog().info(String.format("Calling RagelX/%s for '%s' (%s)", language, machine, opts));
					Object ragelx = ctor.newInstance(machine, language, null);
					method.invoke(ragelx, in, out, opts);
				}
			};
		} catch (final Exception e) {
			return new RagelXFactory() {
				@Override
				public void compile(String machine, String language, InputStream in, OutputStream out, Collection<String> opts) throws Exception {
					throw new Exception("RagelX is not available ", e);
				}
			};
		}
	}
}
