from collections import Counter
import numpy as np
import random
from pprint import pprint
import json
import csv
import argparse
from os import path
import sys

def votemap_of(pipe,attr,source_attr):
    votemap = {}
    for item in pipe:

        # aggregate annotation votes per source
        if attr in item:
            src, annotation = item[source_attr], item[attr]
            votemap[src] = votemap.get(src, Counter()) # ensure counter exists for key
            votemap[src][annotation] += 1
    return votemap

def pipe_append_majority_label(pipe,attr,source_attr="source",dest_attr="label",min_count=3):
    ''' determine the majority vote annotation for each set of documents sharing a common source, 
        and attach that value to each object. Labels are appended only for sets with >= min_count votes. '''

    def most_common(counter):
        _,majority_cnt = counter.most_common()[0]
        majority = set()
        for value,cnt in counter.most_common():
            if cnt==majority_cnt:
                majority.add(value)
            else:
                break
        logger.warn("choosing randomly among %d options \n"%len(majority))
        return random.sample(majority,1)[0]

    # we'll need to go through the pipe twice. 
    # unfortunately, this means we need to cache the pipe
    pipe = list(pipe)

    votemap = votemap_of(pipe,attr,source_attr)
    # determine majority vote for each source
    majority_vote = {}
    for src,votes in votemap.items():
        if sum(votes.values())>=min_count:
            majority_vote[src] = most_common(votes)

    # now transform original instances
    for item in pipe:
        # add a majority vote label attribute (if available)
        if dest_attr not in item and item[source_attr] in majority_vote:
            item[dest_attr] = majority_vote.get(item[source_attr],None)
        yield item

def pipe_append_mean_value(pipe,attr="annotation",source_attr="source",dest_attr="annotation_mean"):
    # we'll need to go through the pipe twice. 
    # unfortunately, this means we need to cache the pipe
    pipe = list(pipe)

    votemap = votemap_of(pipe,attr,source_attr)
    # determine majority vote for each source
    means = {}
    for src,votes in votemap.items():
        #print("src",src,"values",[v for v in votes.elements()],"mean",np.mean([float(v) for v in votes.elements()]))
        means[src] = np.mean([float(v) for v in votes.elements()])

    # now transform original instances
    for item in pipe:
        # add a majority vote label attribute (if available)
        if dest_attr not in item and item[source_attr] in means:
            item[dest_attr] = means.get(item[source_attr],None)
        yield item

def pipe_threshold_value(pipe,attr="annotation_mean",levels=[0.3,0.6],names=["low","medium","high"]):
    levels = levels + [float('inf')]
    assert len(levels)==len(names), "there must be n-1 levels, where n is the number of names."
    for item in pipe:
        if attr in item:
            val = float(item[attr])
            for level,name in zip(levels,names):
                if val < level:
                    item[attr] = name
                    break
        yield item
