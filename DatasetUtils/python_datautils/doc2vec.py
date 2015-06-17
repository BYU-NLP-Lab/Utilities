#!/usr/bin/python3
import pickle
import numpy as np
import shutil
import argparse
import os
import gensim
import pipes
from gensim.models import LdaModel,LdaModel,Word2Vec,Doc2Vec
from gensim.models.doc2vec import LabeledSentence
from gensim.models.word2vec import Vocab
from plf1_python import doc2vec_util
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("datautils.doc2vec")

def model_path(modelname,cachedir,dataset_indexdir):
  dataname = dataset_indexdir.replace('/','-')[1:]
  modelname = "%s-%s.model" % (modelname,dataname)
  return os.path.join(cachedir,modelname)

def exists(loc):
  return loc is not None and os.path.exists(loc)

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='''Convert documents into vectors (a distributed representation). Requires gensim and nltk (`pip3 install -U gensim`; pip install -U nltk)''')
  parser.add_argument('--dataset-basedir',help="The base directory of a dataset. This is prepended to the paths found in index files")
  parser.add_argument('--dataset-indexdir',help="A folder containing index files for some classification dataset. Index files are label names containing files with entries pointing to data files (one entry per line)")
  parser.add_argument('--outdir',default=None,required=True,help="Where to write the new dataset (organized into index and data)")
  parser.add_argument('--method',default="LDA",help="Which vectorization method to use. Options include LDA, WORD2VEC, PARAVEC (mikolov's paragraph vectors)")
  parser.add_argument('--size',default=100,type=int,help="How large should document vectors be?")
  parser.add_argument('--num-workers',default=8,type=int,help="How many cores to use")
  parser.add_argument('--cached-modelpath',default=None,help="A model that should be used as-is (with no additional training).")
  parser.add_argument('--init-modelpath',default=None,help="A model that should be used to initialize prior to training.")
  parser.add_argument('--out-modelpath',required=True,help="Where to save the model that was used.")
  parser.add_argument('--content-encoding',default="latin-1",help="How are dataset documents encoded?")
  parser.add_argument('--index-encoding',default="utf-8",help="How are dataset index files encoded?")
  parser.add_argument('--min-count',default=5,type=int,help="Drop features with <min-count occurences.")
  parser.add_argument('--window',default=7,type=int,help="The context size considered by neural language models.")
  args = parser.parse_args()

  # ensure models dir exists
  args.method = args.method.upper()
  os.makedirs(os.path.dirname(args.out_modelpath), exist_ok=True)

  transformed_data = {}

  #####################################################################################
  ### LDA
  #####################################################################################
  if args.method=="LDA":
    # read a bag of words corpus (bow_corpus)
    # example: [ [(0,1),(1,1)], ...]
    bow_data,id2word = pipes.combination_index2bow(args.dataset_basedir, args.dataset_indexdir, index_encoding=args.index_encoding, content_encoding=args.content_encoding)
    if exists(args.cached_modelpath):
      logger.info("loading lda model %s and using as-is" % args.cached_modelpath)
      model = LdaModel.load(args.cached_modelpath)
    elif exists(args.init_modelpath):
      raise Exception("incremental model training not implemented for LDA. Remove --init-modelpath and try again.")
    else:
      logger.info("training lda model from scratch")
      model = LdaModel(list(pipes.pipe_select_attr(bow_data,attr='data')), alpha='auto', id2word=id2word, num_topics=args.size, passes=20, iterations=100)
      logger.info("saving lda model to %s"%args.out_modelpath)
      model.save(args.out_modelpath)

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

  #####################################################################################
  ### WORD2VEC (see http://radimrehurek.com/2013/09/word2vec-in-python-part-two-optimizing/)
  #####################################################################################
  elif args.method=="WORD2VEC":
    # read a list of sentences
    # example: [ ["whanne","that",...], ...]
    sentences = list(pipes.combination_index2sentences(args.dataset_basedir, args.dataset_indexdir, index_encoding=args.index_encoding, content_encoding=args.content_encoding))
    traindata = list(pipes.pipe_select_attr(sentences,attr="data"))
    if exists(args.cached_modelpath):
      logger.info("loading word2vec model %s and using as-is" % args.cached_modelpath)
      model = Word2Vec.load(args.cached_modelpath)
    else:
      if exists(args.init_modelpath):
        logger.info("initializing word2vec model with %s and then training" % args.init_modelpath)
        model = Word2Vec.load(args.init_modelpath)
        model.train(traindata)
      else:
        logger.info("training word2vec model from scratch")
        model = Word2Vec(traindata, size=args.size, window=args.window, min_count=args.min_count, workers=args.num_workers)
      # trim unneeded model memory = use (much) less RAM
      model.init_sims(replace=True)
      logger.info("saving word2vec model to %s"%args.out_modelpath)
      model.save(args.out_modelpath)

    # translate data
    if args.outdir is not None:
      logger.info("transforming documents to word2vec vectors")
      for item in sentences:
        src, content = item['source'], item['data']
        transformed_data[src] = np.zeros(model.layer1_size)
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
    sentences = list(pipes.combination_index2sentences(args.dataset_basedir, args.dataset_indexdir, index_encoding=args.index_encoding, content_encoding=args.content_encoding))
    traindata = list(doc2vec_util.labeled_sentence_objects(sentences))
    if exists(args.cached_modelpath):
      logger.info("loading doc2vec model %s and using as-is" % args.cached_modelpath)
      model = Doc2Vec.load(args.cached_modelpath)
    else:
      if exists(args.init_modelpath):
        model = doc2vec_util.train_doc2vec_from_word2vec(args.init_modelpath, traindata, train_iterations=20)
      else:
        model = Doc2Vec(traindata, size=args.size, window=args.window, min_count=args.min_count, workers=args.num_workers)
      # trim unneeded model memory = use (much) less RAM
      #model.init_sims(replace=True)
      logger.info("saving doc2vec model to %s"%args.out_modelpath)
      model.save(args.out_modelpath)
      f = open('/tmp/pickled2v','wb') # DELME
      pickle.dump(traindata,f) # DELME
      f.close() # DELME

    # translate data
    if args.outdir is not None:
      logger.info("transforming documents to paragraph-vector model (doc2vec) vectors")
      for item in sentences:
        src, content = item['source'], item['data']
        if src not in model:
          logger.warn("not found in model: document "+src)
        transformed_data[src] = model[src] if src in model else np.zeros(model.layer1_size)

  else:
    raise Exception("unknown method",args.method)


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
    # copy index as is (since paths in the index are relative)
    assert args.dataset_basedir in args.dataset_indexdir, "The dataset indexdir is assumed to be inside the basedir"
    outindex = os.path.join(outbasedir, args.dataset_indexdir.replace(args.dataset_basedir+'/',''))
    shutil.rmtree(outindex, ignore_errors=True)
    logger.info("copying indices from %s to %s" % (args.dataset_indexdir, outindex))
    shutil.copytree(args.dataset_indexdir, outindex)

