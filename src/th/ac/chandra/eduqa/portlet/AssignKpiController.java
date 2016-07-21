package th.ac.chandra.eduqa.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

import th.ac.chandra.eduqa.form.AssignTargetForm;
import th.ac.chandra.eduqa.form.HierarchyAuthorityForm;
import th.ac.chandra.eduqa.form.KpiForm;
import th.ac.chandra.eduqa.form.KpiListForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.DescriptionModel;
import th.ac.chandra.eduqa.model.KpiLevelModel;
import th.ac.chandra.eduqa.model.KpiModel;
import th.ac.chandra.eduqa.model.KpiResultModel;
import th.ac.chandra.eduqa.model.KpiTargetModel;
import th.ac.chandra.eduqa.model.OrgModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("assignKpiController")
@RequestMapping("VIEW")
public class AssignKpiController {
	private static final Logger logger = Logger.getLogger(AssignKpiController.class);
	@Autowired
	@Qualifier("eduqaServiceWSImpl")
	private EduqaService service;

	@InitBinder
	public void initBinder(PortletRequestDataBinder binder, PortletPreferences preferences) {
		logger.debug("initBinder");
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());	
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("VIEW") 
	public String iniPage(PortletRequest request,Model model){
		User user = (User) request.getAttribute(WebKeys.USER);
		DescriptionModel userOrg = new DescriptionModel();
		userOrg.setDescription(user.getScreenName());
		userOrg = service.getOrgOfUser(userOrg);
		
		OrgModel org = new OrgModel();
		org.setOrgId(Integer.parseInt(userOrg.getDescCode()));
		org = service.findOrgById(org);
		//ส่งไปเพื่อแสดงผลใช้ในการทดสอบ 
		model.addAttribute("userDetail","User: "+user.getScreenName()+""
				+ ",  OrgId: "+org.getOrgId());
		
		KpiListForm kpiListForm=null;
		if (!model.containsAttribute("kpiListForm")) {
			kpiListForm = new KpiListForm();
			kpiListForm.setOrgId(org.getOrgId());
			model.addAttribute("kpiListForm",	kpiListForm);
		}else{
			kpiListForm = (KpiListForm)model.asMap().get("kpiListForm");
		}
		HierarchyAuthorityForm HieAuth=null;
		if (!model.containsAttribute("hierarchyAuthorityForm")) {
			HieAuth = new HierarchyAuthorityForm();
			HieAuth.setOrgId(org.getOrgId());
			HieAuth.setLevel(org.getLevelId());
			HieAuth.setUniversity(org.getUniversityCode());
			HieAuth.setFaculty(org.getFacultyCode());
			HieAuth.setCourse(org.getCourseCode());
			model.addAttribute("hierarchyAuthorityForm",	HieAuth);
		}else{
			HieAuth = (HierarchyAuthorityForm)model.asMap().get("hierarchyAuthorityForm");
		}
		KpiResultModel resultModel = new KpiResultModel();
		resultModel.setOrgId(org.getOrgId());
		resultModel.setKpiLevelId(org.getLevelId()  );
		List<KpiResultModel> resultKpis = service.searchKpiResultWithActiveKpi(resultModel);
		List<KpiListForm> lists = convertAccordion(resultKpis);
		model.addAttribute("accordions",lists);
		model.addAttribute("lastPage",service.getResultPage());
		// list
		Map<Integer,String> levelList = new HashMap<Integer,String>();
		List<KpiLevelModel> levels = service.searchKpiLevel(new KpiLevelModel());
		for(KpiLevelModel level : levels){
			levelList.put(level.getLevelId(),level.getDesc());
		}
		model.addAttribute("levelList",levelList);
		
		Map<String,String> uniList = new HashMap<String,String>();
		List<DescriptionModel> unis = service.getUniversityAll(new DescriptionModel());
		for(DescriptionModel uni : unis){
			uniList.put(uni.getDescCode(),uni.getDescription());
		}
		model.addAttribute("uniList",uniList);
		
		Map<String,String> facList = new HashMap<String,String>();
		/*List<DescriptionModel> facs = service.getFacultyAll(new DescriptionModel());
		for(DescriptionModel fac : facs){
			facList.put(fac.getDescCode(),fac.getDescription());
		}*/
		List<OrgModel> facs = service.getOrgFacultyOfUniversity(org);
		for(OrgModel fac : facs){
			facList.put(fac.getFacultyCode(),fac.getFacultyName());
		}
		model.addAttribute("facList",facList);
		
		Map<String,String> corsList = new HashMap<String,String>();
		List<DescriptionModel> cors = service.getCourseAll(new DescriptionModel());
		for(DescriptionModel cor : cors){
			corsList.put(cor.getDescCode(),cor.getDescription());
		}
		model.addAttribute("corsList",corsList);
		model.addAttribute("currentFaculty", (org.getFacultyCode() == null || org.getFacultyCode() == "" ? 0 : org.getFacultyCode()));
		model.addAttribute("currentCourse", (org.getCourseCode() == null || org.getCourseCode() == "" ? 0 : org.getCourseCode()));

		return "dataEntry/assignKpi";
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping("VIEW")
	@RenderMapping( params="render=showList")
	public String renderKpiList(PortletRequest request,Model model){
		User user = (User) request.getAttribute(WebKeys.USER);
		DescriptionModel userOrg = new DescriptionModel();
		userOrg.setDescription(user.getScreenName());
		userOrg = service.getOrgOfUser(userOrg);
		OrgModel org = new OrgModel();
		org.setOrgId(Integer.parseInt(userOrg.getDescCode()));
		org = service.findOrgById(org);
		
		String workOrgId = validParamString(request.getParameter("workOrgId"));
		OrgModel workOrg = new OrgModel();
		workOrg.setOrgId(Integer.parseInt(workOrgId));
		workOrg = service.findOrgById(workOrg);
		
		// handle mapping form		
		KpiListForm kpiListForm=null;
		if (!model.containsAttribute("kpiListForm")) {
			kpiListForm = new KpiListForm();
			kpiListForm.setOrgId(workOrg.getOrgId());
			model.addAttribute("kpiListForm",	kpiListForm);
		}else{
			kpiListForm = (KpiListForm)model.asMap().get("kpiListForm");
		}
		HierarchyAuthorityForm HieAuth=null;
		if (!model.containsAttribute("hierarchyAuthorityForm")) {
			
			//OrgModel org = new OrgModel();
			org.setOrgId(org.getOrgId());
			org = service.findOrgById(org);
			HieAuth = new HierarchyAuthorityForm();
			HieAuth = new HierarchyAuthorityForm();
			HieAuth.setOrgId(workOrg.getOrgId());
			HieAuth.setLevel(workOrg.getLevelId());
			HieAuth.setUniversity(workOrg.getUniversityCode());
			HieAuth.setFaculty(workOrg.getFacultyCode());
			HieAuth.setCourse(workOrg.getCourseCode());
			model.addAttribute("hierarchyAuthorityForm", HieAuth);
		}else{
			HieAuth = (HierarchyAuthorityForm)model.asMap().get("hierarchyAuthorityForm");
		}
		KpiResultModel resultModel = new KpiResultModel();
		resultModel.setOrgId(workOrg.getOrgId());
		resultModel.setKpiLevelId(workOrg.getLevelId()  );
		List<KpiResultModel> resultKpis = service.searchKpiResultWithActiveKpi(resultModel);
		List<KpiListForm> lists = convertAccordion(resultKpis);
		model.addAttribute("accordions",lists);
		model.addAttribute("lastPage",service.getResultPage());
		
		// list from iniPage ชั่วคราว
		Map<Integer,String> levelList = new HashMap<Integer,String>();
		List<KpiLevelModel> levels = service.searchKpiLevel(new KpiLevelModel());
		for(KpiLevelModel level : levels){
			levelList.put(level.getLevelId(),level.getDesc());
		}
		model.addAttribute("levelList",levelList);
		
		Map<String,String> uniList = new HashMap<String,String>();
		List<DescriptionModel> unis = service.getUniversityAll(new DescriptionModel());
		for(DescriptionModel uni : unis){
			uniList.put(uni.getDescCode(),uni.getDescription());
		}
		model.addAttribute("uniList",uniList);
		
		Map<String,String> facList = new HashMap<String,String>();
		List<OrgModel> facs = service.getOrgFacultyOfUniversity(workOrg);
		for(OrgModel fac : facs){
			facList.put(fac.getFacultyCode(),fac.getFacultyName());
		}
		model.addAttribute("facList",facList);
		
		Map<String,String> corsList = new HashMap<String,String>();
		List<DescriptionModel> cors = service.getCourseAll(new DescriptionModel());
		for(DescriptionModel cor : cors){
			corsList.put(cor.getDescCode(),cor.getDescription());
		}
		model.addAttribute("corsList",corsList);

		model.addAttribute("currentFaculty", (HieAuth.getFaculty() == null || HieAuth.getFaculty() == "" ? 0 : HieAuth.getFaculty()));
		model.addAttribute("currentCourse", (HieAuth.getCourse() == null || HieAuth.getCourse() == "" ? 0 : HieAuth.getCourse()));
		model.addAttribute("userDetail","User: "+user.getScreenName()+""
				+ ",  OrgId: "+org.getOrgId()+""
				+ ",  CurrentFaculity: "+HieAuth.getFaculty()+""
				+ ",  CurrentCourse: "+HieAuth.getCourse());
		
		return "dataEntry/assignKpi";
	}
	
	
	@RequestMapping(params="action=doSubmitFilter") 
	public void actionSubmitFilter(javax.portlet.ActionRequest request, javax.portlet.ActionResponse response
			,@ModelAttribute("HierarchyAuthorityForm") HierarchyAuthorityForm HieAuth,BindingResult result,Model model){
		response.setRenderParameter("render", "showList");
		response.setRenderParameter("workOrgId",HieAuth.findoutOrg() );
	}
	@RequestMapping(params="action=doBack2List") 
	public void actionBack2List(javax.portlet.ActionRequest request, javax.portlet.ActionResponse response
			,@ModelAttribute("assignTargetForm") AssignTargetForm assignTargetForm,BindingResult result,Model model){
		response.setRenderParameter("render", "showList");
		response.setRenderParameter("workOrgId",String.valueOf(assignTargetForm.getOrgId()) );
	}
	// kpiList 
	@RequestMapping(params="action=doAssignTarget") 
	public void actionDisplayTarget(javax.portlet.ActionRequest request, javax.portlet.ActionResponse response,
			@ModelAttribute("kpiListForm") KpiListForm kpiListForm,BindingResult result,Model model){
		response.setRenderParameter("render", "showTarget");
		response.setRenderParameter("kpiId", String.valueOf(kpiListForm.getKpiId()));
		response.setRenderParameter("orgId", String.valueOf(kpiListForm.getOrgId()));
	}
	@RequestMapping("VIEW")
	@RenderMapping(params="render=showTarget")
	public String displayTarget(PortletRequest request,Model model){
		//
		Integer kpiId = Integer.parseInt( request.getParameter("kpiId") );
		Integer orgId = Integer.parseInt( request.getParameter("orgId"));
		KpiForm kpiForm=null;
		if (!model.containsAttribute("kpiForm")) {
			kpiForm = new KpiForm();
			model.addAttribute("kpiForm",	kpiForm);
		}else{
			kpiForm = (KpiForm)model.asMap().get("kpiForm");
		}
		// check
		AssignTargetForm af = new AssignTargetForm();
		af.setKpiId(kpiId);
		af.setOrgId(orgId);
		String[] monthList = new String[]{"","มกราคม","กุมภาพันธ์","มีนาคม","เมษายน","พฤษภาคม","มิถุนายน","กรกฎาคม","สิงหาคม","กันยายน","ตุลาคม","พฤศจิกายน","ธันวาคม"};
		if(kpiId!=null && kpiId > 0){
			KpiModel kpi = service.findKpiWithDescById(kpiId);
			kpiForm.setKpiModel(kpi);
			model.addAttribute("kpiForm",kpiForm);
			af.setFrequency( kpi.getPeriodId() );
			if(kpi.getCalendarTypeId()==1){
				af.setStartMonthNo(1);
			}
			else if(kpi.getCalendarTypeId()==2){ //  education month
				SysYearModel sysYearModel = new SysYearModel();
				List<SysYearModel> sysYears = service.searchSysYear(sysYearModel);
				SysYearModel sysYear = sysYears.get(0);
				//sysYear.getFirstMonthAcademic();
				af.setStartMonthNo( Arrays.asList(monthList).indexOf(sysYear.getFirstMonthAcademic()) );
			}
			else if(kpi.getCalendarTypeId()==3){ // fiscal 
				SysYearModel sysYearModel = new SysYearModel();
				List<SysYearModel> sysYears = service.searchSysYear(sysYearModel);
				SysYearModel sysYear = sysYears.get(0);
				//sysYear.getFirstMonthFiscal();
				af.setStartMonthNo( Arrays.asList(monthList).indexOf(sysYear.getFirstMonthFiscal()) );
			}
			KpiTargetModel target = new KpiTargetModel();
			target.setKpiId(kpiId);
			target.setOrgId(orgId);
			List<Double> targets = service.getKpiTarget(target);
			Integer i = 1;
			for(Double value : targets){
				if(i.equals(1)){ 	af.setRmonth1(value); 	}
				else if(i.equals(2)){ 	af.setRmonth2(value); 	}
				else if(i.equals(3)){ 	af.setRmonth3(value); 	}
				else if(i.equals(4)){ 	af.setRmonth4(value); 	}
				else if(i.equals(5)){ 	af.setRmonth5(value); 	}
				else if(i.equals(6)){ 	af.setRmonth6(value); 	}
				else if(i.equals(7)){ 	af.setRmonth7(value); 	}
				else if(i.equals(8)){ 	af.setRmonth8(value); 	}
				else if(i.equals(9)){ 	af.setRmonth9(value); 	}
				else if(i.equals(10)){ 	af.setRmonth10(value); 	}
				else if(i.equals(11)){ 	af.setRmonth11(value); 	}
				else if(i.equals(12)){ 	af.setRmonth12(value); 	}
				i++;
			}
			model.addAttribute("assignTargetForm",af);
		}
		else{ // new detail
			model.addAttribute("kpiForm",kpiForm);
			model.addAttribute("assignTargetForm",af);
		}
		return "dataEntry/assignKpiTarget";
	}
	
	@RequestMapping(params = "action=doSearch") 
	public void actionSearch(javax.portlet.ActionRequest request, javax.portlet.ActionResponse response,
			@ModelAttribute("kpiListForm") KpiListForm kpiListForm,BindingResult result,Model model) { 
		User user = (User) request.getAttribute(WebKeys.USER);
		String keySearch=kpiListForm.getKeySearch();
		response.setRenderParameter("render", "listSearch");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("level", validParamString(kpiListForm.getLevel()) );
		response.setRenderParameter("university", kpiListForm.getUniversity());
		response.setRenderParameter("faculty", kpiListForm.getFaculty());
		response.setRenderParameter("course", kpiListForm.getCourse());
	}
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listSearch")
	public String renderSearch(RenderRequest request,Model model){
			KpiModel KpiModel =new KpiModel ();
			KpiModel.setKeySearch(request.getParameter("keySearch"));
			Paging page = new Paging();  // default pageNo = 1
			KpiModel.setPaging(page);
			// convert Model -> form 
		//	List<KpiModel> kpis = service.searchKpi(KpiModel);
		//	List<KpiListForm> lists = convertAccordion(kpis);
	//		model.addAttribute("accordions",lists);
	//		model.addAttribute("lastPage",service.getResultPage());
			return "dataEntry/assignKpi";
	}
	// detail request
	
	
	/* #####  function */
	private List<KpiListForm> convertAccordion(List<KpiResultModel> kpis){
		ListIterator<KpiResultModel> kpisIter = kpis.listIterator();
		
		//required sort data from service;
		List<KpiResultModel> listKpi = new ArrayList<KpiResultModel>();
		KpiListForm form = new KpiListForm();
		List<KpiListForm> forms = new ArrayList<KpiListForm>();
		Integer previousId = null;
		while(kpisIter.hasNext()){
			KpiResultModel kpi = (KpiResultModel) kpisIter.next();
			if(!kpi.getKpiStructureId().equals(previousId) || previousId==null){ // if not
				form = new KpiListForm();
				form.setStructureName(kpi.getKpiStructureName());
				listKpi = new ArrayList<KpiResultModel>();
				listKpi.add(kpi);
			}
			else{ // equals previousId
				listKpi.add(kpi);
			}
			//
			if(kpisIter.hasNext()){
				Integer current = kpi.getKpiStructureId();
				KpiResultModel next = (KpiResultModel) kpisIter.next();
				if(!next.getKpiStructureId().equals(current)){
					form.setResultKpis(listKpi);
					forms.add(form);
				}
				kpisIter.previous();
			}else{ // last
				form.setResultKpis(listKpi);
				forms.add(form);
			}
			// last thing 
			previousId = kpi.getKpiStructureId();
		}	// end while iterator 
		return forms;
	}
		
	
	@ResourceMapping(value = "doSaveTarget" )
	@ResponseBody 
	public void saveTargetAjax(ResourceRequest request,ResourceResponse response) 
			throws IOException{
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest	=	PortalUtil.getOriginalServletRequest(httpReq);
		JSONObject json = JSONFactoryUtil.createJSONObject();
		JSONObject header = JSONFactoryUtil.createJSONObject();
		JSONObject content = JSONFactoryUtil.createJSONObject();
		List<HashMap<Integer,Integer>> lists = new ArrayList();
		HashMap<Integer,Double> ls = new HashMap<Integer,Double>();
		if( normalRequest.getParameter("m1") !="0" && normalRequest.getParameter("m1")!=""  ){
			ls.put(1,Double.parseDouble(normalRequest.getParameter("m1")) );
		}
		if( normalRequest.getParameter("m2") !="0" && normalRequest.getParameter("m2")!=""  ){
			ls.put(2,Double.parseDouble(normalRequest.getParameter("m2")) );
		}
		if( normalRequest.getParameter("m3") !="0" && normalRequest.getParameter("m3")!=""  ){
			ls.put(3,Double.parseDouble(normalRequest.getParameter("m3")) );
		}
		if( normalRequest.getParameter("m4") !="0" && normalRequest.getParameter("m4")!=""  ){
			ls.put(4,Double.parseDouble(normalRequest.getParameter("m4")) );
		}
		if( normalRequest.getParameter("m5") !="0" && normalRequest.getParameter("m5")!=""  ){
			ls.put(5,Double.parseDouble(normalRequest.getParameter("m5")) );
		}
		if( normalRequest.getParameter("m6") !="0" && normalRequest.getParameter("m6")!=""  ){
			ls.put(6,Double.parseDouble(normalRequest.getParameter("m6")) );
		}
		if( normalRequest.getParameter("m7") !="0" && normalRequest.getParameter("m7")!=""  ){
			ls.put(7,Double.parseDouble(normalRequest.getParameter("m7")) );
		}
		if( normalRequest.getParameter("m8") !="0" && normalRequest.getParameter("m8")!=""  ){
			ls.put(8,Double.parseDouble(normalRequest.getParameter("m8")) );
		}
		if( normalRequest.getParameter("m9") !="0" && normalRequest.getParameter("m9")!=""  ){
			ls.put(9,Double.parseDouble(normalRequest.getParameter("m9")) );
		}
		if( normalRequest.getParameter("m10") !="0" && normalRequest.getParameter("m10")!=""  ){
			ls.put(10,Double.parseDouble(normalRequest.getParameter("m10")) );
		}
		if( normalRequest.getParameter("m11") !="0" && normalRequest.getParameter("m11")!=""  ){
			ls.put(11,Double.parseDouble(normalRequest.getParameter("m11")) );
		}
		if( normalRequest.getParameter("m12") !="0" && normalRequest.getParameter("m12")!=""  ){
			ls.put(12,Double.parseDouble(normalRequest.getParameter("m12")) );
		}
		KpiTargetModel target = new KpiTargetModel();
		target.setListMonth(ls);
		target.setKpiId( Integer.parseInt( normalRequest.getParameter("kpiId") ) );
		target.setOrgId( Integer.parseInt( normalRequest.getParameter("orgId") ) );
		User user = (User) request.getAttribute(WebKeys.USER);
		target.setCreatedBy(user.getFullName());
		target.setUpdatedBy(user.getFullName());
		Integer totalSuccess = service.saveKpiTarget(target);
		if(totalSuccess>0){
			header.put("success",1);
		}else{
			header.put("success", 0);
		}
		json.put("content",content);
		json.put("header",header);
		System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	//# function 
	private String validParamString(Object param){
		String str = null;
		if(param!=null){
			if(param instanceof String ){
				str = (String) param;
			}else if(param instanceof Integer){
				str = String.valueOf( param );
			}
		}else{
			str = "";
		}
		return str;
	}
	private List<String> getOrganizeObject(HierarchyAuthorityForm form){
		List<String> ret = new ArrayList<String>();
		String level = null;
		String org = null;
		if(form.getLevel().equals(1)){
			level = "1";
			org = String.valueOf(form.getUniversity());
		}else if(form.getLevel().equals(2)){
			level = "2";
			org = String.valueOf(form.getFaculty());
		}else if(form.getLevel().equals(3)){
			level = "3";
			org = String.valueOf(form.getCourse());
		}
		ret.add(level);
		ret.add(org);
		return  ret;
	}
	//  #### ajaax
	@ResourceMapping(value="doSearchOrg")
	@ResponseBody 
	public void searchOrgByLevel(ResourceRequest request,ResourceResponse response) 
			throws IOException{
		//WARNING**   service cann't be null
		// return <id,desc>
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest	=	PortalUtil.getOriginalServletRequest(httpReq);
		JSONObject json = JSONFactoryUtil.createJSONObject();
		JSONObject header = JSONFactoryUtil.createJSONObject();
		JSONObject content = JSONFactoryUtil.createJSONObject();
		 JSONArray orgList = 	JSONFactoryUtil.createJSONArray();
		String status = "";
		Integer size = 0;
		if( normalRequest.getParameter("level")!=null && normalRequest.getParameter("level")!=""){

			User user = (User) request.getAttribute(WebKeys.USER);
			DescriptionModel userOrg = new DescriptionModel();
			userOrg.setDescription(user.getScreenName());
			userOrg = service.getOrgOfUser(userOrg);
			
			OrgModel org = new OrgModel();
			org.setOrgId( Integer.parseInt( userOrg.getDescCode()) );
			org.setLevelId( Integer.parseInt(normalRequest.getParameter("level")));
			org.setUniversityCode(validParamString(normalRequest.getParameter("university"))  );
			org.setFacultyCode( validParamString(normalRequest.getParameter("faculty")) );
			org.setCourseCode( validParamString(normalRequest.getParameter("course")));

			List<OrgModel> results = new ArrayList<OrgModel>();
			if(org.getLevelId().equals(0)){
				results = service.getAllUniversity(org);
				if(results!=null){
					size = results.size();
					if(size>0){
			             for (OrgModel result : results) {
			            	 JSONObject rec = JSONFactoryUtil.createJSONObject();
			            	 rec.put("orgCode",result.getUniversityCode());
			            	 rec.put("orgDesc",result.getUniversityName());
			            	 orgList.put(rec);
			             }
					}
				}//end result!=null
			}else if(org.getLevelId().equals(1)){
				results = service.getOrgFacultyOfUniversity(org);
				if(results!=null){
					size = results.size();
					if(size>0){
			             for (OrgModel result : results) {
			            	 JSONObject rec = JSONFactoryUtil.createJSONObject();
			            	 rec.put("orgCode",result.getFacultyCode());
			            	 rec.put("orgDesc",result.getFacultyName());
			            	 orgList.put(rec);
			             }
					}
				}//end result!=null
			}else if(org.getLevelId().equals(2)){
				results = service.getOrgCourseOfFaculty(org);
				if(results!=null){
					size = results.size();
					if(size>0){
			             for (OrgModel result : results) {
			            	 JSONObject rec = JSONFactoryUtil.createJSONObject();
			            	 rec.put("orgCode",result.getCourseCode());
			            	 rec.put("orgDesc",result.getCourseName());
			            	 orgList.put(rec);
			             }
					}
				}//end result!=null
			}
		}else{
			status = "invalid parameters";
		}
		content.put("lists",orgList);
		header.put("status", status);
		header.put("size", size);
		json.put("header",header);
		json.put("content", content);
		System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value="doInsertResult")
	@ResponseBody 
	public void saveResultOfOrg(ResourceRequest request,ResourceResponse response) 
			throws IOException{
		// required orgId, kpisIds
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest	=	PortalUtil.getOriginalServletRequest(httpReq);
		JSONObject json = JSONFactoryUtil.createJSONObject();
		JSONObject header = JSONFactoryUtil.createJSONObject();
		JSONObject content = JSONFactoryUtil.createJSONObject();
		String status = "";
		Integer success = 0;
		User user = (User) request.getAttribute(WebKeys.USER);
		
		if( normalRequest.getParameter("orgId")!=null && normalRequest.getParameter("orgId")!=""){
			Integer orgId = Integer.parseInt(normalRequest.getParameter("orgId"));
			String deleteKpis = normalRequest.getParameter("deleteKpis");
			String insertKpis = normalRequest.getParameter("insertKpis");
			Integer deleteStatusCode = null, insertStatusCode = null; 
			String deleteStatusDesc = "", insertStatusDesc = "";
			
			/*delete result of org before insert*/ 
			if(deleteKpis != null && deleteKpis != ""){
				String[] kpisPart = deleteKpis.split("-");
				List<Integer> kpisList = new ArrayList<Integer>();
				for(String part : kpisPart){
					kpisList.add(Integer.parseInt(part));
				}
				
				KpiResultModel kpiResultM = new KpiResultModel();
				kpiResultM.setOrgId(orgId);
				kpiResultM.setKpiIds(kpisList);
				kpiResultM.setCreatedBy(user.getFullName());
				kpiResultM.setUpdatedBy(user.getFullName());
				
				ResultService rsDel = service.deleteResultByOrg(kpiResultM);
				if(!rsDel.isError()){
					deleteStatusCode = 1;
					deleteStatusDesc = "ลบข้อมูล kpi_result ที่มี kpi_id in("+deleteKpis+") เสร็จสิ้น";
				}else{
					deleteStatusCode = 0;
					deleteStatusDesc = rsDel.getMsgDesc();
				}
			}else{
				deleteStatusCode = 1;
			}
			
			//Save kpi_result
			if(insertKpis != null && insertKpis != ""){
				String[] kpisPart = insertKpis.split("-");
				List<Integer> kpisList = new ArrayList<Integer>();
				for(String part : kpisPart){
					kpisList.add(Integer.parseInt(part));
				}
				
				KpiResultModel kpiResultM = new KpiResultModel();
				kpiResultM.setOrgId(orgId);
				kpiResultM.setKpiIds(kpisList);
				kpiResultM.setCreatedBy(user.getFullName());
				kpiResultM.setUpdatedBy(user.getFullName());
				
				ResultService rsDel = service.saveResultOfOrg(kpiResultM);
				if(!rsDel.isError()){
					insertStatusCode = 1;
					insertStatusDesc = "บันทึกข้อมูล kpi_result ที่มี kpi_id in("+insertKpis+") เสร็จสิ้น";
				}else{
					insertStatusCode = 0;
					insertStatusDesc = rsDel.getMsgDesc();
				}
			}else{
				insertStatusCode = 1;
			}
			
			//ตรวจสอบการทำงานในส่วนการลบ และบันทึก
			if(insertStatusCode == 1 && deleteStatusCode == 1){
				success = 1;
			}else{
				status = "insertStatusDesc: "+insertStatusDesc+",  deleteStatusDesc: "+deleteStatusDesc;
			}
			
		}else{
			status = "invalid parameters";
		}
		
		content.put("lists","");
		header.put("status", status);
		header.put("success",success );
		json.put("header",header);
		json.put("content", content);
		System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value="doReloadResult")
	@ResponseBody 
	public void reloadResultOfOrg(ResourceRequest request,ResourceResponse response) 
			throws IOException{
		// required orgId, kpisIds
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest	=	PortalUtil.getOriginalServletRequest(httpReq);
		JSONObject json = JSONFactoryUtil.createJSONObject();
		JSONObject header = JSONFactoryUtil.createJSONObject();
		JSONObject content = JSONFactoryUtil.createJSONObject();
		 JSONArray orgList = JSONFactoryUtil.createJSONArray();
		String status = "";
		Integer success = 0;
		List<Integer> activeKpiIds = new ArrayList<Integer>();
		if( normalRequest.getParameter("orgId")!=null && normalRequest.getParameter("orgId")!=""){
		
				Integer orgId = Integer.parseInt(normalRequest.getParameter("orgId"));
				OrgModel orgM = new OrgModel();
				orgM.setOrgId(orgId);
				orgM = service.findOrgById(orgM);
				
				KpiResultModel resultModel = new KpiResultModel();
				resultModel.setOrgId(orgM.getOrgId());
				resultModel.setKpiLevelId(orgM.getLevelId());
				List<KpiResultModel> resultKpis = service.searchKpiResultWithActiveKpi(resultModel);
				for( KpiResultModel result : resultKpis){
					if(result.getResultId()>0){
						activeKpiIds.add(result.getKpiId()); // handle result  = activeFlag
					}
				}
				success = 1;
		}else{
			success = 0;
			status = "invalid parameters";
		}
		content.put("lists",StringUtils.join(activeKpiIds,'-'));
		header.put("status", status);
		header.put("success",success );
		json.put("header",header);
		json.put("content", content);
		System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value = "dofindOrgByUserName")
	@ResponseBody
	public void dofindOrgByUserName(ResourceRequest request, ResourceResponse response) throws IOException {
		JSONObject json = JSONFactoryUtil.createJSONObject();
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest = PortalUtil.getOriginalServletRequest(httpReq);
	
		User user = (User) request.getAttribute(WebKeys.USER);
		DescriptionModel userOrg = new DescriptionModel();
		userOrg.setDescription(user.getScreenName());
		userOrg = service.getOrgOfUser(userOrg);
		String UserOrgId = userOrg.getDescCode();
		
		OrgModel org = new OrgModel();
		org.setOrgId(Integer.parseInt(UserOrgId));
		
		OrgModel orgs = service.findOrgById(org);
		JSONArray lists = JSONFactoryUtil.createJSONArray();
		JSONObject connJSON = JSONFactoryUtil.createJSONObject();
		connJSON.put("levelId", orgs.getLevelId());
		
        lists.put(connJSON);
		json.put("userRoleId", lists);
		
		//System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value = "dofindOrgByOrgId")
	@ResponseBody
	public void dofindOrgByOrgId(ResourceRequest request, ResourceResponse response) throws IOException {
		JSONObject json = JSONFactoryUtil.createJSONObject();
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest = PortalUtil.getOriginalServletRequest(httpReq);
	
		Integer orgId = Integer.parseInt( normalRequest.getParameter("orgId"));
		OrgModel org = new OrgModel();
		org.setOrgId(orgId);
		
		OrgModel orgs = service.findOrgById(org);
		JSONArray lists = JSONFactoryUtil.createJSONArray();
		JSONObject connJSON = JSONFactoryUtil.createJSONObject();
		connJSON.put("facultyCode", orgs.getFacultyCode());
        connJSON.put("facultyName", orgs.getFacultyName());
        connJSON.put("courseCode", orgs.getCourseCode());
        connJSON.put("courseName", orgs.getCourseName());
        
        User user = (User) request.getAttribute(WebKeys.USER);
		DescriptionModel userOrg = new DescriptionModel();
		userOrg.setDescription(user.getScreenName());
		userOrg = service.getOrgOfUser(userOrg);
		String UserOrgId = userOrg.getDescCode();
		OrgModel orgByUser = service.findOrgById(org);
		connJSON.put("userRoleOrgId", UserOrgId);
		
        lists.put(connJSON);
		json.put("facultyCourseList", lists);
		
		//System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value="requestOrgFaculty")
	@ResponseBody 
	public void getOrgFaculty(ResourceRequest request,ResourceResponse response) throws IOException{
		JSONObject json = JSONFactoryUtil.createJSONObject();
		
		//Integer groupId = ParamUtil.getInteger(request, "groupId");
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest =PortalUtil.getOriginalServletRequest(httpReq);
		Integer levelId = Integer.parseInt(normalRequest.getParameter("levelId"));
		String university = normalRequest.getParameter("university");
		
		User user = (User) request.getAttribute(WebKeys.USER);
		DescriptionModel userOrg = new DescriptionModel();
		userOrg.setDescription(user.getScreenName());
		userOrg = service.getOrgOfUser(userOrg);
		
		OrgModel orgModel = new OrgModel();
		orgModel.setOrgId(Integer.parseInt(userOrg.getDescCode()));
		orgModel.setLevelId(levelId);
		orgModel.setUniversityCode(university);
		//orgModel.setOtherKeySearch(otherKeySearch);
				
		List<OrgModel> details = service.getOrgFacultyOfUniversity(orgModel);
		JSONArray lists = JSONFactoryUtil.createJSONArray();
		for(OrgModel detail:details ){
			JSONObject connJSON = JSONFactoryUtil.createJSONObject();
         	connJSON.put("id", detail.getFacultyCode());
         	connJSON.put("name", detail.getFacultyName());
         	lists.put(connJSON);
         }
		json.put("lists", lists);
		
		/*List<OrgModel> orgId = service.searchOrgIdByOthersCode(orgModel);
		OrgModel orgIdModel = new OrgModel();
		orgIdModel = orgId.get(0);
		String orgIdStr = orgIdModel.getOrgId().toString();*/
		
		//System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
	
	@ResourceMapping(value="requestOrgCourse")
	@ResponseBody
	public void getOrgCourse(ResourceRequest request,ResourceResponse response) throws IOException{
		JSONObject json = JSONFactoryUtil.createJSONObject();
		
		//Integer groupId = ParamUtil.getInteger(request, "groupId");
		HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
		HttpServletRequest normalRequest	=	PortalUtil.getOriginalServletRequest(httpReq);
		Integer levelId = Integer.parseInt( normalRequest.getParameter("levelId") );
		String uniCode  = normalRequest.getParameter("university");
		String facultyCode = normalRequest.getParameter("faculty");
		OrgModel orgModel = new OrgModel();
		orgModel.setLevelId(levelId);
		orgModel.setUniversityCode(uniCode);
		orgModel.setFacultyCode(facultyCode);
		
		List<OrgModel> details = service.getOrgCourseOfFaculty(orgModel);
		 JSONArray lists = 	JSONFactoryUtil.createJSONArray();
		for(OrgModel detail:details ){
			JSONObject connJSON = JSONFactoryUtil.createJSONObject();
         	connJSON.put("id", detail.getCourseCode());
         	connJSON.put("name", detail.getCourseName());
         	lists.put(connJSON);
         }     	
		json.put("lists", lists);
		//System.out.println(json.toString());
		response.getWriter().write(json.toString());
	}
}
