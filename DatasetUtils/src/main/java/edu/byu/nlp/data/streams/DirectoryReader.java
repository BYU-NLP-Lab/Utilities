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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;

import edu.byu.nlp.io.AbstractIterable;

/**
 * @author rah67
 * @author plf1
 * 
 */
public class DirectoryReader implements DataSource {

	private final class FilesIterator extends UnmodifiableIterator<Map<String,Object>> {

		private final FileObject[] files;
		private int i;
    private String fieldname;

		private FilesIterator(String fieldname, FileObject[] files) {
		  this.fieldname=fieldname;
			this.files = files;
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < files.length;
		}

		@Override
		public Map<String,Object> next() {
			try {
				String indexFilename = directory.getName().getRelativeName(files[i++].getName());
				Map<String,Object> item = Maps.newHashMap();
				item.put(fieldname, indexFilename);
				return item;
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final class FilesIterable extends AbstractIterable<Map<String,Object>> {
		@Override
		public Iterator<Map<String,Object>> iterator() {
			try {
				// sort children files before returning them so that 
				// results are more predictable across API changes, 
				// architectures, etc.
				FileObject[] children = directory.getChildren();
				Arrays.sort(children, new Comparator<FileObject>() {
					@Override
					public int compare(FileObject o1, FileObject o2) {
						return Ordering.natural().compare(o1.toString(), o2.toString());
					}
				});
				return new FilesIterator(fieldname,children);
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private FileObject directory;
	private String source;
  private String fieldname;

	public DirectoryReader(FileObject directory, String fieldname) throws FileSystemException {
		Preconditions.checkNotNull(directory);
		Preconditions.checkArgument(directory.getType() == FileType.FOLDER, directory + " is not a directory");
		this.source=directory.toString();
		this.directory = directory;
		this.fieldname=fieldname;
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<Map<String,Object>> getStream(){
		return new FilesIterable();
	}

	@Override
	public String getStreamSource() {
		return source;
	}

}
