package th.ac.chandra.eduqa.portlet;

import java.sql.Timestamp;
//import java.text.DateFormat;
import java.text.ParseException;
///import java.text.SimpleDateFormat;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;

import th.ac.chandra.eduqa.constant.ServiceConstant;
import th.ac.chandra.eduqa.form.ThresholdForm;
import th.ac.chandra.eduqa.mapper.CustomObjectMapper;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiLevelModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.model.ThresholdModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("thresholdController")
@RequestMapping("VIEW")
@SessionAttributes({ "thresholdForm" })
public class ThresholdController {
	private static final Logger logger = Logger
			.getLogger(ThresholdController.class);
	@Autowired
	@Qualifier("eduqaServiceWSImpl")
	private EduqaService service;

	private Integer getCurrentYear(){
		SysYearModel sysYearModel = new SysYearModel();
		List<SysYearModel> sysYears = service.searchSysYear(sysYearModel);
		try{
			sysYearModel = sysYears.get(0);
			return sysYearModel.getMasterAcademicYear();
		}catch(Exception err){
			return 9999;
		}
	}

	@InitBinder
	public void initBinder(PortletRequestDataBinder binder,
			PortletPreferences preferences) {
		logger.debug("initBinder");
		binder.registerCustomEditor(byte[].class,
				new ByteArrayMultipartFileEditor());
	}

	// First visit //
	@RequestMapping("VIEW")	
	public String listDetail(PortletRequest request, Model model) {
		ThresholdForm thresholdForm = null;
		if (!model.containsAttribute("thresholdForm")) {
			thresholdForm = new ThresholdForm();
			model.addAttribute("thresholdForm", thresholdForm);
		} else {
			thresholdForm = (ThresholdForm) model.asMap().get("thresholdForm");
		}
		ThresholdModel thresholdModel = new ThresholdModel();
		KpiLevelModel kpiLevelModel = new KpiLevelModel();
		kpiLevelModel.setActive(1);
		String keySearch = thresholdForm.getKeySearch();
		Paging page = new Paging(); //default pageNo = 1
		
		List<KpiLevelModel> kpiLevels = service.searchKpiLevel(kpiLevelModel);
		KpiLevelModel fLevel = new KpiLevelModel();
		fLevel = kpiLevels.get(0);
		String fristLevel = fLevel.getLevelId().toString();
		if(keySearch == null || keySearch.length() == 0){
			thresholdModel.setKeySearch(fristLevel);
		}else{
			thresholdModel.setKeySearch(keySearch);
		}
		thresholdModel.setPaging(page);
		thresholdModel.setActive("99");

		List<ThresholdModel> thresholds = service.searchThreshold(thresholdModel);
		model.addAttribute("thresholds", thresholds);
		model.addAttribute("levels", kpiLevels);
		model.addAttribute("firstLevel", fristLevel);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("keyListStatus", thresholdModel.getActive());
		return "master/Threshold";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("thresholdForm") ThresholdForm thresholdForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		thresholdForm.getThresholdModel().setThresholdId(null);
		thresholdForm.getThresholdModel().setAcademicYear(getCurrentYear());
		thresholdForm.getThresholdModel().setCreatedBy(user.getFullName());
		thresholdForm.getThresholdModel().setUpdatedBy(user.getFullName());
		String keySearch = thresholdForm.getKeySearch();
		String keyListStatus = thresholdForm.getKeyListStatus();

		ResultService rs = service.saveThreshold(thresholdForm.getThresholdModel());
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("keyListStatus", keyListStatus);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", "20");
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("thresholdForm") ThresholdForm thresholdForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		thresholdForm.getThresholdModel().setAcademicYear(getCurrentYear());
		thresholdForm.getThresholdModel().setUpdatedBy(user.getFullName());
		String keySearch = thresholdForm.getKeySearch();
		String keyListStatus = thresholdForm.getKeyListStatus();
		String createStr = thresholdForm.getCreateDate();
		
		Timestamp timestamp = Timestamp.valueOf(createStr);
		thresholdForm.getThresholdModel().setCreatedDate(timestamp);
		ResultService  rs = service.updateThreshold(thresholdForm.getThresholdModel());
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("keyListStatus", keyListStatus);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", "20");
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("thresholdForm") ThresholdForm thresholdForm,
			BindingResult result, Model model) {
		ThresholdModel thresholdModel = new ThresholdModel();
		thresholdModel.setThresholdId(thresholdForm.getThresholdModel().getThresholdId());		
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteThreshold(thresholdModel);
		if(recoedCount == -9){
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}
		String pageNo = "1";
		String pageSize = thresholdForm.getPageSize();
		String keySearch = thresholdForm.getKeySearch();
		String keyListStatus = thresholdForm.getKeyListStatus();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("keyListStatus", keyListStatus);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
	}

	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("thresholdForm") ThresholdForm thresholdForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", thresholdForm.getKeySearch());
		response.setRenderParameter("keyListStatus", thresholdForm.getKeyListStatus());
		response.setRenderParameter("pageNoStr", thresholdForm.getPageNo().toString());
		response.setRenderParameter("pageSize", thresholdForm.getPageSize());
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("thresholdForm") ThresholdForm thresholdForm,
			BindingResult result, Model model) {
		String pageNo = thresholdForm.getPageNo().toString();
		String pageSize = thresholdForm.getPageSize();
		String keySearch = thresholdForm.getKeySearch();
		String keyListStatus = thresholdForm.getKeyListStatus();
		String messageCode = "";
		String messageDesc = "";
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("keyListStatus", keyListStatus);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("messageCode", messageCode);
	}
	
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listPage")
	public String RenderListPage(@RequestParam("keySearch") String keySearch,
			@RequestParam("pageNoStr") String pageNoStr,
			@RequestParam("pageSize") String pageSize,
			@RequestParam("messageDesc") String messageDesc,
			@RequestParam("messageCode") String messageCode, 
			@RequestParam("keyListStatus") String keyListStatus, 			
			Model model) {
		ThresholdModel thresholdModel = new ThresholdModel();
		KpiLevelModel kpiLevelModel = new KpiLevelModel();
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		thresholdModel.setKeySearch(keySearch);	
		thresholdModel.setActive(keyListStatus);
		thresholdModel.setPaging(page);
		thresholdModel.getPaging().setPageSize(Integer.parseInt(pageSize));		
		List<KpiLevelModel> levels = service.searchKpiLevel(kpiLevelModel);
		List<ThresholdModel> thresholds = service.searchThreshold(thresholdModel);
		model.addAttribute("thresholds", thresholds);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("levels", levels);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute( "keyListStatus" , keyListStatus);
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		return "master/Threshold";
	}

}
