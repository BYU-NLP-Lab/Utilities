#!/usr/bin/python3
import numpy as np
import shutil
import argparse
import os.path
import os
import gensim
import pipes
from gensim.models import LdaModel,LdaModel,Word2Vec,Doc2Vec
from gensim.models.doc2vec import LabeledSentence
from gensim.corpora import  WikiCorpus
from gensim.models.word2vec import LineSentence
import logging
import sys
import multiprocessing
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("datautils.wiki2vec")

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='''Create a serialized gensim word2vec/doc2vec model using a wikipedia dump. Taken from https://groups.google.com/forum/#!topic/gensim/MJWrDw_IvXw.''')
  parser.add_argument('linespath',help="A document with one sentence/doc per line (training data).")
  parser.add_argument('modelpath',help="Where to output the trained model")
  parser.add_argument('--workers',default=multiprocessing.cpu_count(),help="How many workers (cpus) to use?")
  parser.add_argument('--min-count',default=5,type=int,help="Drop features with <min-count occurences.")
  parser.add_argument('--window',default=10,type=int,help="The context size considered by neural language models.")
  parser.add_argument('--size',default=300,type=int,help="The context size considered by neural language models.")
  args = parser.parse_args()

  # check and process input arguments
  inp, outp = args.linespath, args.modelpath
  
  model = Word2Vec(LineSentence(inp), size=args.size, window=args.window, min_count=args.min_count, workers=args.workers)

  model.save(outp)
