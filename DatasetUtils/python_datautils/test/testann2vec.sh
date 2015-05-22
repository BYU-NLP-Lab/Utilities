#!/bin/sh

pushd ..; ./ann2vec.py --dataset-basedir=/aml/data/newsgroups --json-annotation-stream /aml/data/plf1/cfgroups/cfgroups1000-tiny.json --outdir=/tmp/ann-d2v-tiny; popd
#pushd ..; ./ann2vec.py --dataset-basedir=/aml/data/newsgroups --json-annotation-stream /aml/data/plf1/cfgroups/cfgroups1000.json --outdir=/tmp/ann-d2v-tiny; popd
