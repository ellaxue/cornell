package IO;

import javax.swing.tree.ExpandVetoException;
import project.Tuple;

public interface TupleReader {

/**
 * read next tuple from file
 */
public Tuple readNext() throws Exception;

/**
 * reset the reader to the starting position
 * @throws Exception 
 */
public void reset() throws Exception;

/**
 * reset the reader to a specific tuple index
 */
public void reset(int index) throws Exception; 

public int getCurTotalPageRead();

public int getCurPageTupleRead();

public void close() throws Exception;
}