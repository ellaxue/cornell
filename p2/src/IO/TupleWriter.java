package IO;

import java.io.IOException;

import project.Tuple;

public interface TupleWriter {

	
/**
 * write next tuple	
 */
public void writeNext(Tuple tu) throws IOException;

public void writeNext(String str) throws IOException;


/**
 * close the writer
 */
public void close() throws IOException;

public void writeHeader(String line, int numElement);
}