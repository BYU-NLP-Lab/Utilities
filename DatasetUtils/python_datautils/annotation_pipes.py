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
            src = item[source_attr] if source_attr in item else None
            annotation = item[attr]
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

