package com.garganttua.server.bundles.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.garganttua.server.core.deployment.artefacts.GGServerApplicationConfiguration;
import com.garganttua.server.core.execution.GGServerApplicationEngine;
import com.garganttua.server.core.update.GGServerApplicationDeploymentManager;
import com.google.common.io.Files;

import net.lingala.zip4j.core.ZipFile;


/**
 */
@Mojo(name = "bundle", threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
public class GGServerPluginMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}")
	private File buildDirectory;

	@Parameter(defaultValue = "${project.basedir}")
	private File basedir;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject mavenProject;

	@Parameter(property = "dependencies")
	private List<Dependency> dependencies;

	@Parameter(property = "commpression")
	private Compression compression;
	
	@Parameter(property = "mainClass")
	private String mainClass;
	
	@Parameter(property = "mainManifestName")
	private String mainManifestName;

	public void execute() throws MojoExecutionException {
		ZipUtils zip = new ZipUtils();
		
		String[] ext = {"properties"};
		GGServerApplicationConfiguration.configurationFileExtensions = ext;
		
		List<Dependency> libs = new ArrayList<Dependency>();
		getLog().info("Build dir " + this.buildDirectory.getAbsolutePath());

		for (final Artifact artifact : mavenProject.getArtifacts()) {
			Dependency e = new Dependency(artifact.getGroupId(), artifact.getArtifactId(), DependencyDestination.none.getString(),
					artifact.getVersion(), false, artifact);

			for (Dependency econf : this.dependencies) {
				if (econf.equals(e)) {
					e.setDest(econf.getDest());
					e.setUnpack(econf.isUnpack());
					break;
				} else {
					e.setDest(DependencyDestination.none.getString());
				}
			}
			libs.add(e);

			System.out.println(e.toXml());
		}

		// create build directory
		String buildDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId()
				+ "-" + this.mavenProject.getVersion();

		String confDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId()
				+ "-" + this.mavenProject.getVersion() + File.separator + "conf";
		String libDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId() + "-"
				+ this.mavenProject.getVersion() + File.separator + "libs";
		String binDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId() + "-"
				+ this.mavenProject.getVersion() + File.separator + "bin";
		String binlibsDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId()
				+ "-" + this.mavenProject.getVersion() + File.separator + "bin" + File.separator + "libs";
		String deployDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId()
				+ "-" + this.mavenProject.getVersion() + File.separator + "deploy";
		String logsDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId()
				+ "-" + this.mavenProject.getVersion() + File.separator + "logs";
		String tmpDir = this.buildDirectory.getAbsolutePath() + File.separator + this.mavenProject.getArtifactId() + "-"
				+ this.mavenProject.getVersion() + File.separator + "tmp";

		File f = new File(buildDir);
		if (f.exists()) {
			f.delete();
		}
		f.mkdirs();

		File confDirF = new File(confDir);
		File libDirF = new File(libDir);
		File binDirF = new File(binDir);
		File binlibsDirF = new File(binlibsDir);
		File deployDirF = new File(deployDir);
		File logsDirF = new File(logsDir);
		File tmpDirF = new File(tmpDir);
		
		zip.addFile(confDirF, DependencyDestination.none);
		zip.addFile(libDirF, DependencyDestination.none);
		zip.addFile(binDirF, DependencyDestination.none);
		zip.addFile(binlibsDirF, DependencyDestination.none);
		zip.addFile(deployDirF, DependencyDestination.none);
		zip.addFile(logsDirF, DependencyDestination.none);
		zip.addFile(tmpDirF, DependencyDestination.none);

		confDirF.mkdir();
		libDirF.mkdir();
		binDirF.mkdir();
		binlibsDirF.mkdir();
		deployDirF.mkdir();
		logsDirF.mkdir();
		tmpDirF.mkdir();

		getLog().info("Created " + f.getAbsolutePath());
		getLog().info("Created " + confDirF.getAbsolutePath());
		getLog().info("Created " + libDirF.getAbsolutePath());
		getLog().info("Created " + binDirF.getAbsolutePath());
		getLog().info("Created " + binlibsDirF.getAbsolutePath());
		getLog().info("Created " + deployDirF.getAbsolutePath());
		getLog().info("Created " + logsDirF.getAbsolutePath());
		getLog().info("Created " + tmpDirF.getAbsolutePath());

		// Copy the libs
		for (Dependency lib : libs) {
			String dir = null;
			
			String[] dests = lib.getDest().split(";");
			
			for( String destStr: dests ) {
				DependencyDestination dest = DependencyDestination.fromString(destStr);
				
				switch (dest) {
				case bin:
					dir = binDir;
					break;
				case binlibs:
					dir = binlibsDir;
					break;
				case conf:
					dir = confDir;
					break;
				case deploy:
					dir = deployDir;
					break;
				case libs:
					dir = libDir;
					break;
				default:
				case none:
					continue;
				}
				File libf = lib.getArtifact().getFile();
				
				File newLib = new File(dir+File.separator+libf.getName());
				
				try {
					Files.copy(libf, newLib);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if( !lib.isUnpack() ) {
					zip.addFile(newLib, DependencyDestination.fromString(lib.getDest()));
				} else {
				    String source = newLib.getAbsolutePath();
				    String destination = dir;
	
				    try {
				         ZipFile zipFile = new ZipFile(source);
				         zipFile.extractAll(destination);
				         newLib.delete();
				    } catch (net.lingala.zip4j.exception.ZipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		
		//generate the main manifest 
		try {
			String dirold = getCurrentDirectory();
			setCurrentDirectory(buildDir);
			
			GGServerApplicationDeploymentManager mgr = GGServerApplicationDeploymentManager.init(confDir, deployDir);
			GGServerApplicationEngine engine = GGServerApplicationEngine.init(mgr, buildDir);
		
			engine.readFolder(libDir, false, false);
			engine.readFolder(confDir, false, false);
			
			String temp = confDir + File.separator+ this.mainManifestName;
			
			getLog().info("Written " + temp);
			
			engine.generateManifest(temp, true, this.mainClass);
		
			setCurrentDirectory(dirold);
		} catch(Exception e) {
			e.printStackTrace();
		}


		// Generate archive

		try {
			ZipUtils2.zipFolder(new File(buildDir), new File(buildDir+".zip"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.mavenProject.getArtifact().setFile(new File(buildDir+".zip"));

	}

    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;  // Boolean indicating whether directory was set
        File    directory;       // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
        {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return result;
    }
    
    public static String getCurrentDirectory() {
    	return System.getProperty("user.dir");
    }

}