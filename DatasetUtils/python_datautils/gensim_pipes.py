import sys
import re
from pprint import pprint
import json
import csv
import argparse
from os import path
import numpy as np
from gensim.models import Word2Vec,Doc2Vec
from gensim.models.doc2vec import LabeledSentence
from gensim.models.word2vec import Vocab
import sklearn.decomposition
import scipy
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("python_datautils.gensim_pipes")


def labeled_sentence_objects(sentences):
    for i,item in enumerate(sentences):
        src, content = item['source'], item['data']
        # we set the label to doc id so we can look up an embedding for that doc later
        #sentence_labels = [src,"SENT_%d"%i]
        sentence_labels = [src]
        if len(content)>0:
            yield LabeledSentence(labels=sentence_labels,words=content)


def train_doc2vec_from_word2vec(w2vmodel,traindata,train_iterations=20,train_words=False):

    def __add_rows_to_matrix(m,num_rows,use_zeros=False):
        mrows, mcols= m.shape[0], m.shape[1]
        # create a larger matrix
        newm = np.zeros((mrows+num_rows,mcols), dtype=m.dtype)
        if not use_zeros:
            # randomize weights vector by vector, rather than materializing a huge random matrix in RAM at once
            for i in range(mrows,mrows+num_rows):
                # adapted from word2vec.reset_weights, just in case there was some empirical reason they randomize like this
                newm[i] = (np.random.rand(mcols) - 0.5) / mcols
        # copy old rows over
        newm[0:mrows,] = m
        return newm

    def __extend_model_with_labels(model,labeled_sentences):
        newvocab = {}
        # get all the new vocab terms (and their counts)
        for lsent in labeled_sentences:
            for label in lsent.labels:
                if label not in model:
                    if label not in newvocab:
                        newvocab[label] = Vocab(count=0)
                    newvocab[label].count += len(lsent.words)
        logger.info("extending d2v vocabulary with %d document labels"%len(newvocab))
        # extend the model's vocabulary and index
        for w,v in newvocab.items():
            v.index = len(model.vocab)
            assert len(model.index2word)==v.index and len(model.vocab)==v.index
            model.vocab[w] = v
            model.index2word.append(w)
        model.create_binary_tree()
        model.precalc_sampling()
        model.syn0 = __add_rows_to_matrix(model.syn0,len(newvocab))
        model.syn1 = __add_rows_to_matrix(model.syn1,len(newvocab), use_zeros=True)
        logger.info("finished extending d2v vocabulary with %d document labels"%len(newvocab))

    # load vectors into a doc2vec model
    if isinstance(w2vmodel,str):
        logger.info("initializing doc2vec model with %s and then training" % w2vmodel)
        w2v = Word2Vec.load(w2vmodel)
    else: 
        w2v = w2vmodel
    d2v = Doc2Vec()
    d2v.vocab=w2v.vocab
    d2v.syn0=w2v.syn0
    d2v.syn1=w2v.syn1
    d2v.windows=w2v.window
    d2v.index2word=w2v.index2word
    #d2v.iter=train_iterations # ideally we've be able to just use this, but it doesn't appear to be working
    # now tweak word embeddings and learn doc embeddings for current dataset 
    d2v.train_words = train_words
    __extend_model_with_labels(d2v,traindata)
    num_words = sum([len(s.words) for s in traindata])
    for i in range(train_iterations):
        np.random.shuffle(traindata)
        d2v.train(traindata,total_words=num_words)
    return d2v



def pipe_txt2list_doc2vec(pipe,id_attr,data_attr,model,train_words=False,doc_id="source",word_delim=" "):
    ''' transform a text field into a vector using mikolov and quoc's paragraph
        vector model (doc2vec in gensim). A pre-trained word2vec model is used to initialize word vectors, and 
        then additional embeddings are added, one per source, and the model is trained 
        to embed each source/document. Sources with no known words are set to the zero vector. '''
    logger.info("Embedding text with doc2vec...")
    def label_for(item):
        return "src=%s"%item[id_attr].replace(' ','_')
    def words_for(item):
        return [v for v in item[data_attr].lower().split(word_delim) if v!=""]
    def traindata_for(items):
        # use a dict to ensure we have exactly 1 copy of each tweet
        label2data = {}
        for item in items:
            if id_attr in item and data_attr in item:
                label2data[label_for(item)] = words_for(item)
        return labeled_sentence_objects([{"source":k, "data":v} for k,v in label2data.items()])

    items = list(pipe)

    # update w2v model with doc vectors 
    traindata = list(traindata_for(items))
    d2v = train_doc2vec_from_word2vec(model, traindata, train_words=train_words, train_iterations=20)
    d2v.save('/tmp/d2v.model') # TODO: for debugging

    # substitute each attribute value for its embedding
    for item in items:
        if id_attr in item and data_attr in item:
            item[data_attr] = list(d2v[label_for(item)]) # convert out of numpy into a json-representable format
            item[data_attr] = [float(v) for v in item[data_attr]] # TODO: is this necessary?
        yield item



def pipe_txt2list_word2vec(pipe,attr,model,word_delim=" "):
    ''' transform a text field into a vector by splitting it into words, 
        looking up each word's embedding in a given serialized word2vec model, 
        and then averaging those. Attributes with no known words are set 
        to the zero vector. '''
    logger.info("Embedding text with word2vec...")

    if isinstance(model,str):
        w2v = Word2Vec.load(model)
    else:
        w2v = model

    for item in pipe:
        if attr in item:
            # split out attr
            words = [w.lower() for w in item[attr].split(word_delim)]

            embedding = np.zeros(w2v.layer1_size,dtype='float64')
            num_words = 0.0
            for word in words:
                if word in w2v:
                    num_words += 1.0
                    embedding += w2v[word]
            if num_words>0:
                embedding /= num_words
            else:
                logger.warn("data had 0 words known to word2vec: %s"%item[attr])

            item[attr] = list(embedding) # convert out of numpy into a json-representable format
        yield item

def pipe_combine_embeddings(pipe,attr1,attr2,dest_attr,cosine=True,l1norm=False,l2norm=False,raw1=False,raw2=False,rawdiff=False,pca1=False,pca2=False,pcadiff=False,pca_components=0):
    logger.info("Combining embeddings...")

    def embedding_string(v):
        ''' a hashable string representation so we can avoid adding the 
            same vector to the dataset twice. This will clobber some 
            vectors that are VERY close to one another (in every dimension), 
            but that's pretty unlikely. '''
        return json.dumps([round(n,ndigits=4) for n in list(v)])

    # unfortunately, we need to cache the pipe to compute pca
    if pca_components>0 and (pca1 or pca2 or pcadiff):
        pipe = list(pipe)
        logger.info("pca-fitting %d items..." % len(pipe))

        # precompute PCA transform for entire dataset
        embeddings = {}
        for item in pipe:
            # get the values of the attributes to combine
            vals = [item[attr] for attr in [attr1,attr2] if attr in item]
            assert len(vals)==0 or len(vals)==2
            for v in vals:
                embeddings[embedding_string(v)] = v
        pca = sklearn.decomposition.pca.PCA()
        pca.fit(list(embeddings.values()))
        pca_components = min(pca_components, pca.n_components_)

    logger.info("combining vectors %s and %s..."%(attr1,attr2))
    for item in pipe:
        newitem = {k:v for k,v in item.items() if k not in [attr1,attr2]}

        # get the values of the attributes to combine
        vals = [item[attr] for attr in [attr1,attr2] if attr in item]
        assert len(vals)==0 or len(vals)==2
        if len(vals)==2:
            v1,v2=np.array(vals[0]),np.array(vals[1])
            if pca_components>0:
                v1pca,v2pca = pca.transform(v1)[0][0:pca_components], pca.transform(v2)[0][0:pca_components]

            features = []
            if cosine:
                features.append(scipy.spatial.distance.cosine(v1,v2))
            if l1norm:
                features.append(np.linalg.norm(v1-v2,1)) # L1 norm
            if l2norm:
                features.append(np.linalg.norm(v1-v2,2)) # L2 norm
            if raw1:
                features.extend(v1)
            if raw2:
                features.extend(v2)
            if rawdiff:
                features.extend(v1-v2)
            if pca1:
                features.extend(v1pca)
            if pca2:
                features.extend(v2pca)
            if pcadiff:
                features.extend(v1pca-v2pca)
            newitem[dest_attr] = features

        yield newitem

