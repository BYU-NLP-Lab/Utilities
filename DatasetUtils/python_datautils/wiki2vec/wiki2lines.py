#!/usr/bin/python3
import os
import os.path
import sys
import logging
import argparse
from plf1_python import miscellaneous as misc
from gensim.corpora import  WikiCorpus
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("datautils.wiki2vec")

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='''Create a serialized gensim word2vec/doc2vec model using a wikipedia dump. Taken from https://groups.google.com/forum/#!topic/gensim/MJWrDw_IvXw.''')
  parser.add_argument('wikidump',help="The enwiki-latest-pages-articles.xml.bz2 file downloaded from http://dumps.wikimedia.org/enwiki/latest/")
  parser.add_argument('modelpath',help="Where to output the trained model")
  args = parser.parse_args()

  # check and process input arguments
  inp, outp = args.wikidump, args.modelpath
  logger.info("transforming %s to %s" % (inp,outp))
  space = " "
  i = 0

  os.makedirs(os.path.dirname(outp), exist_ok=True)
  output = open(outp,'w')

  wiki = WikiCorpus(inp, dictionary={})
  for btext in wiki.get_texts():
    utext = [v.decode('utf-8') for v in btext] # bytes -> string
    for sent in misc.sublists_with_max_size(utext,max_size=1000):
      output.write(space.join(sent) + "\n")
      i = i + 1
      if (i % 10000 == 0):
        logger.info("Saved " + str(i) + " articles")

  output.close()

  logger.info("Finished. Saved " + str(i) + " articles")

