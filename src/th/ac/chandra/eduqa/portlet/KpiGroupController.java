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
import th.ac.chandra.eduqa.form.KpiGroupForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiGroupModel;
import th.ac.chandra.eduqa.model.KpiGroupTypeModel;
import th.ac.chandra.eduqa.model.OrgTypeModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiGroupController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiGroupForm" })
public class KpiGroupController {
	private static final Logger logger = Logger
			.getLogger(KpiGroupController.class);
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
		KpiGroupForm kpiGroupForm = null;
		if (!model.containsAttribute("kpiGroupForm")) {
			kpiGroupForm = new KpiGroupForm();
			model.addAttribute("kpiGroupForm", kpiGroupForm);
		} else {
			kpiGroupForm = (KpiGroupForm) model.asMap().get("kpiGroupForm");
		}
		KpiGroupModel kpiGroupModel = new KpiGroupModel();
		KpiGroupTypeModel kpiGroupTypeModel = new KpiGroupTypeModel();
		kpiGroupTypeModel.setActive("1");
		OrgTypeModel orgTypeModel = new OrgTypeModel();
		Paging page = new Paging();
		String keySearch = kpiGroupForm.getKeySearch();		
		kpiGroupModel.setPaging(page);

		List<KpiGroupTypeModel> groupTypes = service.searchKpiGroupType(kpiGroupTypeModel);
		List<OrgTypeModel> orgType = service.searchOrgType(orgTypeModel);
		List<KpiGroupModel> groups = service.searchKpiGroup(kpiGroupModel);
		Integer groupResultPage = service.getResultPage();		
		model.addAttribute("groups", groups);
		model.addAttribute("groupTypes", groupTypes);
		model.addAttribute("orgTypes", orgType);
		model.addAttribute("lastPage", groupResultPage);
		model.addAttribute("PageCur", "1");
		model.addAttribute("keySearch","");
		model.addAttribute("keyListStatus","99");
		return "master/KpiGroup";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, 
			Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiGroupForm.getKpiGroupModel().setGroupId(null);
		kpiGroupForm.getKpiGroupModel().setAcademicYear(getCurrentYear());
		kpiGroupForm.getKpiGroupModel().setCreatedBy(user.getFullName());
		kpiGroupForm.getKpiGroupModel().setUpdatedBy(user.getFullName());
		ResultService rs = service.saveKpiGroup(kpiGroupForm.getKpiGroupModel());
		String pageNo = kpiGroupForm.getPageNo().toString();
		String pageSize = kpiGroupForm.getPageSize();
		String keySearch = kpiGroupForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo); 
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("keyListStatus", kpiGroupForm.getKeyListStatus());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());

	}	
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		// kpiGroupForm.getKpiGroupModel().setId(null);
		kpiGroupForm.getKpiGroupModel().setAcademicYear(getCurrentYear());
		kpiGroupForm.getKpiGroupModel().setUpdatedBy(user.getFullName());
		String createStr = kpiGroupForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiGroupForm.getKpiGroupModel().setCreatedDate(timestamp);
		ResultService rs = service.updateKpiGroup(kpiGroupForm.getKpiGroupModel());
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiGroupForm.getKeySearch() );
		response.setRenderParameter("pageNoStr", kpiGroupForm.getPageNo().toString() ); 
		response.setRenderParameter("pageSize", kpiGroupForm.getPageSize() );
		response.setRenderParameter("messageCode", rs.getMsgCode() );
		response.setRenderParameter("messageDesc", rs.getMsgDesc() );
		response.setRenderParameter("keyListStatus" ,kpiGroupForm.getKeyListStatus());
	
	}

	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		KpiGroupModel kpiGroupModel = new KpiGroupModel();
		kpiGroupModel.setGroupId(kpiGroupForm.getKpiGroupModel().getGroupId());		
		//service.deleteKpiGroup(kpiGroupModel);
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiGroup(kpiGroupModel);
		if(recoedCount == -9){
			//model.addAttribute(ServiceConstant.ERROR_MESSAGE_KEY, ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE);
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}
		//Render to list page.
		String pageNo = kpiGroupForm.getPageNo().toString();
		String pageSize = kpiGroupForm.getPageSize();
		String keySearch = kpiGroupForm.getKeySearch();
		String keyListStatus = kpiGroupForm.getKeyListStatus();
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
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, Model model) {		
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiGroupForm.getKeySearch());
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", kpiGroupForm.getPageSize());
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiGroupForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, Model model) {
		String pageNo = kpiGroupForm.getPageNo().toString();
		String pageSize = kpiGroupForm.getPageSize();
		String keySearch = kpiGroupForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiGroupForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiGroupForm") KpiGroupForm kpiGroupForm,
			BindingResult result, Model model) {
		String pageSize = kpiGroupForm.getPageSize();
		String keySearch = kpiGroupForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiGroupForm.getKeyListStatus());
	}
	
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listPage")
	public String RenderListPage(@RequestParam("keySearch") String keySearch,
			@RequestParam("keyListStatus") String keyListStatus,
			@RequestParam("pageNoStr") String pageNoStr,
			@RequestParam("pageSize") int pageSize,
			@RequestParam("messageDesc") String messageDesc,
			@RequestParam("messageCode") String messageCode, 
			Model model) {
		KpiGroupModel kpiGroupModel = new KpiGroupModel();
		KpiGroupTypeModel kpiGroupTypeModel = new KpiGroupTypeModel();
		kpiGroupTypeModel.setActive("1");
		OrgTypeModel orgTypeModel = new OrgTypeModel();
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiGroupModel.setKeySearch(keySearch);
		kpiGroupModel.setActive(keyListStatus);
		kpiGroupModel.setPaging(page);
		kpiGroupModel.getPaging().setPageSize(pageSize);

		List<KpiGroupModel> groups = service.searchKpiGroup(kpiGroupModel);
		Integer groupResultPage = service.getResultPage();
		List<KpiGroupTypeModel> groupTypes = service.searchKpiGroupType(kpiGroupTypeModel);
		List<OrgTypeModel> orgType = service.searchOrgType(orgTypeModel);
		model.addAttribute("groups", groups);
		model.addAttribute("groupTypes", groupTypes);
		model.addAttribute("orgTypes", orgType);
		model.addAttribute("lastPage", groupResultPage);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("keyListStatus", keyListStatus);
		return "master/KpiGroup";
	}
	
}
