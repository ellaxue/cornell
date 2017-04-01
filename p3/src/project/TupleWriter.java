package project;

import java.io.IOException;

public interface TupleWriter {

	
/**
 * write next tuple	
 */
public void writeNext(Tuple tu) throws IOException;


/**
 * close the writer
 */
public void close() throws IOException;
}