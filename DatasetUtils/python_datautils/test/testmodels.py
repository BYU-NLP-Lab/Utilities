#!/usr/bin/python3
from gensim.models import LdaModel,LdaMulticore,Word2Vec,Doc2Vec

lda=LdaMulticore.load("/tmp/doc2vec-models/LDA-aml-data-newsgroups-indices-tiny_set.model")
w2v=Word2Vec.load("/tmp/doc2vec-models/WORD2VEC-aml-data-newsgroups-indices-tiny_set.model")
d2v=Doc2Vec.load("/tmp/doc2vec-models/PARAVEC-aml-data-newsgroups-indices-tiny_set.model")

#lda=LdaMulticore.load("/tmp/doc2vec-models/LDA-aml-data-newsgroups-indices-full_set-all.model")
#w2v=Word2Vec.load("/tmp/doc2vec-models/WORD2VEC-aml-data-newsgroups-indices-full_set-all.model")
#d2v=Doc2Vec.load("/tmp/doc2vec-models/PARAVEC-aml-data-newsgroups-indices-full_set-all.model")
