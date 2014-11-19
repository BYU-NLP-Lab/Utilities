package edu.byu.nlp.util;

import org.fest.assertions.Assertions;
import org.fest.assertions.Fail;
import org.junit.Test;

import edu.byu.nlp.util.TableCounter.SparseTableVisitor;

public class TableCounterTest {

	private static TableCounter<String,String,String> mockTable(){
		TableCounter<String, String, String> tc = new TableCounter<String, String, String>();
		tc.incrementCount("r1", "c1", "john");
		tc.incrementCount("r1", "c1", "john");
		tc.incrementCount("r1", "c1", "john");
		tc.incrementCount("r1", "c1", "mary",5);

		tc.incrementCount("r1", "c2", "john");

		tc.incrementCount("r2", "c1", "john");
		tc.incrementCount("r2", "c2", "john");
		tc.incrementCount("r2", "c3", "john");
		return tc;
	}
	
	@Test
	public void testCounterGetSetCounts(){
		TableCounter<String, String, String> table = mockTable();

		Assertions.assertThat(table.getCount("r1", "c1", "john")).isEqualTo(3);
		Assertions.assertThat(table.getCount("r1", "c1", "mary")).isEqualTo(5);
		Assertions.assertThat(table.getCount("r2", "c1", "john")).isEqualTo(1);
		Assertions.assertThat(table.getCount("r2", "c2", "john")).isEqualTo(1);
		Assertions.assertThat(table.getCount("r2", "c3", "john")).isEqualTo(1);
		
		Assertions.assertThat(table.getCount("r1", "c65", "mary")).isEqualTo(0);
		Assertions.assertThat(table.getCount("r1", "c2", "mary")).isEqualTo(0);
	}
	
	@Test
	public void testVisitors(){
		final TableCounter<String, String, String> table = mockTable();
		
		table.visitEntriesSparsely(new SparseTableVisitor<String, String, String>() {
			@Override
			public void visitEntry(String row, String col, String item, int count) {
				if (row=="r1" && col=="c1" && item=="john"){
					Assertions.assertThat(3);
				}
				else if (row=="r1" && col=="c1" && item=="mary"){
					Assertions.assertThat(5);
				}
				else if (row=="r1" && col=="c2" && item=="john"){
					Assertions.assertThat(1);
				}
				else if (row=="r2" && col=="c1" && item=="john"){
					Assertions.assertThat(1);
				}
				else if (row=="r2" && col=="c2" && item=="john"){
					Assertions.assertThat(1);
				}
				else if (row=="r2" && col=="c3" && item=="john"){
					Assertions.assertThat(1);
				}
				else{
					Fail.fail("entry shouldn't exist: ["+row+"]["+col+"]["+item+"]");
				}
			}
		});
	}
	
}
