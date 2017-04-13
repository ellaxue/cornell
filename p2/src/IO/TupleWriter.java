package IO;

import java.io.IOException;

import project.Tuple;

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