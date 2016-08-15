package th.ac.chandra.eduqa.portlet;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
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
import th.ac.chandra.eduqa.form.KpiGroupTypeForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiGroupTypeModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiGroupTypeController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiGroupTypeForm" })
public class KpiGroupTypeController {
	private static final Logger logger = Logger
			.getLogger(KpiGroupTypeController.class);
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
		KpiGroupTypeForm kpiGroupTypeForm = null;
		if (!model.containsAttribute("kpiGroupTypeForm")) {
			kpiGroupTypeForm = new KpiGroupTypeForm();
			model.addAttribute("kpiGroupTypeForm", kpiGroupTypeForm);
		} else {
			kpiGroupTypeForm = (KpiGroupTypeForm) model.asMap().get("kpiGroupTypeForm");
		}
		KpiGroupTypeModel kpiGroupTypeModel = new KpiGroupTypeModel();
		Paging page = new Paging(); //default pageNo = 1
		kpiGroupTypeModel.setPaging(page);
		List<KpiGroupTypeModel> groupTypes = service.searchKpiGroupType(kpiGroupTypeModel);
		model.addAttribute("groupTypes", groupTypes);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		model.addAttribute("keySearch","");
		model.addAttribute("keyListStatus","99");
		return "master/KpiGroupType";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiGroupTypeForm.getKpiGroupTypeModel().setGroupTypeId(null);
		kpiGroupTypeForm.getKpiGroupTypeModel().setAcademicYear(getCurrentYear());
		kpiGroupTypeForm.getKpiGroupTypeModel().setCreatedBy(user.getFullName());
		kpiGroupTypeForm.getKpiGroupTypeModel().setUpdatedBy(user.getFullName());
		ResultService rs = service.saveKpiGroupType(kpiGroupTypeForm.getKpiGroupTypeModel());
		String pageNo = kpiGroupTypeForm.getPageNo().toString();
		String pageSize = kpiGroupTypeForm.getPageSize();
		String keySearch = kpiGroupTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiGroupTypeForm.getKpiGroupTypeModel().setAcademicYear(getCurrentYear());
		kpiGroupTypeForm.getKpiGroupTypeModel().setUpdatedBy(user.getFullName());
		String createStr = kpiGroupTypeForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiGroupTypeForm.getKpiGroupTypeModel().setCreatedDate(timestamp);
		ResultService rs = service.updateKpiGroupType(kpiGroupTypeForm.getKpiGroupTypeModel());
		
		String pageNo = kpiGroupTypeForm.getPageNo().toString();
		String pageSize = kpiGroupTypeForm.getPageSize();
		String keySearch = kpiGroupTypeForm.getKeySearch();
		
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		KpiGroupTypeModel kpiGroupTypeModel = new KpiGroupTypeModel();
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiGroupType(kpiGroupTypeModel);
		if(recoedCount == -9){
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}
		//Render to list page.
		String pageNo = kpiGroupTypeForm.getPageNo().toString();
		String pageSize = kpiGroupTypeForm.getPageSize();
		String keySearch = kpiGroupTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}

	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiGroupTypeForm.getKeySearch());
		response.setRenderParameter("pageNoStr", kpiGroupTypeForm.getPageNo().toString());
		response.setRenderParameter("pageSize", kpiGroupTypeForm.getPageSize());
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) {
		String pageNo = kpiGroupTypeForm.getPageNo().toString();
		String pageSize = kpiGroupTypeForm.getPageSize();
		String keySearch = kpiGroupTypeForm.getKeySearch();
		String messageCode = "";
		String messageDesc = "";
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupTypeForm") KpiGroupTypeForm kpiGroupTypeForm,
			BindingResult result, Model model) {
		String pageSize = kpiGroupTypeForm.getPageSize();
		String keySearch = kpiGroupTypeForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("keyListStatus",kpiGroupTypeForm.getKeyListStatus());
	}
	
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listPage")
	public String RenderListPage(@RequestParam("keySearch") String keySearch,
			@RequestParam("pageNoStr") String pageNoStr,
			@RequestParam("pageSize") int pageSize,
			@RequestParam("messageCode") String messageCode, 
			@RequestParam("messageDesc") String messageDesc,
			@RequestParam("keyListStatus") String keyListStatus,
			Model model) {
		KpiGroupTypeModel kpiGroupTypeModel = new KpiGroupTypeModel();
		kpiGroupTypeModel.setKeySearch(keySearch);
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiGroupTypeModel.setActive(keyListStatus);
		kpiGroupTypeModel.setPaging(page);
		kpiGroupTypeModel.getPaging().setPageSize(pageSize);
		List<KpiGroupTypeModel> groupTypes = service.searchKpiGroupType(kpiGroupTypeModel);
		model.addAttribute("groupTypes", groupTypes);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("keyListStatus", keyListStatus);
		return "master/KpiGroupType";
	}
}
