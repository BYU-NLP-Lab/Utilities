package edu.byu.nlp.util;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public class Functions2 {

	@SafeVarargs
	public static <A> Function<A, A> compose(Function<A, A>... funcs) {
		  if (funcs.length==0){
			  return null;
		  }
		  if (funcs.length==1){
			  return funcs[0];
		  }
		  if (funcs.length==2){
			  return Functions.compose(funcs[0],funcs[1]);
		  }
		  return Functions.compose(funcs[0], compose(Arrays.subarray(funcs, 1)));
	  }
	  
}
