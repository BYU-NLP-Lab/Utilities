#!/usr/bin/python3
import numpy as np
import calendar
import dateutil.parser
import csv
import chardet
import itertools
import nltk.data
from nltk.stem.porter import PorterStemmer
import re
from collections import Counter
import os
import sys
import json
# ensure cwd is scanned for modules in case we are running from there but 
# haven't taken the trouble to put python_datutils on our PYTHONPATH
sys.path.append(os.path.dirname(os.getcwd()))
from python_datautils import annotation_pipes
from python_datautils.data_structures import Indexer
import pkg_resources
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
def input_csv(csv_path,encoding=None,delimiter=','):
    ''' A pipe (generator) that yields a list of dicts, one for 
        each row of a csv file '''
    encoding = autodetect_encoding(csv_path,encoding)
    with open(csv_path,encoding=encoding) as csv_file:
        for obj in csv.DictReader(csv_file, delimiter=delimiter):
            yield obj

def input_json(json_path,encoding=None):
    ''' A pipe (generator) that yields a list of dicts, one for each 
        entry in a json file'''
    encoding = autodetect_encoding(json_path,encoding)
    with open(json_path,encoding=encoding) as json_file:
        for obj in json.load(json_file):
            yield obj

def input_index(dataset_splitdir,attr,label_attr="label",encoding=None):
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
                    yield {label_attr:index_filename, attr:datafile.strip()}

###########################################################################
# Utility functions
###########################################################################

def pass_through(pipe,copy=False):
    for item in pipe:
        assert isinstance(item,dict)
        yield item.copy() if copy else item

def pipe_concat(*pipes,copy=False):
    for pipe in pipes:
        for item in pass_through(pipe,copy=copy):
            yield item

def pipe_select_attr(pipe,attr,default_value=None,copy=False):
    ''' Transform each dict into a list formed by indexing into the dict with each attr in turn. '''
    for item in pass_through(pipe,copy=copy):
        yield item.get(attr,default_value)

###########################################################################
# Stream Transform Pipes (changes the number of items)
###########################################################################

def pipe_groupby_attrs(pipe,attrs,default_value=None,copy=False):
    ''' Combine all dicts with same values for the set groupby_attrs. (other values are concatenated in a list)'''
    assert isinstance(attrs,list) or isinstance(attrs,tuple), "attrs must be a list or tuple"
    groupmap = {}
    for item in pass_through(pipe,copy=copy):
        val = tuple(item.get(v,default_value) for v in attrs)
        # find (or create) the group representative
        if val not in groupmap:
            groupmap[val] = dict(item)
        else:
            groupitem = groupmap[val]
            # concatenate additional attribute values onto the group rep
            for attr in item:
                assert attr in groupitem, "all grouped items must have the same attributes"
                # merge groupby values
                if attr in attrs:
                    continue 
                # ensure non-groupby values are lists
                elif not isinstance(groupitem[attr],list):
                    groupitem[attr] = [groupitem[attr]]
                groupitem[attr].append(item[attr])
    # yield results
    for item in groupmap.values():
        yield item

def pipe_split_items(pipe,move_attrs,copy_attrs=[],copy=False):
    ''' For every item, remove the indicated attributes (move_attrs) and add them to a new item.
        If copy_attrs are indicated, then copy these to the new item without removing them 
        from the old. Items that contain no move_attrs are not affect (no new item is created).'''
    assert isinstance(move_attrs,list) or isinstance(move_attrs,tuple), "move_attrs must be a tuple or list: %s"%move_attrs
    assert isinstance(copy_attrs,list) or isinstance(copy_attrs,tuple), "copy_attrs must be a tuple or list: %s"%copy_attrs
    for item in pass_through(pipe,copy=copy):
        # yield non-trivial newitem
        if len(set(move_attrs).intersection(item.keys()))>0:
            yield {k:v for k,v in item.items() if k in move_attrs or k in copy_attrs}
        # yield original item (minus moved fields)
        yield {k:v for k,v in item.items() if k not in move_attrs}

def pipe_select_attr_list(pipe,attrs,default_value=None,copy=False):
    ''' Transform each dict into a list formed by indexing into the dict with each attr in turn. '''
    assert isinstance(attrs,list) or isinstance(attrs,tuple), "attrs must be a tuple or list: %s"%move_attrs
    for item in pass_through(pipe,copy=copy):
        arr = []
        for attr in attrs:
            arr.append(item.get(attr,default_value))
        yield arr

def pipe_drop_by_regex(pipe,attr,pattern,reverse=False,copy=False):
    ''' drop items whose indicated attribute has a 
        value (cast to string) that matches the pattern (using re.match) '''
    for item in pass_through(pipe,copy=copy):
        dropitem = attr in item and re.match(pattern,item[attr])
        if reverse:
            dropitem = not dropitem
        if not dropitem:
            yield item

def pipe_list2txt_flatten(pipe,attr,copy=False):
    ''' make a separate data item for every item in a list. 
        if a list is empty, return a single empty string. '''
    for item in pass_through(pipe,copy=copy):
        tokens = item[attr] 
        assert isinstance(tokens,list)
        if len(tokens)==0:
            # empty lists shouldn't entirely disappear
            yield transformed_item(item,attr,"") 
        for subitem in tokens:
            yield transformed_item(item,attr,subitem)

###########################################################################
# Item Transform Pipes  (changes the number of item attributes)
###########################################################################

def pipe_drop_attr_by_regex(pipe,attr,pattern,reverse=False,copy=False):
    ''' drop attributes whose value (cast to string) matches the pattern (using re.match) '''
    for item in pass_through(pipe,copy=copy):
        dropattr = attr in item and re.match(pattern,item[attr])
        if reverse:
            dropattr = not dropattr
        if dropattr:
            del item[attr]
        yield item

def pipe_rename_attr(pipe,attr,rename_to,copy=False):
    ''' Transform each dict into a list formed by indexing into the dict with each attr in turn. '''
    for item in pass_through(pipe,copy=copy):
        if attr in item:
            item[rename_to] = item[attr]
            del item[attr]
        yield item

def pipe_split_attr(pipe,attr,delim=" ",copy=False):
    ''' splits an attribute value on the delimiter, assigning each 
        part its own attribute, "attr_1", "attr_2", etc. '''
    for item in pass_through(pipe,copy=copy):
        if attr in item:
            # split out attr
            parts = item[attr].split(delim)
            # remove old attribute
            del item[attr]
            # add new numbered attributes (even if there is only one, for consistency)
            for i,part in enumerate(parts):
                item["%s_%d"%(attr,i)] = part
        yield item

def pipe_retain_attrs(pipe,attrs,copy=False):
    ''' retain only the indicated attributes '''
    for item in pass_through(pipe,copy=copy):
        yield {k:v for k,v in item.items() if k in attrs}

def pipe_combine_attrs(pipe,attrs,dest_attr="data",delimiter="|",copy=False):
    for item in pass_through(pipe,copy=copy):
        vals = [item[a] for a in attrs if a in item]
        if vals:
            item[dest_attr] = delimiter.join(vals)
        yield item

def pipe_append_filecontent(pipe,filepath_attr,dest_attr="data",basedir='',encoding=None,copy=False):
    ''' Append file content to the end of each tuple, reading to filepaths at position "index" '''
    for item in pass_through(pipe,copy=copy):
        filepath = item[filepath_attr] 
        assert isinstance(filepath,str), "%s is not a file path string "%filepath
        try:
            encoding = autodetect_encoding(os.path.join(basedir,filepath),encoding)
            with open(os.path.join(basedir,filepath),encoding=encoding) as datafile:
                yield transformed_item(item,dest_attr,datafile.read())
        except Exception as e:
            logger.warn("unable to open file %s. Skipping. %s" % (os.path.join(basedir,filepath),e))

def pipe_append_mean_value(pipe,attr,source_attr=None,dest_attr=None):
    ''' Group items by source_attr and append a mean value. If source 
        is None, appends the global mean value '''
    dest_attr = dest_attr if dest_attr else "%s_mean"%attr # default dest_attr add a _mean suffix to attr
    def source_val(item,source_attr):
        if source_attr is None or source_attr not in item:
            return None
        return item[source_attr]
    # we'll need to go through the pipe twice. 
    # unfortunately, this means we need to cache the pipe
    pipe = list(pipe)

    votemap = annotation_pipes.votemap_of(pipe,attr,source_attr)
    # determine majority vote for each source
    means = {}
    for src,votes in votemap.items():
        #print("src",src,"values",[v for v in votes.elements()],"mean",np.mean([float(v) for v in votes.elements()]))
        means[src] = np.mean([float(v) for v in votes.elements()])

    # now transform original instances
    for item in pipe:
        # add a majority vote label attribute (if available)
        if source_attr is None or (source_attr in item and item[source_attr] in means):
            item[dest_attr] = means[source_val(item,source_attr)]
        yield item

def pipe_append_thresholded_value(pipe,attr,dest_attr,levels=[0.3,0.6],names=["low","medium","high"],copy=False):
    ''' threshold the indicated attribute value (cast to a float) into several buckets.'''
    levels = levels + [float('inf')]
    assert len(levels)==len(names), "there must be n-1 levels, where n is the number of names."
    for item in pass_through(pipe,copy=copy):
        if attr in item:
            val = float(item[attr])
            for level,name in zip(levels,names):
                if val < level:
                    item[dest_attr] = name
                    break
        yield item

###########################################################################
# Attribute Value Transform Pipes  (changes attribute values)
###########################################################################

def pipe_val2val(pipe,attr,transform,copy=False):
    ''' apply the transform function to indicated attribute 
        value of each item in the pipe (if it has the indicated attribute) '''
    for item in pass_through(pipe,copy=copy):
        if attr in item:
            item[attr] = transform(item[attr])
        yield item

# Punkt sentence detector described here:
#    Kiss, Tibor and Strunk, Jan (2006): Unsupervised Multilingual Sentence
#    Boundary Detection.  Computational Linguistics 32: 485-525.
nltk.download('punkt',quiet=True)
sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
def pipe_txt2list_sentence_splitter(pipe,attr="data",copy=False):
    ''' split text into sentences based on an nltk module '''
    return pipe_txt2val(pipe,attr,lambda txt:sent_detector.tokenize(txt))

def pipe_val2txt_stringcast(pipe,attr="data",copy=False):
    ''' Cast an attribute value to a string. '''
    return pipe_txt2val(pipe,attr,lambda x:x)

def pipe_list2list_porter_stemmer(pipe,attr="data",copy=False):
    ps = PorterStemmer()
    for item in pass_through(pipe,copy=copy):
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            newtokens.append(ps.stem(token))
        yield transformed_item(item,attr,newtokens)

def pipe_list2list_count_cutoff(pipe,attr,min_count=5,copy=False):
    featureCounter = Counter()
    pipe1,pipe2 = itertools.tee(pipe,2)
    for item in pass_through(pipe1,copy=copy):
        tokens = item[attr] 
        assert isinstance(tokens,list)
        for token in tokens:
            featureCounter[token] += 1
    for item in pass_through(pipe2,copy=copy):
        tokens = item[attr] 
        assert isinstance(tokens,list)
        newtokens = []
        for token in tokens:
            assert isinstance(token,str)
            if featureCounter[token]>=min_count:
                newtokens.append(token)
            else:
                logger.debug("removing rare word",token)
        yield transformed_item(item,attr,newtokens)

def pipe_list2list_remove_short_tokens(pipe,attr,min_token_len=2,copy=False):
    for item in pass_through(pipe,copy=copy):
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

def pipe_list2list_remove_stopwords(pipe,attr="data",stopwords_path=None,encoding=None,copy=False):
    if stopwords_path is not None:
        encoding = autodetect_encoding(stopwords_path,encoding)
    stopwords = mallet_stopwords if stopwords_path is None else open(stopwords_path,encoding=encoding).readlines()
    for item in pass_through(pipe,copy=copy):
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

def pipe_txt2list_tokenize(pipe,attr="data",split_regex="[^a-zA-Z]+",copy=False):
    ''' split text into tokens based on a regex '''
    return pipe_txt2val(pipe,attr,lambda txt: re.split(split_regex, txt))

def pipe_list2list_tokens2bow(pipe,attr="data",token_indexer=None,copy=False):
    ''' uses an indexer 
        to convert each list of tokens to a bag 
        of words representation (example bow corpus = [[(0,1),(1,1)], ...] )'''
    if token_indexer is None:
        token_indexer = Indexer()
    for item in pass_through(pipe,copy=copy):
        tokens = item[attr] 
        assert isinstance(tokens,list)
        bow = Counter()
        for token in tokens:
            assert isinstance(token,str)
            token_index = token_indexer[token]
            bow[token_index] += 1
        yield transformed_item(item,attr,list(bow.items()))

###########################################################################
# String Transform Pipes 
###########################################################################

def pipe_txt2val(pipe,attr,transform,copy=False):
    ''' apply the transform function to indicated attribute 
        value of each item (cast to string) in the pipe (if it has the indicated attribute) '''
    return pipe_val2val(pipe,attr,lambda x:transform(str(x)))

def pipe_txt2int_parse_timestamp(pipe,attr,copy=False):
    ''' assume that a string encodes some kind of date/time representation
        parse it into a timestamp using python's dateutil.parser '''
    return pipe_txt2val(pipe,attr, 
        lambda txt: calendar.timegm(dateutil.parser.parse(txt).timetuple()) )

def pipe_txt2txt_strip(pipe,attr,copy=False):
    ''' call strip on the value (cast to string) of the indicated attribute '''
    return pipe_txt2val(pipe,attr, lambda x: x.strip())

def pipe_txt2txt_dictionary_lookup(pipe,attr,lookup,word_delim=" ",copy=False):
    ''' transform a text field by substituting each 'word' in the text field with 
        the value obtained from a lookup table '''
    return pipe_txt2val(pipe,attr, 
        lambda txt: word_delim.join([lookup.get(word.lower(),word) for word in txt.split(word_delim)]))

def pipe_txt2txt_emailheader_stripper(pipe,attr="data",header_regex="\n\n|\r\r|\n\r\n\r",copy=False):
    ''' remove the email header from text (look for empty line delimiter)'''
    def remove_email_header(txt):
        match = re.search(header_regex,txt)
        return txt[match.end():] if match else txt
    return pipe_txt2val(pipe,attr,remove_email_header)

def pipe_txt2txt_sub(pipe,attr="data",pattern="before",sub="after",copy=False):
    ''' apply re.sub functionality to an attribute (cast to a string) '''
    return pipe_txt2val(pipe,attr,lambda x: re.sub(pattern,sub,x))

def pipe_txt2txt_lower(pipe,attr="data",copy=False):
    ''' make text lower case '''
    return pipe_txt2val(pipe,attr,lambda x: x.lower())

###########################################################################
# Combos
###########################################################################
def combination_filepath2sentences(pipe,dataset_basedir,filepath_attr="datapath",filecontent_attr="data",content_encoding=None,copy=False):
    pipe = pipe_append_filecontent(pipe,dataset_basedir, filepath_attr=filepath_attr, dest_attr=filecontent_attr, encoding=content_encoding,copy=copy)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_txt2list_sentence_splitter(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2txt_flatten(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_txt2txt_lower(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_txt2list_tokenize(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2list_remove_short_tokens(pipe,1,attr=filecontent_attr,copy=copy) # strip empty strings
    return pipe

def combination_json2sentences(dataset_basedir,json_path,filepath_attr="datapath",data_attr="data",index_encoding=None,content_encoding=None,copy=False):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content is indexed by [sentence][token]. '''
    pipe = input_json(json_path,encoding=index_encoding,copy=copy)
    pipe = pipe_groupby_attrs(pipe,attrs=["source","datapath","label","labelobserved"],copy=copy)
    return combination_filepath2sentences(pipe,dataset_basedir,filepath_attr=filepath_attr,filecontent_attr=data_attr,content_encoding=content_encoding,copy=copy)

def combination_index2sentences(dataset_basedir,dataset_splitdir,label_attr="label",filepath_attr="source",data_attr="data",index_encoding=None,content_encoding=None,copy=False):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content is indexed by [sentence][token]. '''
    pipe = input_index(dataset_splitdir,label_attr=label_attr,filepath_attr=filepath_attr,encoding=index_encoding,copy=copy)
    pipe = combination_filepath2sentences(pipe,dataset_basedir,filepath_attr=filepath_attr,filecontent_attr=data_attr,content_encoding=content_encoding,copy=copy)
    return pipe

def combination_index2bow(dataset_basedir,dataset_splitdir,filepath_attr="source",filecontent_attr="data",label_attr="label",index_encoding=None, content_encoding=None,copy=False):
    ''' returns a pipe (generator) of dicts {label_attr:'', filepath_attr:'', data_attr:[[]]}. Where the data_attr content has been tokenized, filtered, stemmed, counted, and indexed. 
        Also returns a reverse index to allow you to look up words based on their id. '''
    pipe = input_index(dataset_splitdir,filepath_attr=filepath_attr,label_attr=label_attr,encoding=index_encoding,copy=copy)
    pipe = pipe_append_filecontent(pipe,dataset_basedir,filepath_attr=filepath_attr,dest_attr=filecontent_attr,encoding=content_encoding,copy=copy)
    pipe = pipe_txt2txt_lower(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_txt2txt_emailheader_stripper(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_txt2list_tokenize(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2list_remove_short_tokens(pipe,3,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2list_remove_stopwords(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2list_porter_stemmer(pipe,attr=filecontent_attr,copy=copy)
    pipe = pipe_list2list_count_cutoff(pipe,5,attr=filecontent_attr,copy=copy)
    token_indexer = Indexer()
    pipe = pipe_list2list_tokens2bow(pipe,token_indexer=token_indexer,attr=filecontent_attr,copy=copy)
    return list(pipe), token_indexer.reverse_lookup_table() # run full pipe so that indexer is populated

