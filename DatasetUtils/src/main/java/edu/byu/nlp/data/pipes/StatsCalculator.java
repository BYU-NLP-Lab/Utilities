package edu.byu.nlp.data.pipes;

import java.util.List;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.util.Indexer;

public class StatsCalculator<D, L>  {

	private Indexer<D> wordIndexer;
	private Indexer<L> labelIndexer;
	private Indexer<Long> annotatorIndexer;
	private Indexer<Long> instanceIndexer;
	
	public StatsCalculator(Indexer<D> wordIndexer, Indexer<L> labelIndexer, 
			Indexer<Long> instanceIndexer, Indexer<Long> annotatorIndexer){
		this.wordIndexer=wordIndexer;
		this.labelIndexer=labelIndexer;
		this.instanceIndexer=instanceIndexer;
		this.annotatorIndexer=annotatorIndexer;
	}

	public Indexer<D> getWordIndexer(){
		return wordIndexer;
	}

	public Indexer<L> getLabelIndexer(){
		return labelIndexer;
	}
	
	public Indexer<Long> getInstanceIdIndexer(){
		return instanceIndexer;
	}
	
	public Indexer<Long> getAnnotatorIdIndexer(){
		return annotatorIndexer;
	}
	
	public static <D,L> StatsCalculator<D,L> calculate(Iterable<FlatInstance<List<D>, L>> data) {
		Indexer<D> wordIndexer = new Indexer<D>();
		Indexer<L> labelIndexer = new Indexer<L>();
		Indexer<Long> annotatorIndexer = new Indexer<Long>();
		Indexer<Long> instanceIndexer = new Indexer<Long>();
		
		for (FlatInstance<List<D>, L> inst: data){
			for (D word: inst.getData()){
				wordIndexer.add(word);
			}
			labelIndexer.add(inst.getLabel());
			annotatorIndexer.add(inst.getAnnotator());
			instanceIndexer.add(inst.getInstanceId());
		}
		
		return new StatsCalculator<D,L>(wordIndexer, labelIndexer, instanceIndexer, annotatorIndexer);
	}

}
