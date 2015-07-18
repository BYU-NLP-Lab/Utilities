/**
 * 
 */
package edu.byu.nlp.data.streams;

import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import edu.byu.nlp.io.Files2;

/**
 * @author robbie
 * @author plf1
 * 
 */
public class IndexFileToFileList implements Function<String,Iterable<String>> {

	private static final Logger logger = LoggerFactory.getLogger(IndexFileToFileList.class);

	private final Charset charset;

	public IndexFileToFileList() {
		this(Charset.defaultCharset());
	}

	public IndexFileToFileList(Charset charset) {
	  Preconditions.checkNotNull(charset);
		this.charset = charset;
	}

  @Override
  public Iterable<String> apply(String indexFilePath) {
    try {

      FileObject indexFileObject = VFS.getManager().resolveFile(indexFilePath);
      logger.info("Processing " + indexFilePath);
      
      return Files2.open(indexFileObject, charset);
      
    } catch (FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

}
