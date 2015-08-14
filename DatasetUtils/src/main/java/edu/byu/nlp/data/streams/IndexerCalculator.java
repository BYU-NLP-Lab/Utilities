package edu.byu.nlp.data.streams;

import java.util.Map;

import edu.byu.nlp.data.types.DataStreamInstance;
import edu.byu.nlp.util.Indexer;

public class IndexerCalculator<D, L>  {

	private Indexer<D> wordIndexer;
	private Indexer<L> labelIndexer;
	private Indexer<String> annotatorIdIndexer;
	private Indexer<String> instanceIdIndexer;

  public IndexerCalculator(){
    this(new Indexer<D>(),new Indexer<L>(),new Indexer<String>(),new Indexer<String>());
  }
	public IndexerCalculator(Indexer<D> wordIndexer, Indexer<L> labelIndexer, 
			Indexer<String> instanceIndexer, Indexer<String> annotatorIndexer){
		this.setWordIndexer(wordIndexer);
		this.setLabelIndexer(labelIndexer);
		this.setInstanceIdIndexer(instanceIndexer);
		this.setAnnotatorIdIndexer(annotatorIndexer);
	}

	public Indexer<D> getWordIndexer(){
		return wordIndexer;
	}

	public Indexer<L> getLabelIndexer(){
		return labelIndexer;
	}
	
	public Indexer<String> getInstanceIdIndexer(){
		return instanceIdIndexer;
	}
	
	public Indexer<String> getAnnotatorIdIndexer(){
		return annotatorIdIndexer;
	}
	
  public static <D,L> IndexerCalculator<D,L> calculate(Iterable<Map<String, Object>> data) {
	  IndexerCalculator<D,L> indexers = new IndexerCalculator<>();
	  populateNonFeatureIndexes(data, indexers);
	  populateWordIndex(data, indexers);
	  return indexers;
	}

  public static <D,L> IndexerCalculator<D,L> calculateNonFeatureIndexes(Iterable<Map<String, Object>> data) {
    IndexerCalculator<D,L> indexers = new IndexerCalculator<>();
    populateNonFeatureIndexes(data, indexers);
    return indexers;
  }

  @SuppressWarnings("unchecked")
  public static <D,L> void populateNonFeatureIndexes(Iterable<Map<String, Object>> data, IndexerCalculator<D,L> indexers) {
    Indexer<L> labelIndexer = indexers.getLabelIndexer();
    Indexer<String> annotatorIndexer = indexers.getAnnotatorIdIndexer();
    Indexer<String> instanceIndexer = indexers.getInstanceIdIndexer();
    
    for (Map<String, Object> inst: data){
      if (DataStreamInstance.isAnnotation(inst)){
        // only record annotation id of annotations
        // (this avoids adding automatic annotator to the indexer)
        annotatorIndexer.add((String)inst.get(DataStreamInstance.ANNOTATOR));
      }
      labelIndexer.add((L)DataStreamInstance.getRaw(inst, DataStreamInstance.LABEL));
      labelIndexer.add((L)DataStreamInstance.getRaw(inst, DataStreamInstance.ANNOTATION));
      instanceIndexer.add((String)DataStreamInstance.getRaw(inst, DataStreamInstance.INSTANCE_ID));
    }
  }
  
  private static <D,L> void populateWordIndex(Iterable<Map<String, Object>> data, IndexerCalculator<D,L> indexers) {
    Indexer<D> wordIndexer = indexers.getWordIndexer();
    
    for (Map<String, Object> inst: data){
      if (DataStreamInstance.getData(inst)!=null){
        // only record data if it's a label
        @SuppressWarnings("unchecked")
        Iterable<Iterable<D>> sentences = (Iterable<Iterable<D>>) DataStreamInstance.getRaw(inst, DataStreamInstance.DATA);
        for (Iterable<D> sentence: sentences){
          for (D word: sentence){
            wordIndexer.add(word);
          }
        }
      }
    }
  }
  /**
   * @param wordIndexer the wordIndexer to set
   */
  public void setWordIndexer(Indexer<D> wordIndexer) {
    this.wordIndexer = wordIndexer;
  }
  /**
   * @param labelIndexer the labelIndexer to set
   */
  public void setLabelIndexer(Indexer<L> labelIndexer) {
    this.labelIndexer = labelIndexer;
  }
  /**
   * @param annotatorIdIndexer the annotatorIdIndexer to set
   */
  public void setAnnotatorIdIndexer(Indexer<String> annotatorIdIndexer) {
    this.annotatorIdIndexer = annotatorIdIndexer;
  }
  /**
   * @param instanceIdIndexer the instanceIdIndexer to set
   */
  public void setInstanceIdIndexer(Indexer<String> instanceIdIndexer) {
    this.instanceIdIndexer = instanceIdIndexer;
  }
  
}
