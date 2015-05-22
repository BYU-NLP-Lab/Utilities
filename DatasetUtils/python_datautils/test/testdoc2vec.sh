#!/bin/sh

# generate models on newsgroups-tiny
pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/tiny_set --method=LDA --outdir=/tmp/lda-tiny; popd
#pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/tiny_set --method=WORD2VEC --outdir=/tmp/w2v-tiny; popd
#pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/tiny_set --method=PARAVEC --outdir=/tmp/d2v-tiny; popd
#
## generate models on newsgroups-full
#pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/full_set/all --method=LDA --out=/tmp/lda-full; popd
#pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/full_set/all --method=WORD2VEC --out=/tmp/w2v-full; popd
#pushd ..; ./doc2vec.py --dataset-basedir=/aml/data/newsgroups --dataset-split /aml/data/newsgroups/indices/full_set/all --method=PARAVEC --out=/tmp/d2v-full; popd

