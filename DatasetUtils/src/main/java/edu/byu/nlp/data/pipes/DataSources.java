/**
 * Copyright 2012 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.data.pipes;

import java.util.List;

import com.google.common.collect.Lists;

import edu.byu.nlp.data.FlatInstance;



/**
 * @author rah67
 * @author plf1
 *
 */
public class DataSources {

	private DataSources() { }
	
	private static class ConnectedDataSource<ID, IL, OD, OL> implements DataSource<OD, OL> {

		private final DataSource<ID, IL> src;
		private final LabeledInstancePipe<ID, IL, OD, OL> labeledInstancePipe;
		
		public ConnectedDataSource(DataSource<ID, IL> src, LabeledInstancePipe<ID, IL, OD, OL> labeledInstancePipe) {
			this.src = src;
			this.labeledInstancePipe = labeledInstancePipe;
		}
		
		@Override
		public Iterable<FlatInstance<OD, OL>> getLabeledInstances() {
			return labeledInstancePipe.apply(src.getLabeledInstances());
		}

		@Override
		public String getSource() {
			return src.getSource();
		}
		
	}
	
	public static <ID, IL, OD, OL> DataSource<OD, OL> connect(
			DataSource<ID, IL> src, 
			LabeledInstancePipe<ID, IL, OD, OL> labeledInstancePipe) {
		return new ConnectedDataSource<ID, IL, OD, OL>(src, labeledInstancePipe);
	}
	
	// read in all of the data at once to reduce disk reads
	public static <D, L> List<FlatInstance<D, L>> cache(DataSource<D, L> source) {
		return Lists.newArrayList(source.getLabeledInstances());
	}

	private static class IterableDataSource<D, L> implements DataSource<D, L> {

		private final Iterable<FlatInstance<D, L>> labelIt;
		private String source;
		
		public IterableDataSource(String source, Iterable<FlatInstance<D, L>> labelIt) {
			this.source=source;
			this.labelIt = labelIt;
		}
		@Override
		public Iterable<FlatInstance<D, L>> getLabeledInstances() {
			return labelIt;
		}
		@Override
		public String getSource() {
			return source;
		}
		
	}
	
	public static <D, L> DataSource<D, L> from(
			String source,
			Iterable<FlatInstance<D, L>> labelIt) {
		return new IterableDataSource<D, L>(source,labelIt);
	}

	/**
	public static <L, W> List<FlatInstance<L, List<W>>> cacheSequenceData(DataSource<L, List<W>> source) {
		List<FlatInstance<L, List<W>>> cached = Lists.newArrayList();
		for (FlatInstance<L, List<W>> instance : source.getData()) {
			
			cached.add(BasicInstance.of(instance.getLabel(), instance.getSource(), instance.getData()));
		}
		return cached;
	}
   **/
}
