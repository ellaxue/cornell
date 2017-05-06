package IO;

import project.Tuple;

public interface TupleWriter {

	
/**
 * write next tuple	
 */
public void writeNext(Tuple tu) throws Exception;

public void writeNext(String str) throws Exception;


/**
 * close the writer
 */
public void close() throws Exception;

public void writeHeader(String line) throws Exception;
}