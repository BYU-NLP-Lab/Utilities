package edu.byu.nlp.util;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.byu.nlp.util.Integers.MutableInteger;

public class TableCounter<R,C,I> {

	private Table<R, C, Counter<I>> table = HashBasedTable.create();
	
	public static<R,C,I> TableCounter<R,C,I> create(){
		return new TableCounter<R,C,I>();
	}

	public void incrementCount(R row, C col, I item){
		incrementCount(row, col, item, 1);
	}
	
	public void incrementCount(R row, C col, I item, int val){
		if (!table.contains(row, col)){
			table.put(row, col, new HashCounter<I>());
		}
		Counter<I> items = table.get(row, col);
		
		items.incrementCount(item, val);
	}
	
	public Counter<I> getCounter(R row, C col){
		return table.get(row, col);
	}
	
	public int getCount(R row, C col, I item){
		if (table.contains(row, col)){
			return table.get(row, col).getCount(item);
		}
		return 0;
	}
	
	public int totalCount(){
	  final MutableInteger retval = MutableInteger.from(0);
	  visitEntriesSparsely(new SparseTableVisitor<R, C, I>() {
      @Override
      public void visitEntry(R row, C col, I item, int count) {
        retval.setValue(retval.getValue()+count);
      }
    });
	  return retval.getValue();
	}

	public void visitRowEntriesSparsely(R row, SparseTableVisitor<R,C,I> visitor){
		if (table.containsRow(row)){
			Map<C, Counter<I>> rowEntry = table.row(row);
			for (Entry<C, Counter<I>> colEntry: rowEntry.entrySet()){
				C col = colEntry.getKey();
				for (Entry<I, Integer> itemEntry: colEntry.getValue().entrySet()){
					I item = itemEntry.getKey();
					
					visitor.visitEntry(row, col, item, itemEntry.getValue());
				}
			}
		}
	}
	
	public void visitEntriesSparsely(SparseTableVisitor<R,C,I> visitor){
		for (Entry<R, Map<C, Counter<I>>> rowEntry: table.rowMap().entrySet()){
			R row = rowEntry.getKey();
			for (Entry<C, Counter<I>> colEntry: rowEntry.getValue().entrySet()){
				C col = colEntry.getKey();
				for (Entry<I, Integer> itemEntry: colEntry.getValue().entrySet()){
					I item = itemEntry.getKey();
					
					visitor.visitEntry(row, col, item, itemEntry.getValue());
				}
			}
		}
	}
	
	
	public static abstract class SparseTableVisitor<R,C,I>{
		public abstract void visitEntry(R row, C col, I item, int count);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		visitEntriesSparsely(new SparseTableVisitor<R, C, I>() {
			
			@Override
			public void visitEntry(R row, C col, I item, int count) {
				sb.append(sb.append("\n\t["+row+"]["+col+"]["+item+"]="+count));
			}
		});
		return sb.toString();
	}
}
