package edu.byu.nlp.data.pipes;

import java.util.List;

import edu.byu.nlp.data.FlatInstance;
import edu.byu.nlp.data.types.SparseFeatureVector;
import edu.byu.nlp.util.Indexer;

public class IndexerCalculator<D, L>  {

	private Indexer<D> wordIndexer;
	private Indexer<L> labelIndexer;
	private Indexer<Long> annotatorIdIndexer;
	private Indexer<Long> instanceIdIndexer;

  public IndexerCalculator(){
    this(new Indexer<D>(),new Indexer<L>(),new Indexer<Long>(),new Indexer<Long>());
  }
	public IndexerCalculator(Indexer<D> wordIndexer, Indexer<L> labelIndexer, 
			Indexer<Long> instanceIndexer, Indexer<Long> annotatorIndexer){
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
	
	public Indexer<Long> getInstanceIdIndexer(){
		return instanceIdIndexer;
	}
	
	public Indexer<Long> getAnnotatorIdIndexer(){
		return annotatorIdIndexer;
	}
	
	@SuppressWarnings("unchecked")
  public static <D,L> IndexerCalculator<D,L> calculate(Iterable<FlatInstance<List<List<D>>, L>> data) {
	  IndexerCalculator<D, L> indexers = new IndexerCalculator<>();
	  populateNonFeatureIndexes((Iterable<FlatInstance<?, L>>)(Object)data, indexers);
	  populateWordIndex(data, indexers);
	  return indexers;
	}

  @SuppressWarnings("unchecked")
  public static <D,L> IndexerCalculator<D,L> calculateNonFeatureIndexes(Iterable<FlatInstance<SparseFeatureVector, L>> data) {
    IndexerCalculator<D, L> indexers = new IndexerCalculator<>();
    populateNonFeatureIndexes((Iterable<FlatInstance<?, L>>)(Object)data, indexers);
    return indexers;
  }

  public static <L> void populateNonFeatureIndexes(Iterable<FlatInstance<?, L>> data, IndexerCalculator<?,L> indexers) {
    Indexer<L> labelIndexer = indexers.getLabelIndexer();
    Indexer<Long> annotatorIndexer = indexers.getAnnotatorIdIndexer();
    Indexer<Long> instanceIndexer = indexers.getInstanceIdIndexer();
    
    for (FlatInstance<?, L> inst: data){
      if (inst.isAnnotation()){
        // only record annotation id of annotations
        // (this avoids adding automatic annotator to the indexer)
        annotatorIndexer.add(inst.getAnnotator());
      }
      labelIndexer.add(inst.getLabel());
      instanceIndexer.add(inst.getInstanceId());
    }
  }
  
  private static <D,L> void populateWordIndex(Iterable<FlatInstance<List<List<D>>, L>> data, IndexerCalculator<D,L> indexers) {
    Indexer<D> wordIndexer = indexers.getWordIndexer();
    
    for (FlatInstance<List<List<D>>, L> inst: data){
      if (!inst.isAnnotation()){
        // only record data if it's a label
        for (List<D> sentence: inst.getData()){
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
  public void setAnnotatorIdIndexer(Indexer<Long> annotatorIdIndexer) {
    this.annotatorIdIndexer = annotatorIdIndexer;
  }
  /**
   * @param instanceIdIndexer the instanceIdIndexer to set
   */
  public void setInstanceIdIndexer(Indexer<Long> instanceIdIndexer) {
    this.instanceIdIndexer = instanceIdIndexer;
  }
  
}
