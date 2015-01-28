/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.data.pipes.Instances.OneToManyLabeledInstanceFunction;
import edu.byu.nlp.io.Files2;
import edu.byu.nlp.io.Paths;

/**
 * @author robbie
 * 
 */
public class IndexFileToLabeledFileList implements OneToManyLabeledInstanceFunction<String, String, String> {

	private static final Logger logger = LoggerFactory.getLogger(IndexFileToLabeledFileList.class);

	private final FileObject basedir;
	private final Charset charset;

	public IndexFileToLabeledFileList(FileObject basedir) {
		this(basedir, Charset.defaultCharset());
	}

	public IndexFileToLabeledFileList(FileObject basedir, Charset charset) {
		Preconditions.checkNotNull(charset);
		this.basedir = basedir;
		this.charset = charset;
	}

	@Override
	public Iterator<FlatInstance<String, String>> apply(final FlatInstance<String, String> indexFile) {
		try {
			String indexFilename = indexFile.getData();
			String indexFilePath = indexFile.getSource();
			
			logger.info("Processing " + indexFilename);
			
			final String label = Paths.splitExtension(indexFilename).getFirst();
			final String source = indexFilePath + "/" + indexFilename;
			Iterable<String> lineIterable = Files2.open(basedir.resolveFile(indexFilename), charset);
			return Iterators.transform(lineIterable.iterator(), new Function<String, FlatInstance<String, String>>() {
				@Override
				public FlatInstance<String, String> apply(String filedata) {
					return new FlatLabeledInstance<String,String>(
							AnnotationInterfaceJavaUtils.newLabeledInstance(filedata, label, source, false));
				}

			});
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

}
