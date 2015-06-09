#!/bin/sh

TMPDIR=enwiki_word2vec
WIKIDUMP=$TMPDIR/enwiki-latest-pages-articles.xml.bz2
LINES=$TMPDIR/enwiki-lines.txt
MODEL=$TMPDIR/enwiki-word2vec.model

python wiki2lines.py $WIKIDUMP $LINES
bzip2 $LINES 
python lines2vec.py "$LINES".bz2 $MODEL
