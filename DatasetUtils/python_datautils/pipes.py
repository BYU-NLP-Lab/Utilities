#!/usr/bin/python3
import chardet
import itertools
import nltk.data
from nltk.stem.porter import PorterStemmer
import re
from collections import Counter
import os
import pkg_resources
from data_structures import Indexer
import sys
import json
# ensure cwd is scanned for modules in case we are running from there but 
# haven't taken the trouble to put python_datutils on our PYTHONPATH
sys.path.append(os.path.dirname(os.getcwd()))
mallet_stopwords = set(str(pkg_resources.resource_string('python_datautils','mallet_stopwords.txt'),encoding='utf-8').split())
import logging
logger = logging.getLogger("pipes")

def transformed_item(item,attr,newvalue):
    newitem = dict(item)
    newitem[attr] = newvalue
    return newitem

def autodetect_encoding(path,encoding=None):
    if encoding is not None:
        return encoding
    with open(path,'rb') as f:
        return chardet.detect(f.read())['encoding']

###########################################################################
# Inputs
###########################################################################
def input_json(json_path,encoding=None):
    ''' A pipe (generator) that yields a list of dicts, one for each 
        entry in a json file'''
    encoding = autodetect_encoding(json_path,encoding)
    with open(json_path,encoding=encoding) as json_file:
        for obj in json.load(json_file):
            yield obj

def input_index(dataset_splitdir,filepath_attr="src",label_attr="label",encoding=None):
    ''' A pipe (generator) that yields (label,filepath), one for each 
        entry in index files (whose name are their label) 
        found in the dataset_splitdir '''
    count = 0
    print("splitdir",dataset_splitdir)
    for root, dirs, files in os.walk(dataset_splitdir):
        for index_filename in files:
            encoding = autodetect_encoding(os.path.join(root,index_filename),encoding)
            with open(os.path.join(root,index_filename),encoding=encoding) as index_file:
                for datafile in index_file:
                    count += 1
                    if count%100==0:
                        logger.info("processing document %d"%count)
                    #print(datafile.strip())
                    yield {label_attr:index_filename, filepath_attr:datafile.strip()}

###########################################################################
# Pipes
###########################################################################
def pipe_select_attr(pipe,attr,default_value=None):
    ''' Transform each dict into a list formed by indexing into the dict with each attr in turn. '''
    for item in pipe:
        assert isinstance(item,dict)
        yield item.get(attr,default_value)

def pipe_groupby_attrs(pipe,groupby_attrs,default_value=None):
    ''' Combine all dicts with same values for the set groupby_attrs. (other values are concatenated in a list)'''
    groupby_attrs = list(groupby_attrs)
    groupmap = {}
    for item in pipe:
        assert isinstance(item,dict)
        val = tuple(item.get(v,default_value) for v in groupby_attrs)
        # find (or create) the group representative
        if val not in groupmap:
            groupmap[val] = dict(item)
        else:
            groupitem = groupmap[val]
            # concatenate additional attribute values onto the group rep
            for attr in item:
                assert attr in groupitem, "all grouped items must have the same attributes"
                # merge groupby values
                if attr in groupby_attrs:
                    continue 
                # ensure non-groupby values are lists
                elif not isinstance(groupitem[attr],list):
                    groupitem[attr] = [groupitem[attr]]
                groupitem[attr].append(item[attr])
    # yield results
    for item in groupmap.values():
        yield item

def pipe_select_attr_list(pipe,attrs,default_value=None):
    ''' Transform each dict into a list formed by indexing into the dict with each attr in turn. '''
    for item in pipe:
        assert isinstance(item,dict)
        arr = []
        for attr in attrs:
            arr.append(item.get(attr,default_value))
        yield arr

def pipe_append_filecontent(pipe,basedir,encoding=None,filepath_attr="src",filecontent_attr="data"):
    ''' Append file content to the end of each tuple, reading to filepaths at position "index" '''
    for item in pipe:
        assert isinstance(item,dict)
        filepath = item[filepath_attr] 
        assert isinstance(filepath,str), "%s is not a file path string "%filepath
        try:
            encoding = autodetect_encoding(os.path.join(basedir,filepath),encoding)
            with open(os.path.join(basedir,filepath),encoding=encoding) as datafile:
                yield transformed_item(item,filecontent_attr,datafile.read())
        except Exception as e:
            logger.warn("unable to open file %s. Skipping. %s" % (os.path.join(basedir,filepath),e))

def pipe_txt2txt_emailheader_stripper(pipe,header_regex="\n\n|\r\r|\n\r\n\r",attr="data"):
    ''' remove the email header from text (look for empty line delimiter)'''
    for item in pipe:
        assert isinstance(item,dict)
        email_text = item[attr] 
        assert isinstance(email_text,str)
        match = re.search(header_regex,email_text)
        text = email_text[match.end():]
        yield transformed_item(item,attr,text)

def pipe_txt2txt_lower(pipe,attr="data"):
    ''' make text lower case '''
    for item in pipe:
        assert isinstance(item,dict)
        text = item[attr] 
        assert isinstance(text,str)
        yield transformed_item(item,attr,text.lower())

# Punkt sentence detector described here:
#    Kiss, Tibor and Strunk, Jan (2006): Unsupervised Multilingual Sentence
#    Boundary Detection.  Computational Linguistics 32: 485-525.
nltk.download('punkt')
sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
def pipe_txt2list_sentence_splitter(pipe,attr="data",split_regex="[^a-zA-Z]+"):
    ''' split text into sentences based on an nltk module '''
    for item in pipe:
        assert isinstance(item,dict)
        text = item[attr] 
        assert isinstance(text,str)
        tokens = sent_detector.tokenize(text)
        yield transformed_item(item,attr,tokens)

def pipe_list2txt_flatten(pipe,attr="data",split_regex="[^a-zA-Z]+"):
    ''' make a separate data item for every item in a list. 
        if a list is empty, return a single empty string. '''
    for item in pipe:
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        if len(tokens)==0:
            # empty lists shouldn't entirely disappear
            yield transformed_item(item,attr,"") 
        for subitem in tokens:
            yield transformed_item(item,attr,subitem)

def pipe_list2list_porter_stemmer(pipe,attr="data"):
    ps = PorterStemmer()
    for item in pipe:
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            newtokens.append(ps.stem(token))
        yield transformed_item(item,attr,newtokens)

def pipe_list2list_count_cutoff(pipe,cutoff,attr="data"):
    featureCounter = Counter()
    pipe1,pipe2 = itertools.tee(pipe,2)
    for item in pipe1:
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        for token in tokens:
            featureCounter[token] += 1
    for item in pipe2:
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if featureCounter[token]>cutoff:
                newtokens.append(token)
            else:
                logger.debug("removing rare word",token)
        yield transformed_item(item,attr,newtokens)

def pipe_list2list_remove_short_tokens(pipe,min_token_len,attr="data"):
    for item in pipe:
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if len(token)>=min_token_len:
                newtokens.append(token)
            else:
                logger.debug("removing short word",token)
        yield transformed_item(item,attr,newtokens)

def pipe_list2list_remove_stopwords(pipe,attr="data",stopwords_path=None,encoding=None):
    if stopwords_path is not None:
        encoding = autodetect_encoding(stopwords_path,encoding)
    stopwords = mallet_stopwords if stopwords_path is None else open(stopwords_path,encoding=encoding).readlines()
    for item in pipe:
        assert isinstance(item,dict)
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if token not in stopwords:
                newtokens.append(token)
            else:
                logger.debug("removing stopword",token)
        yield transformed_item(item,attr,newtokens)

def pipe_txt2list_tokenize(pipe,attr="data",split_regex="[^a-zA-Z]+"):
    ''' split text into tokens based on a regex '''
    for item in pipe:
        assert isinstance(item,dict)
        text = item[attr] 
        assert isinstance(text,str)
        tokens = re.split(split_regex, text)
        assert isinstance(tokens,list)
        yield transformed_item(item,attr,tokens)

def pipe_list2list_tokens2bow(pipe,attr="data",token_indexer=None):
    ''' uses an indexer 
        to convert each list of tokens to a bag 
        of words representation (example bow corpus = [[(0,1),(1,1)], ...] )'''
    if token_indexer is None:
        token_indexer = Indexer()
    for item in pipe:
        assert isinstance(item,dict)
        tokens = item[attr] 
        assert isinstance(tokens,list)
        bow = Counter()
        for token in tokens:
            assert isinstance(token,str)
            token_index = token_indexer[token]
            bow[token_index] += 1
        yield transformed_item(item,attr,list(bow.items()))

###########################################################################
# Combos
###########################################################################
def combination_filepath2sentences(pipe,dataset_basedir,filepath_attr="datapath",filecontent_attr="data",content_encoding=None):
    pipe = pipe_append_filecontent(pipe,dataset_basedir, filepath_attr=filepath_attr, filecontent_attr=filecontent_attr, encoding=content_encoding)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,attr=filecontent_attr)
    pipe = pipe_txt2list_sentence_splitter(pipe,attr=filecontent_attr)
    pipe = pipe_list2txt_flatten(pipe,attr=filecontent_attr)
    pipe = pipe_txt2txt_lower(pipe,attr=filecontent_attr)
    pipe = pipe_txt2list_tokenize(pipe,attr=filecontent_attr)
    pipe = pipe_list2list_remove_short_tokens(pipe,1,attr=filecontent_attr) # strip empty strings
    return pipe

def combination_json2sentences(dataset_basedir,json_path,filepath_attr="datapath",data_attr="data",index_encoding=None,content_encoding=None):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content is indexed by [sentence][token]. '''
    pipe = input_json(json_path,encoding=index_encoding)
    pipe = pipe_groupby_attrs(pipe,groupby_attrs=["source","datapath","label","labelobserved"])
    return combination_filepath2sentences(pipe,dataset_basedir,filepath_attr=filepath_attr,filecontent_attr=data_attr,content_encoding=content_encoding)

def combination_index2sentences(dataset_basedir,dataset_splitdir,label_attr="label",filepath_attr="source",data_attr="data",index_encoding=None,content_encoding=None):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content is indexed by [sentence][token]. '''
    pipe = input_index(dataset_splitdir,label_attr=label_attr,filepath_attr=filepath_attr,encoding=index_encoding)
    pipe = combination_filepath2sentences(pipe,dataset_basedir,filepath_attr=filepath_attr,filecontent_attr=data_attr,content_encoding=content_encoding)
    return pipe

def combination_index2bow(dataset_basedir,dataset_splitdir,filepath_attr="source",filecontent_attr="data",label_attr="label",index_encoding=None, content_encoding=None):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content has been tokenized, filtered, stemmed, counted, and indexed. 
        Also returns a reverse index to allow you to look up words based on their id. '''
    pipe = input_index(dataset_splitdir,filepath_attr=filepath_attr,label_attr=label_attr,encoding=index_encoding)
    pipe = pipe_append_filecontent(pipe,dataset_basedir,filepath_attr=filepath_attr,filecontent_attr=filecontent_attr,encoding=content_encoding)
    pipe = pipe_txt2txt_lower(pipe,attr=filecontent_attr)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,attr=filecontent_attr)
    pipe = pipe_txt2list_tokenize(pipe,attr=filecontent_attr)
    pipe = pipe_list2list_remove_short_tokens(pipe,3,attr=filecontent_attr)
    pipe = pipe_list2list_remove_stopwords(pipe,attr=filecontent_attr)
    pipe = pipe_list2list_porter_stemmer(pipe,attr=filecontent_attr)
    pipe = pipe_list2list_count_cutoff(pipe,5,attr=filecontent_attr)
    token_indexer = Indexer()
    pipe = pipe_list2list_tokens2bow(pipe,token_indexer=token_indexer,attr=filecontent_attr) 
    return list(pipe), token_indexer.reverse_lookup_table() # run full pipe so that indexer is populated

