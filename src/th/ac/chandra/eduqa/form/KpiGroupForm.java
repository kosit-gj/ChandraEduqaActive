package th.ac.chandra.eduqa.form;

import java.io.Serializable;

import th.ac.chandra.eduqa.model.KpiGroupModel;

public class KpiGroupForm extends CommonForm implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6498711139841794347L;
	/**
	 * 
	 */
	private KpiGroupModel kpiGroupModel;
	private String createDate;
	private String pageSize = "10";
	private String keyListStatus;
	
	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	public KpiGroupForm(KpiGroupModel KpiGroupModel) {
		super();
		this.kpiGroupModel = KpiGroupModel;
	}
	public KpiGroupForm() {
		super();
		kpiGroupModel=new KpiGroupModel();
	}
	public KpiGroupModel getKpiGroupModel() {
		return kpiGroupModel;
	}
	public void setKpiGroupModel(KpiGroupModel KpiGroupModel) {
		this.kpiGroupModel = KpiGroupModel;
	}
	
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getKeyListStatus() {
		return keyListStatus;
	}
	public void setKeyListStatus(String keyListStatus) {
		this.keyListStatus = keyListStatus;
	}
	
	
}
