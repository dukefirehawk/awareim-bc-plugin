package com.neural.json.parser;

import java.io.Reader;
import java.util.List;

public interface IBigCommerceParser<T> {

	List<T> parse(Reader reader) throws Exception;
}
