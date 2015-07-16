/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import edu.byu.nlp.io.Files2;

public class FilenameToContents implements Function<String,String> {

	private static final Logger logger = LoggerFactory.getLogger(FilenameToContents.class);
	
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
  public String apply(String filename) {
    try {
      logger.debug("Processing " + filename);
      FileObject file = basedir.resolveFile(filename);
      return Files2.toString(file, charset);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
