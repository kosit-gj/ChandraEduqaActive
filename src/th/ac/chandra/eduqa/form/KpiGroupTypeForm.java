package th.ac.chandra.eduqa.form;

import java.io.Serializable;

import th.ac.chandra.eduqa.model.KpiGroupTypeModel;

public class KpiGroupTypeForm extends CommonForm implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6498711139841794347L;
	/**
	 * 
	 */
	private KpiGroupTypeModel kpiGroupTypeModel;
	private String createDate;
	private String pageSize = "10";
	private String keyListStatus;
	
	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	
	
	public KpiGroupTypeForm(KpiGroupTypeModel kpiGroupTypeModel) {
		super();
		this.kpiGroupTypeModel = kpiGroupTypeModel;
	}
	public KpiGroupTypeForm() {
		super();
		kpiGroupTypeModel=new KpiGroupTypeModel();
	}
	public KpiGroupTypeModel getKpiGroupTypeModel() {
		return kpiGroupTypeModel;
	}
	public void setKpiGroupTypeModel(KpiGroupTypeModel KpiGroupModel) {
		this.kpiGroupTypeModel = KpiGroupModel;
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
