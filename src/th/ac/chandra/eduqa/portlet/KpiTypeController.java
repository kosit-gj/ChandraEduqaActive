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
import th.ac.chandra.eduqa.form.KpiTypeForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiTypeModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiTypeController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiTypeForm" })
public class KpiTypeController {
	private static final Logger logger = Logger
			.getLogger(KpiTypeController.class);
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
		KpiTypeForm kpiTypeForm = null;
		if (!model.containsAttribute("kpiTypeForm")) {
			kpiTypeForm = new KpiTypeForm();
			model.addAttribute("kpiTypeForm", kpiTypeForm);
		} else {
			kpiTypeForm = (KpiTypeForm) model.asMap().get("kpiTypeForm");
		}
		KpiTypeModel kpiTypeModel = new KpiTypeModel();
		Paging page = new Paging(); //default pageNo = 1
		kpiTypeModel.setPaging(page);
		List<KpiTypeModel> types = service.searchKpiType(kpiTypeModel);
		model.addAttribute("types", types);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		model.addAttribute("keySearch","");
		model.addAttribute("keyListStatus","99");
		return "master/KpiType";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiTypeForm.getKpiTypeModel().setTypeId(null);
		kpiTypeForm.getKpiTypeModel().setAcademicYear(getCurrentYear());
		kpiTypeForm.getKpiTypeModel().setCreatedBy(user.getFullName());
		kpiTypeForm.getKpiTypeModel().setUpdatedBy(user.getFullName());
		ResultService rs = service.saveKpiType(kpiTypeForm.getKpiTypeModel());
		String pageNo = kpiTypeForm.getPageNo().toString();
		String pageSize = kpiTypeForm.getPageSize();
		String keySearch = kpiTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiTypeForm.getKpiTypeModel().setAcademicYear(getCurrentYear());
		kpiTypeForm.getKpiTypeModel().setUpdatedBy(user.getFullName());
		String createStr = kpiTypeForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiTypeForm.getKpiTypeModel().setCreatedDate(timestamp);
		ResultService rs = service.updateKpiType(kpiTypeForm.getKpiTypeModel());
		String pageNo = kpiTypeForm.getPageNo().toString();
		String pageSize = kpiTypeForm.getPageSize();
		String keySearch = kpiTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		KpiTypeModel kpiTypeModel = new KpiTypeModel();
		kpiTypeModel.setTypeId(kpiTypeForm.getKpiTypeModel().getTypeId());		
		//service.deleteKpiType(kpiTypeModel);
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiType(kpiTypeModel);
		if(recoedCount == -9){
			//model.addAttribute(ServiceConstant.ERROR_MESSAGE_KEY, ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE);
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}
		//Render to list page.
		String pageNo = kpiTypeForm.getPageNo().toString();
		String pageSize = kpiTypeForm.getPageSize();
		String keySearch = kpiTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}

	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiTypeForm.getKeySearch());
		response.setRenderParameter("pageNoStr", kpiTypeForm.getPageNo().toString());
		response.setRenderParameter("pageSize", kpiTypeForm.getPageSize());
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) {
		String pageNo = kpiTypeForm.getPageNo().toString();
		String pageSize = kpiTypeForm.getPageSize();
		String keySearch = kpiTypeForm.getKeySearch();
		String messageCode = "";
		String messageDesc = "";
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiTypeForm") KpiTypeForm kpiTypeForm,
			BindingResult result, Model model) {
		String pageSize = kpiTypeForm.getPageSize();
		String keySearch = kpiTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiTypeForm.getKeyListStatus());
	}
	
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listPage")
	public String RenderListPage(@RequestParam("keySearch") String keySearch,
			@RequestParam("pageNoStr") String pageNoStr,
			@RequestParam("pageSize") int pageSize,
			@RequestParam("messageDesc") String messageDesc,
			@RequestParam("messageCode") String messageCode,
			@RequestParam("keyListStatus") String keyListStatus,
			Model model) {
		KpiTypeModel kpiTypeModel = new KpiTypeModel();
		kpiTypeModel.setKeySearch(keySearch);
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiTypeModel.setPaging(page);
		kpiTypeModel.getPaging().setPageSize(pageSize);
		kpiTypeModel.setActive(keyListStatus);
		List<KpiTypeModel> types = service.searchKpiType(kpiTypeModel);
		model.addAttribute("types", types);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("keyListStatus", keyListStatus);
		return "master/KpiType";
	}
	
}
