package edu.byu.nlp.data.pipes;

import java.util.List;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.util.Indexer;

public class IndexerCalculator<D, L>  {

	private Indexer<D> wordIndexer;
	private Indexer<L> labelIndexer;
	private Indexer<Long> annotatorIndexer;
	private Indexer<Long> instanceIndexer;
	
	public IndexerCalculator(Indexer<D> wordIndexer, Indexer<L> labelIndexer, 
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
	
	public static <D,L> IndexerCalculator<D,L> calculate(Iterable<FlatInstance<List<List<D>>, L>> data) {
		Indexer<D> wordIndexer = new Indexer<D>();
		Indexer<L> labelIndexer = new Indexer<L>();
		Indexer<Long> annotatorIndexer = new Indexer<Long>();
		Indexer<Long> instanceIndexer = new Indexer<Long>();
		
		for (FlatInstance<List<List<D>>, L> inst: data){
			if (inst.isAnnotation()){
				// only record annotation id of annotations
				// (this avoids adding automatic annotator to the indexer)
				annotatorIndexer.add(inst.getAnnotator());
			}
			else{
				// only record data if it's a label
				for (List<D> sentence: inst.getData()){
					for (D word: sentence){
						wordIndexer.add(word);
					}
				}
			}

			labelIndexer.add(inst.getLabel());
			instanceIndexer.add(inst.getInstanceId());
		}
		
		return new IndexerCalculator<D,L>(wordIndexer, labelIndexer, instanceIndexer, annotatorIndexer);
	}

}
