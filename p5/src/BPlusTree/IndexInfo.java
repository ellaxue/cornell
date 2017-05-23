package BPlusTree;

/**
 * This class stores relation index information
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class IndexInfo {
	private String indexCol;	
	private Boolean clustered;
	private String tableName;
	private Integer lowKey;
	private Integer highKey;
	
	/**
	 * @return the lowKey
	 */
	public Integer getLowKey() {
		return lowKey;
	}
	/**
	 * @param lowKey the lowKey to set
	 */
	public void setLowKey(Integer lowKey) {
		this.lowKey = lowKey;
	}
	/**
	 * @return the highKey
	 */
	public Integer getHighKey() {
		return highKey;
	}
	/**
	 * @param highKey the highKey to set
	 */
	public void setHighKey(Integer highKey) {
		this.highKey = highKey;
	}
	/**
	 * constructor
	 * @param index which column needs to be indexed in the bplustree
	 * @param cluster should the index be clustered
	 */
	public IndexInfo(String index, Boolean cluster){
		indexCol = index;
		clustered = cluster;
	}
	public IndexInfo(String tableName, String colName, Boolean isCluster, Integer indexLowBound, Integer indexUpBound) {
		this.tableName = tableName;
		this.indexCol = colName;
		this.clustered = isCluster;
		this.lowKey = indexLowBound;
		this.highKey = indexUpBound;
		
		// TODO Auto-generated constructor stub
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
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
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
