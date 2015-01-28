package edu.byu.nlp.util;

public class Enums {

	public static <T extends Enum<T>> boolean isEnumValue(String stringVal, Class<? extends Enum<T>> en){
		Enum<T>[] values = en.getEnumConstants();
		for (Enum<T> enumVal: values){
			if (enumVal.name().toLowerCase().equals(stringVal.toLowerCase())){
				return true;
			}
		}
		return false;
	}

}
