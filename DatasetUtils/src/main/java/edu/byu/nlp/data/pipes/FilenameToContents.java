/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileObject;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.io.Files2;

public class FilenameToContents implements Function<FlatInstance<String,String>, FlatInstance<String,String>> {

	public static Logger logger = Logger.getLogger(FilenameToContents.class.getName());
	
	private final FileObject basedir;
	private final Charset charset;
	
	public FilenameToContents(FileObject basedir) {
		this(basedir, Charset.defaultCharset());
	}
	
	public FilenameToContents(FileObject basedir, Charset charset) {
		Preconditions.checkNotNull(charset);
		this.basedir = basedir;
		this.charset = charset;
	}

	@Override
	public FlatInstance<String, String> apply(FlatInstance<String, String> label) {
		try {
			String filename = label.getData(); 
			logger.fine("Processing " + filename);
			FileObject file = basedir.resolveFile(filename);
			return new FlatLabeledInstance<String,String>(
					AnnotationInterfaceJavaUtils.<String,String>newLabeledInstance(
							Files2.toString(file, charset), label.getLabel(), file.getName().getPath(), false));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
