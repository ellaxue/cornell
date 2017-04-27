package BPlusTree;

/**
 * This class stores relation index information
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class IndexInfo {
	private String indexCol;	
	private Boolean clustered;
	
	/**
	 * constructor
	 * @param index which column needs to be indexed in the bplustree
	 * @param cluster should the index be clustered
	 */
	public IndexInfo(String index, Boolean cluster){
		indexCol = index;
		clustered = cluster;
	}
	/**
	 * @return the indexCol
	 */
	public String getIndexCol() {
		return indexCol;
	}

	/**
	 * @param indexCol the indexCol to set
	 */
	public void setIndexCol(String indexCol) {
		this.indexCol = indexCol;
	}

	/**
	 * @return the clustered
	 */
	public Boolean getClustered() {
		return clustered;
	}

	/**
	 * @param clustered the clustered to set
	 */
	public void setClustered(Boolean clustered) {
		this.clustered = clustered;
	}

}
