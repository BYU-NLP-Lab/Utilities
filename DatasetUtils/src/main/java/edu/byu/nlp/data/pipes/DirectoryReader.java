/**
 * Copyright 2012 Brigham Young University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.byu.nlp.data.pipes;

import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;

import edu.byu.nlp.annotationinterface.java.AnnotationInterfaceJavaUtils;
import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.FlatLabeledInstance;
import edu.byu.nlp.io.AbstractIterable;

/**
 * @author rah67
 * 
 */
public class DirectoryReader implements DataSource<String, String> {

	private final class FilesIterator extends UnmodifiableIterator<FlatInstance<String, String>> {

		private final FileObject[] files;
		private int i;

		private FilesIterator(FileObject[] files) {
			this.files = files;
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < files.length;
		}

		@Override
		public FlatInstance<String, String> next() {
			try {
				String indexFilename = directory.getName().getRelativeName(files[i++].getName());
				return new FlatLabeledInstance<String,String>(AnnotationInterfaceJavaUtils.<String,String>newLabeledInstance(
						indexFilename, indexFilename, directory.getName().getPath()));
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final class FilesIterable extends AbstractIterable<FlatInstance<String, String>> {
		@Override
		public Iterator<FlatInstance<String, String>> iterator() {
			try {
				return new FilesIterator(directory.getChildren());
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private FileObject directory;
	private String source;

	public DirectoryReader(String source, FileObject directory) throws FileSystemException {
		Preconditions.checkNotNull(directory);
		Preconditions.checkArgument(directory.getType() == FileType.FOLDER, directory + " is not a directory");
		this.source=source;
		this.directory = directory;
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<FlatInstance<String, String>> getLabeledInstances() {
		return new FilesIterable();
	}

	@Override
	public String getSource() {
		return source;
	}

}
