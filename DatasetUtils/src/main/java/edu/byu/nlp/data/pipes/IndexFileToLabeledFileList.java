/**
 * 
 */
package edu.byu.nlp.data.pipes;

import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
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
 * @author plf1
 * 
 */
public class IndexFileToLabeledFileList implements OneToManyLabeledInstanceFunction<String, String, String> {

	private static final Logger logger = LoggerFactory.getLogger(IndexFileToLabeledFileList.class);

	private final Charset charset;

	public IndexFileToLabeledFileList() {
		this(Charset.defaultCharset());
	}

	public IndexFileToLabeledFileList(Charset charset) {
		Preconditions.checkNotNull(charset);
		this.charset = charset;
	}

	@Override
	public Iterator<FlatInstance<String, String>> apply(final FlatInstance<String, String> indexFile) {
		try {

			String indexFilename = indexFile.getData();
			String indexDir = indexFile.getSource();
			final String indexFilePath = indexDir + "/" + indexFilename;
			FileObject indexFileObject = VFS.getManager().resolveFile(indexFilePath);
			
			logger.info("Processing " + indexFilename);
			
			final String label = Paths.splitExtension(indexFilename).getFirst();
			Iterable<String> lineIterable = Files2.open(indexFileObject, charset);
			return Iterators.transform(lineIterable.iterator(), new Function<String, FlatInstance<String, String>>() {
				@Override
				public FlatInstance<String, String> apply(String filedata) {
					return new FlatLabeledInstance<String,String>(
							AnnotationInterfaceJavaUtils.newLabeledInstance(filedata, label, indexFilePath, false));
				}

			});
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

}
