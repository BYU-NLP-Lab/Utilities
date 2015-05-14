#!/usr/bin/python3
import itertools
import nltk.data
from nltk.stem.porter import PorterStemmer
import re
from collections import Counter
import os
import pkg_resources
from data_structures import Indexer
import sys
# ensure cwd is scanned for modules in case we are running from there but 
# haven't taken the trouble to put python_datutils on our PYTHONPATH
sys.path.append(os.path.dirname(os.getcwd()))
mallet_stopwords = set(str(pkg_resources.resource_string('python_datautils','mallet_stopwords.txt'),encoding='utf-8').split())
import logging
logger = logging.getLogger("pipes")

def transformed_item(item,index,newvalue):
    newitem = list(item)
    newitem[index] = newvalue
    return newitem

def input_index(dataset_splitdir,encoding='utf-8'):
    ''' A pipe (generator) that yields a list of filepath, one for each 
        entry in index files (whose name are their labels, ignored) 
        found in the dataset_splitdir '''
    count = 0
    for root, dirs, files in os.walk(dataset_splitdir):
        for index_filename in files:
            with open(os.path.join(root,index_filename),encoding=encoding) as indexfile:
                for datafile in indexfile:
                    count += 1
                    if count%100==0:
                        logger.info("processing document %d"%count)
                    yield datafile.strip()

def input_labeledindex(dataset_splitdir,encoding='utf-8'):
    ''' A pipe (generator) that yields (label,filepath), one for each 
        entry in index files (whose name are their label) 
        found in the dataset_splitdir '''
    count = 0
    for root, dirs, files in os.walk(dataset_splitdir):
        for index_filename in files:
            with open(os.path.join(root,index_filename),encoding=encoding) as index_file:
                for datafile in index_file:
                    count += 1
                    if count%100==0:
                        logger.info("processing document %d"%count)
                    yield index_filename,datafile.strip()

def pipe_append_filecontent(pipe,basedir,encoding='utf-8',index=0):
    ''' Append file content to the end of each tuple, reading to filepaths at position "index" '''
    for item in pipe:
        filepath = item[index] 
        assert isinstance(filepath,str)
        try:
            with open(os.path.join(basedir,filepath),encoding=encoding) as datafile:
                newitem = list(item)
                newitem.append(datafile.read())
                yield newitem
        except:
            logger.warn("unable to open file %s. Skipping." % os.path.join(basedir,filepath))

def pipe_txt2txt_emailheader_stripper(pipe,index=0,header_regex="\n\n|\r\r|\n\r\n\r"):
    ''' remove the email header from text (look for empty line delimiter)'''
    for item in pipe:
        email_text = item[index] 
        assert isinstance(email_text,str)
        match = re.search(header_regex,email_text)
        text = email_text[match.end():]
        yield transformed_item(item,index,text)

def pipe_txt2txt_lower(pipe,index=0):
    ''' make text lower case '''
    for item in pipe:
        text = item[index] 
        assert isinstance(text,str)
        yield transformed_item(item,index,text.lower())

# Punkt sentence detector described here:
#    Kiss, Tibor and Strunk, Jan (2006): Unsupervised Multilingual Sentence
#    Boundary Detection.  Computational Linguistics 32: 485-525.
nltk.download('punkt')
sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
def pipe_txt2list_sentence_splitter(pipe,index=0,split_regex="[^a-zA-Z]+"):
    ''' split text into sentences based on an nltk module '''
    for item in pipe:
        text = item[index] 
        assert isinstance(text,str)
        tokens = sent_detector.tokenize(text)
        yield transformed_item(item,index,tokens)

def pipe_list2txt_flatten(pipe,index=0,split_regex="[^a-zA-Z]+"):
    ''' make a separate data item for every item in a list '''
    for item in pipe:
        tokens = item[index] 
        assert isinstance(tokens,list)
        for subitem in tokens:
            yield transformed_item(item,index,subitem)

def pipe_list2list_porter_stemmer(pipe,index=0):
    ps = PorterStemmer()
    for item in pipe:
        tokens = item[index] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            newtokens.append(ps.stem(token))
        yield transformed_item(item,index,newtokens)

def pipe_list2list_count_cutoff(pipe,cutoff,index=0):
    featureCounter = Counter()
    pipe1,pipe2 = itertools.tee(pipe,2)
    for item in pipe1:
        tokens = item[index] 
        assert isinstance(tokens,list)
        for token in tokens:
            featureCounter[token] += 1
    for item in pipe2:
        tokens = item[index] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if featureCounter[token]>cutoff:
                newtokens.append(token)
            else:
                logger.debug("removing rare word",token)
        yield transformed_item(item,index,newtokens)

def pipe_list2list_remove_short_tokens(pipe,min_token_len,index=0):
    for item in pipe:
        tokens = item[index] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if len(token)>min_token_len:
                newtokens.append(token)
            else:
                logger.debug("removing short word",token)
        yield transformed_item(item,index,newtokens)

def pipe_list2list_remove_stopwords(pipe,index=0,stopwords_path=None):
    stopwords = mallet_stopwords if stopwords_path is None else open(stopwords_path).read().split()
    for item in pipe:
        tokens = item[index] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if token not in stopwords:
                newtokens.append(token)
            else:
                logger.debug("removing stopword",token)
        yield transformed_item(item,index,newtokens)

def pipe_txt2list_tokenize(pipe,index=0,split_regex="[^a-zA-Z]+"):
    ''' split text into tokens based on a regex '''
    for item in pipe:
        text = item[index] 
        assert isinstance(text,str)
        tokens = re.split(split_regex, text)
        assert isinstance(tokens,list)
        yield transformed_item(item,index,tokens)

def pipe_list2list_tokens2bow(pipe,index=0,token_indexer=None):
    ''' uses an indexer 
        to convert each list of tokens to a bag 
        of words representation (example bow corpus = [[(0,1),(1,1)], ...] )'''
    if token_indexer is None:
        token_indexer = Indexer()
    for item in pipe:
        tokens = item[index] 
        assert isinstance(tokens,list)
        bow = Counter()
        for token in tokens:
            assert isinstance(token,str)
            token_index = token_indexer[token]
            bow[token_index] += 1
        yield transformed_item(item,index,list(bow.items()))

def select_index(pipe,index):
    ''' Get a pipe that selects only a single item from the full tuple pipe (generator) '''
    for item in pipe:
        assert len(item)>index
        yield item[index]

def combination_index2sentences(dataset_basedir,dataset_splitdir,index_encoding='utf-8', content_encoding='utf-8'):
    ''' returns a pipe (generator) with items of the form (label,src,content). Content has been tokenized into sentence and token. '''
    pipe = input_labeledindex(dataset_splitdir,encoding=index_encoding)
    pipe = pipe_append_filecontent(pipe,dataset_basedir,index=1,encoding=content_encoding)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,index=2)
    pipe = pipe_txt2list_sentence_splitter(pipe,index=2)
    pipe = pipe_list2txt_flatten(pipe,index=2)
    pipe = pipe_txt2txt_lower(pipe,index=2)
    pipe = pipe_txt2list_tokenize(pipe,index=2)
    return pipe

def combination_index2bow(dataset_basedir,dataset_splitdir,index_encoding='utf-8', content_encoding='utf-8'):
    ''' returns a pipe (generator) with items of the form (label,src,content). Content has been tokenized, filtered, stemmed, counted, and indexed. 
        Also returns a reverse index to allow you to look up words based on their id. '''
    pipe = input_labeledindex(dataset_splitdir,encoding=index_encoding)
    pipe = pipe_append_filecontent(pipe,dataset_basedir,index=1,encoding=content_encoding)
    pipe = pipe_txt2txt_lower(pipe,index=2)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,index=2)
    pipe = pipe_txt2list_tokenize(pipe,index=2)
    pipe = pipe_list2list_remove_short_tokens(pipe,2,index=2)
    pipe = pipe_list2list_remove_stopwords(pipe,index=2)
    pipe = pipe_list2list_porter_stemmer(pipe,index=2)
    pipe = pipe_list2list_count_cutoff(pipe,5,index=2)
    token_indexer = Indexer()
    pipe = pipe_list2list_tokens2bow(pipe,token_indexer=token_indexer,index=2) 
    return list(pipe), token_indexer.reverse_lookup_table() # run full pipe so that indexer is populated

