#!/bin/sh

TMPDIR=enwiki_word2vec
WIKIDUMP=$TMPDIR/enwiki-latest-pages-articles.xml.bz2
LINEFILE=$TMPDIR/enwiki-lines.txt
MODEL=$TMPDIR/enwiki-word2vec.model

python3 wiki2lines.py $WIKIDUMP $LINEFILE
bzip2 $LINEFILE 
python3 lines2vec.py "$LINEFILE".bz2 $MODEL
