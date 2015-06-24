#!/usr/bin/python3
import unittest
from python_datautils import pipes

paragraph_txt = '''Call me Ishmael. Some years ago—never mind how long precisely—having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people's hats off—then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.'''

simple_data = {
                "worker":["1","1","1","2","2"],
                "time":["8/24/2013 20:27:15","8/24/2013 20:27:17","8/24/2013 20:27:19","8/24/2013 20:27:21","8/24/2013 20:27:23"],
                "data":["Once upon a time","in a galaxy far, far, away","Whanne that Aprille with his shoures soote","the quick brown fox","jumps over the lazy dog"],
                "annotation":["yes","no","yes","no","no"],
                "instance_id":["doc1","doc2","doc3","doc1","doc2"]
            }

simple_items = list(pipes.input_json("testdata/simple.json")) # only do IO once
def simplepipe():
    return pipes.pass_through(simple_items,copy=True) # return a copy in case they mutate it


class TestUtilityMethods(unittest.TestCase):

    def test_pipe_concat(self):
        items = list(pipes.pipe_concat(simplepipe(),simplepipe(),simplepipe()))
        orig_items = list(simplepipe())
        self.assertEqual(len(items),5*3)
        self.assertEqual(items[0],orig_items[0])
        self.assertEqual(items[5],orig_items[0])
        self.assertEqual(items[10],orig_items[0])
        self.assertEqual(items[3],orig_items[3])
        self.assertEqual(items[8],orig_items[3])
        self.assertEqual(items[13],orig_items[3])

    def test_select_attr(self):
        workers = list(pipes.pipe_select_attr(simplepipe(),"worker"))
        self.assertEqual(workers,["1","1","1","2","2"])


class TestPipeTransformPipes(unittest.TestCase):

    def test_pipe_groupby_attrs(self):
        items = list(pipes.pipe_groupby_attrs(simplepipe(),["worker"]))
        self.assertEqual(len(items), 2)
        self.assertEqual(len(items[0].keys()), 5) 

        w1,w2 = (items[0],items[1]) if items[0]["worker"] == "1" else (items[1],items[0])
        self.assertEqual(w1["annotation"], ["yes","no","yes"])
        self.assertEqual(w2["annotation"], ["no","no"])

        items = list(pipes.pipe_groupby_attrs(simplepipe(),["worker","annotation"]))
        self.assertEqual(len(items[0].keys()), 5) 
        self.assertEqual(len(items), 3) 

    def test_pipe_split_items(self):
        items = list(pipes.pipe_split_items([{"data":"hey","data_0":"hi","data_1":"ho"}], move_attrs=["data_0","data_1"]))
        self.assertEqual(len(items),2)
        orig,new = (items[0],items[1]) if "data" in items[0] else (items[1],items[0])
        self.assertEqual([orig,new],[{"data":"hey"},{"data_0":"hi","data_1":"ho"}])

    def test_pipe_select_attr_list(self):
        items = list(pipes.pipe_select_attr_list(simplepipe(),["worker","annotation"]))
        self.assertEqual(items,[['1', 'yes'], ['1', 'no'], ['1', 'yes'], ['2', 'no'], ['2', 'no']])

    def test_pipe_drop_by_attr_value(self):
        items = list(pipes.pipe_drop_by_attr_value(simplepipe(),"data",pattern=".* a .*",reverse=True))
        self.assertEqual(len(items),2)
        orig_items = list(simplepipe())
        self.assertTrue(items[0]==orig_items[0] or items[0]==orig_items[1])
        self.assertTrue(items[1]==orig_items[0] or items[1]==orig_items[1])

    def test_pipe_list2txt_flatten(self):
        pipe = pipes.pipe_txt2list_tokenize(simplepipe(),"data")
        items = list(pipes.pipe_list2txt_flatten(pipe,"data"))
        self.assertEqual(len(items),26)
        self.assertEqual(items[0]["data"],"Once")
        self.assertEqual(items[25]["data"],"dog")


class TestItemTransformPipes(unittest.TestCase):

    def test_pipe_rename_attr(self):
        items = list(pipes.pipe_rename_attr(simplepipe(),"worker",rename_to="annotator"))
        for item in items:
            self.assertTrue("worker" not in item)
            self.assertTrue("annotator" in item)
        self.assertEqual(items[0]["annotator"], "1")

    def test_pipe_split_attr(self):
        items = list(pipes.pipe_split_attr(simplepipe(),"data",delim=" "))
        for item in items:
            self.assertTrue("data" not in item)
            self.assertTrue("data_0" in item)
        self.assertEqual(items[0]["data_0"],"Once")
        self.assertEqual(items[0]["data_1"],"upon")
        self.assertEqual(items[0]["data_2"],"a")
        self.assertEqual(items[0]["data_3"],"time")

    def test_pipe_retain_attrs(self):
        items = list(pipes.pipe_retain_attrs(simplepipe(),["worker","data"]))
        for item in items:
            self.assertTrue("worker" in item)
            self.assertTrue("data" in item)
            self.assertEqual(len(item.keys()), 2)
        self.assertEqual(items[3]["worker"], "2")

    def test_pipe_combine_attrs(self):
        items = list(pipes.pipe_combine_attrs(simplepipe(),["worker","annotation"],dest_attr="wa",delimiter="|"))
        for item in items:
            self.assertTrue("worker" in item)
            self.assertTrue("annotation" in item)
            self.assertTrue("wa" in item)
            self.assertEqual(len(item.keys()), 6)
        self.assertEqual(items[0]["wa"], "1|yes")
        self.assertEqual(items[2]["wa"], "1|yes")
        self.assertEqual(items[4]["wa"], "2|no")

    def test_pipe_append_filecontent(self):
        items = list(pipes.pipe_append_filecontent([{"datapath":"testdata/simple.csv"}],filepath_attr="datapath",dest_attr="data"))
        self.assertTrue("data" in items[0])
        with open("testdata/simple.csv") as f:
            content = f.read()
            self.assertTrue(items[0]["data"]==content)


class TestAttributePipes(unittest.TestCase):

    def test_pipe_val2val(self):
        items = list(pipes.pipe_val2val(simplepipe(),"worker", lambda x : x))
        # the pass-through lambda function should leave the items unaltered
        self.assertEqual(items,list(simplepipe()))
        
    def test_pipe_txt2list_sentence_splitter(self):
        items = [{"data" : paragraph_txt}]
        items = list(pipes.pipe_txt2list_sentence_splitter(items,attr="data"))
        self.assertEqual(len(items[0]["data"]),8, "8 sentences ought to be detected in the first paragraph of moby dick")

    def test_pipe_val2txt_stringcast(self):
        orig_items = list(simplepipe())
        # change worker ids to ints
        items = list(simplepipe())
        items[0]["worker"] = 1
        items[4]["worker"] = 2
        self.assertNotEqual(items,orig_items)
        # now strip the fireld
        pipe = pipes.pipe_val2txt_stringcast(items,"worker")
        self.assertEqual(list(pipe),orig_items)

    def test_pipe_txt2list_tokenize(self):
        items = list(pipes.pipe_txt2list_tokenize(simplepipe(),"data"))
        self.assertEqual(items[1]["data"], ["in","a","galaxy","far","far","away"])

    def test_pipe_list2list_porter_stemmer(self):
        pipe = pipes.pipe_txt2list_tokenize(simplepipe(),"data")
        items = list(pipes.pipe_list2list_porter_stemmer(pipe,"data"))
        self.assertEqual(items[4]["data"], ["jump","over","the","lazi","dog"])

    def test_pipe_list2list_count_cutoff(self):
        pipe = pipes.pipe_txt2list_tokenize(simplepipe(),"data")
        items = list(pipes.pipe_list2list_count_cutoff(pipe,"data",min_count=2))
        self.assertEqual(items[0]["data"], ["a"])
        self.assertEqual(items[1]["data"], ["a","far","far"])
        self.assertEqual(items[4]["data"], ["the"])

    def test_pipe_list2list_remove_short_tokens(self):
        pipe = pipes.pipe_txt2list_tokenize(simplepipe(),"data")
        items = list(pipes.pipe_list2list_remove_short_tokens(pipe,"data",min_token_len=4))
        self.assertEqual(items[0]["data"], ["Once","upon","time"])
        self.assertEqual(items[1]["data"], ["galaxy","away"])
        self.assertEqual(items[3]["data"], ["quick","brown"])

    def test_pipe_list2list_remove_stopwords(self):
        pipe = pipes.pipe_txt2list_tokenize(simplepipe(),"data")
        items = list(pipes.pipe_list2list_remove_stopwords(pipe,"data"))
        self.assertEqual(items[0]["data"], ["Once","time"])
        self.assertEqual(items[1]["data"], ["galaxy"])
        self.assertEqual(items[3]["data"], ["quick","brown","fox"])

    def test_pipe_list2list_tokens2bow(self):
        indexer = {"once":0,"upon":1,"a":2,"time":3}
        pipe = pipes.pipe_txt2list_tokenize([{"data":"once upon upon a a a time time time time"}],"data")
        items = list(pipes.pipe_list2list_tokens2bow(pipe,"data"))
        self.assertEqual(items[0]["data"], [(0,1),(1,2),(2,3),(3,4)])


class TestInputPipes(unittest.TestCase):

    def test_input_csv(self):
        items = list(pipes.input_csv("testdata/simple.csv"))
        for i in range(5):
            self.assertEqual(items[i]["worker"],simple_data["worker"][i])
            self.assertEqual(items[i]["time"],simple_data["time"][i])
            self.assertEqual(items[i]["data"],simple_data["data"][i])
            self.assertEqual(items[i]["annotation"],simple_data["annotation"][i])
            self.assertEqual(items[i]["instance_id"],simple_data["instance_id"][i])

    def test_input_json(self):
        items = list(pipes.input_json("testdata/simple.json"))
        for i in range(5):
            self.assertEqual(items[i]["worker"],simple_data["worker"][i])
            self.assertEqual(items[i]["time"],simple_data["time"][i])
            self.assertEqual(items[i]["data"],simple_data["data"][i])
            self.assertEqual(items[i]["annotation"],simple_data["annotation"][i])
            self.assertEqual(items[i]["instance_id"],simple_data["instance_id"][i])


class TestStringPipes(unittest.TestCase):

    def test_pipe_txt2val(self):
        items = list(pipes.pipe_txt2val(simplepipe(),"worker", lambda x : x))
        # the pass-through lambda function should leave the items unaltered
        self.assertEqual(items,list(simplepipe()))

    def test_pipe_txt2int_parse_timestamp(self):
        items = list(pipes.pipe_txt2int_parse_timestamp(simplepipe(),"time"))
        self.assertEqual(items[0]["time"],1377376035)
        self.assertEqual(items[4]["time"],1377376043)

    def test_pipe_txt2txt_strip(self):
        orig_items = list(simplepipe())
        # insert whitespace into some values
        items = list(simplepipe())
        items[0]["worker"] = "    1 "
        items[3]["annotation"] = "\tno\n"
        self.assertNotEqual(items,orig_items)
        # now strip the fireld
        pipe = pipes.pipe_txt2txt_strip(items,"worker")
        pipe = pipes.pipe_txt2txt_strip(pipe,"annotation")
        self.assertEqual(list(pipe),orig_items)

    def test_pipe_txt2txt_dictionary_lookup(self):
        orig_items = list(simplepipe())
        # corrupt some values
        items = list(simplepipe())
        items[0]["annotation"] = "sure"
        items[3]["annotation"] = "no way!"
        self.assertNotEqual(items,orig_items)
        # now map them back with a lookup dict
        lookup = {"sure":"yes","no way!":"no"}
        pipe = pipes.pipe_txt2txt_dictionary_lookup(items,"annotation",lookup,word_delim="_")
        self.assertEqual(list(pipe),orig_items)

    def test_pipe_txt2txt_emailheader_stripper(self):
        orig_items = list(simplepipe())
        # add email headers to data
        items = list(simplepipe())
        items[0]["data"] = "subject:memo\nsender:vader\nrecipients:luke,leia\n\nOnce upon a time"
        self.assertNotEqual(items,orig_items)
        # now map them back with a lookup dict
        pipe = pipes.pipe_txt2txt_emailheader_stripper(items,"data")
        self.assertEqual(list(pipe),orig_items)

    def test_pipe_txt2txt_sub(self):
        orig_items = list(simplepipe())
        # corrups some data items
        items = list(simplepipe())
        items[0]["data"] = "Once upon a space"
        items[1]["data"] = "in a galaxy up, up, away"
        self.assertNotEqual(items,orig_items)
        # now map them back with substitutions
        pipe = pipes.pipe_txt2txt_sub(items,"data",pattern="up,",sub="far,")
        pipe = pipes.pipe_txt2txt_sub(pipe,"data",pattern="space",sub="time")
        self.assertEqual(list(pipe),orig_items)

    def test_pipe_txt2txt_lower(self):
        orig_items = list(simplepipe())
        # corrups some data items
        items = list(simplepipe())
        items[0]["data"] = "oNCE UPON A TIME"
        items[1]["data"] = "iN a gAlaXy Far, fAr, away"
        self.assertNotEqual(items,orig_items)
        # now map them and the truth to lowercase
        loweritems = list(pipes.pipe_txt2txt_lower(items,"data"))
        lowerorig_items = list(pipes.pipe_txt2txt_lower(orig_items,"data"))
        self.assertEqual(loweritems,lowerorig_items)

if __name__=="__main__":
    unittest.main()
