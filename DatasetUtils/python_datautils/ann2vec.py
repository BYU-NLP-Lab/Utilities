#!/usr/bin/python3
import numpy as np
import shutil
import argparse
from os import path
import os
import gensim
import pipes
from gensim.models import LdaModel,LdaModel,Word2Vec,Doc2Vec
from gensim.models.doc2vec import LabeledSentence
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("datautils.doc2vec")

def model_path(modelname,cachedir,dataset_split):
  dataname = dataset_split.replace('/','-')[1:]
  modelname = "%s-%s.model" % (modelname,dataname)
  return os.path.join(cachedir,modelname)

def info_from(item):
  ''' return (source, annotators, annotations) where annotators and 
      annotations are parallel lists '''
  src, annotators, annotations = item['source'], item['annotator'], item['annotation']
  # ensure list type
  annotators = [annotators] if isinstance(annotators,int) else annotators
  annotations = [annotations] if isinstance(annotations,int) else annotations
  assert len(annotators)==len(annotations)
  return src,annotators,annotations

def sentence_labels(item):
  ''' generate sentence labels for the given sentence. In order to redunce the number of parameters we have to 
      work with, we only add document id (src) for documents that have at least one annotation. '''
  sentence_labels = []
  if 'annotator' in item:
    src, annotators, annotations = info_from(item)
    sentence_labels = [src]
    # an embedding for each annotator+annotation pair
    sentence_labels.extend(["%s-%s"%(a,v) for a,v in zip(annotators,annotations)])
    # an embedding for each annotation value
    sentence_labels.extend(annotations)
  return sentence_labels

def labeled_sentence_objects(sentences):
  for i,item in enumerate(sentences):
    content = item['data'] 
    # an embedding for each doc id 
    # an embedding for each annotation and annotator+annotation pair
    yield LabeledSentence(labels=sentence_labels(item),words=content) 

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='''Convert annotated documents into vectors (a distributed representation) using the doc2vec algorithm with both word information and annotation information. Requires gensim and nltk (`pip3 install -U gensim`; pip install -U nltk)''')
  parser.add_argument('--dataset-basedir',help="The base directory of the dataset whose instances are referred to by the json annotation objects.")
  parser.add_argument('--json-annotation-stream',help="A json file containin a list of annotation objects")
  parser.add_argument('--outdir',default="out",help="Where to write the new dataset (organized into index and data)")
  parser.add_argument('--size',default=100,type=int,help="How large should document vectors be?")
  parser.add_argument('--num-workers',default=8,type=int,help="How many cores to use")
  parser.add_argument('--modeldir',default="/tmp/doc2vec-models",help="Where to cache trained models for reference.")
  parser.add_argument('--content-encoding',default="latin-1",help="How are dataset documents encoded?")
  parser.add_argument('--index-encoding',default="utf-8",help="How are dataset index files encoded?")
  parser.add_argument('--min-count',default=5,type=int,help="Drop features with <min-count occurences.")
  parser.add_argument('--window',default=5,type=int,help="The context size considered by neural language models.")
  args = parser.parse_args()

  # ensure models dir exists
  modelpath = model_path("ann2vec",args.modeldir,args.json_annotation_stream)
  os.makedirs(os.path.dirname(modelpath), exist_ok=True)

  transformed_data = {}

  #####################################################################################
  ### PARAVEC (see http://radimrehurek.com/2014/12/doc2vec-tutorial/)
  #####################################################################################
  # read a list of LabeledSentences (defined by gensim)
  sentences = list(pipes.combination_json2sentences(args.dataset_basedir, args.json_annotation_stream, filepath_attr="datapath",data_attr="data", index_encoding=args.index_encoding, content_encoding=args.content_encoding))
  if os.path.exists(modelpath):
    model = Doc2Vec.load(modelpath)
  else:
    model = Doc2Vec(list(labeled_sentence_objects(sentences)), size=args.size, window=args.window, min_count=args.min_count, workers=args.num_workers)
    model.save(modelpath)

  # translate data
  if args.outdir is not None:
    logger.info("transforming documents to paragraph-vector model (doc2vec) vectors")
    for sent in sentences:
      vec = np.zeros(args.size*2)
      if 'annotation' in sent:
        labels = sentence_labels(sent)
        src_label = labels[0]
        other_labels = labels[1:]

        # empty documents may have been dropped due to feature selection
        if src_label not in model:
          logger.warn("not found in model: document "+src_label)

        # first half for doc vectors
        vec[:args.size] += model[src_label] if src_label in model else np.zeros(args.size)
        # second half for annotation vectors
        for label in other_labels:
          vec[args.size:] += model[label] if label in model else np.zeros(args.size)
        
      # the doc vector will be computed once per sentence, redundantly. Remember only the last
      transformed_data[sent['source']] = vec

  #####################################################################################
  ### Output transformed dataset
  #####################################################################################
  # translate dataset, copying index and converting data to vectors
  outbasedir = args.outdir
  if outbasedir is not None:
    logger.info("writing new dataset to %s" % outbasedir)
    # write new data documents
    for relpath,docvec in transformed_data.items():
      inpath = os.path.join(args.dataset_basedir,relpath)
      outpath = os.path.join(outbasedir,relpath)
      os.makedirs(os.path.dirname(outpath), exist_ok=True)
      with open(outpath,'w') as outfile:
        outfile.write('\n'.join([str(v) for v in docvec]))

