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

def labeled_sentence_objects(sentences):
  for i,item in enumerate(sentences):
    src, content = item['source'], item['data']
    # we set the label to doc id so we can look up an embedding for that doc later
    #sentence_labels = [src,"SENT_%d"%i]
    sentence_labels = [src]
    yield LabeledSentence(labels=sentence_labels,words=content) 

def to_vector(model,doc):
  return "bogus"

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='''Convert documents into vectors (a distributed representation). Requires gensim and nltk (`pip3 install -U gensim`; pip install -U nltk)''')
  parser.add_argument('--dataset-basedir',help="The base directory of a dataset. This is prepended to the paths found in index files")
  parser.add_argument('--dataset-split',help="A split containing index files for some classification dataset. Index files are label names containing files with entries pointing to data files (one entry per line)")
  parser.add_argument('--outdir',default="out",help="Where to write the new dataset (organized into index and data)")
  parser.add_argument('--method',default="LDA",help="Which vectorization method to use. Options include LDA, WORD2VEC, PARAVEC (mikolov's paragraph vectors)")
  parser.add_argument('--size',default=100,type=int,help="How large should document vectors be?")
  parser.add_argument('--num-workers',default=8,type=int,help="How many cores to use")
  parser.add_argument('--modeldir',default="/tmp/doc2vec-models",help="Where to cache trained models for reference.")
  parser.add_argument('--modelpath',default=None,help="A specific model file to load")
  parser.add_argument('--content-encoding',default="latin-1",help="How are dataset documents encoded?")
  parser.add_argument('--index-encoding',default="utf-8",help="How are dataset index files encoded?")
  parser.add_argument('--min-count',default=5,type=int,help="Drop features with <min-count occurences.")
  parser.add_argument('--window',default=7,type=int,help="The context size considered by neural language models.")
  args = parser.parse_args()

  # check args
  assert args.modelpath is None or os.path.exists(args.modelpath), "--modelpath does not exist"

  def load_word2vec_model(modelpath):
    try:
      return Word2Vec.load(modelpath)
    except:
      return Word2Vec.load_word2vec_format(modelpath,binary=True)

  # ensure models dir exists
  args.method = args.method.upper()
  modelpath = args.modelpath or model_path(args.method,args.modeldir,args.dataset_split)
  os.makedirs(os.path.dirname(modelpath), exist_ok=True)

  transformed_data = {}

  ######################################################################################
  ### LDA
  ######################################################################################
  if args.method=="LDA":
    # read a bag of words corpus (bow_corpus)
    # example: [ [(0,1),(1,1)], ...]
    bow_data,id2word = pipes.combination_index2bow(args.dataset_basedir, args.dataset_split, index_encoding=args.index_encoding, content_encoding=args.content_encoding)
    if os.path.exists(modelpath):
      model = LdaModel.load(modelpath)
    else:
      logger.info("training lda model")
      model = LdaModel(list(pipes.pipe_select_attr(bow_data,attr='data')), alpha='auto', id2word=id2word, num_topics=args.size, passes=20, iterations=100)
      model.save(modelpath)

    # translate data
    if args.outdir is not None:
      logger.info("transforming documents to lda vectors")
      for item in bow_data:
        src, content = item['source'], item['data']
        #doctuples = model[content] # for some reason omits topics <= .01
        doctuples = model.__getitem__(content, eps=0)
        docdict = dict(doctuples)
        docvec = [str(docdict[i]) for i in range(len(docdict))] # enforce consistent topic order
        transformed_data[src] = docvec

  ######################################################################################
  ### WORD2VEC (see http://radimrehurek.com/2013/09/word2vec-in-python-part-two-optimizing/)
  ######################################################################################
  elif args.method=="WORD2VEC":
    # read a list of sentences
    # example: [ ["whanne","that",...], ...]
    sentences = list(pipes.combination_index2sentences(args.dataset_basedir, args.dataset_split, index_encoding=args.index_encoding, content_encoding=args.content_encoding))
    if os.path.exists(modelpath):
      model = load_word2vec_model(modelpath)
      args.size=model.layer1_size
    else:
      logger.info("training word2vec model")
      model = Word2Vec(list(pipes.pipe_select_attr(sentences,attr="data")), size=args.size, window=args.window, min_count=args.min_count, workers=args.num_workers)
      model.save(modelpath)
      #model.most_similar(positive=["Tuesday"],negative=[])
      #model.similarity("blue","green")
      #model["computer"]

    # translate data
    if args.outdir is not None:
      logger.info("transforming documents to word2vec vectors")
      for item in sentences:
        src, content = item['source'], item['data']
        transformed_data[src] = np.zeros(args.size)
        num_addends = 0.0
        for word in content:
          if word in model:
            num_addends += 1.0
            transformed_data[src] += model[word] # sum together all word vectors in doc
        transformed_data[src] /= num_addends or 1.0 # average word vectors

  ######################################################################################
  ### PARAVEC (see http://radimrehurek.com/2014/12/doc2vec-tutorial/)
  ######################################################################################
  elif args.method=="PARAVEC":
    # read a list of LabeledSentences (defined by gensim)
    sentences = list(pipes.combination_index2sentences(args.dataset_basedir, args.dataset_split, index_encoding=args.index_encoding, content_encoding=args.content_encoding))
    if os.path.exists(modelpath):
      model = Doc2Vec.load(modelpath)
    else:
      model = Doc2Vec(list(labeled_sentence_objects(sentences)), size=args.size, window=args.window, min_count=args.min_count, workers=args.num_workers)
      model.save(modelpath)

    # translate data
    if args.outdir is not None:
      logger.info("transforming documents to paragraph-vector model (doc2vec) vectors")
      for item in sentences:
        src, content = item['source'], item['data']
        if src not in model:
          logger.warn("not found in model: document "+src)
        transformed_data[src] = model[src] if src in model else np.zeros(args.size)

  else:
    raise Exception("unknown method",args.method)


  ######################################################################################
  ### Output transformed dataset
  ######################################################################################
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
    # copy index as is (since paths in the index are relative)
    assert args.dataset_basedir in args.dataset_split, "The dataset split dir is assumed to be inside the basedir"
    outsplit = os.path.join(outbasedir, args.dataset_split.replace(args.dataset_basedir+'/',''))
    shutil.rmtree(outsplit, ignore_errors=True)
    logger.info("copying indices from %s to %s" % (args.dataset_split, outsplit))
    shutil.copytree(args.dataset_split, outsplit)

