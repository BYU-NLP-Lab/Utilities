def flatten(item):
    ''' iterate through a nested list structure '''
    if type(item) is list or type(item) is tuple or type(item) is set:
        for subitem in item:
            for part in flatten(subitem):
                yield part
    else:
        yield item

class Indexer:
    def __init__(self):
        self.index = {}
    def get(self,item):
        return self.index_of(item)
    def __getitem__(self,item):
        return self.index_of(item)
    def index_of(self,item):
        ''' get the index of an item, adding it first if necessary '''
        if item not in self.index:
            self.index[item] = len(self)
        return self.index[item]
    def __len__(self):
        return len(self.index)
    def lookup_table(self):
        return dict(self.index)
    def reverse_lookup_table(self):
        return {v:k for k,v in self.lookup_table().items()}
    def __str__(self):
        return str(self.to_dict())
