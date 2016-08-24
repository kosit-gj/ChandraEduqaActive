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
import th.ac.chandra.eduqa.form.KpiStrucForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiGroupModel;
import th.ac.chandra.eduqa.model.KpiStrucModel;
import th.ac.chandra.eduqa.model.StrucTypeModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiStrucController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiStrucForm" })
public class KpiStrucController {
	private static final Logger logger = Logger
			.getLogger(KpiStrucController.class);
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
		KpiStrucForm kpiStrucForm = null;
		if (!model.containsAttribute("kpiStrucForm")) {
			kpiStrucForm = new KpiStrucForm();
			model.addAttribute("kpiStrucForm", kpiStrucForm);
		} else {
			kpiStrucForm = (KpiStrucForm) model.asMap().get("kpiStrucForm");
		}
		KpiStrucModel kpiStrucModel = new KpiStrucModel();
		KpiGroupModel kpiGroupModel = new KpiGroupModel();
		StrucTypeModel strucTypeModel = new StrucTypeModel();
		Paging page = new Paging();		
		List<KpiGroupModel> groups = service.searchKpiGroup(kpiGroupModel);
		kpiStrucModel.setGroupId(groups.get(0).getGroupId());
		kpiStrucModel.setKeySearch("");
		kpiStrucModel.setActive("99");
		kpiStrucModel.setPaging(page);
		List<StrucTypeModel> strucTypes = service.searchStrucType(strucTypeModel);
		List<KpiStrucModel> strucs = service.searchKpiStruc(kpiStrucModel);		
		model.addAttribute("strucs", strucs);
		model.addAttribute("groups", groups);
		model.addAttribute("strucTypes", strucTypes);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		model.addAttribute("groupId", kpiStrucModel.getGroupId());
		model.addAttribute("keySearch", kpiStrucModel.getKeySearch());
		model.addAttribute("keyListStatus", kpiStrucModel.getActive());		
		return "master/KpiStruc";
	}
	
	@RequestMapping("VIEW")
	@RenderMapping(params = "render=listPage")
	public String RenderListPage(@RequestParam("pageNoStr") String pageNoStr,
			@RequestParam("pageSize") int pageSize,
			@RequestParam("messageDesc") String messageDesc,
			@RequestParam("messageCode") String messageCode,
			@RequestParam("groupId") String groupId,
			@RequestParam("keySearch") String keySearch,
			@RequestParam("keyListStatus") String keyListStatus,
			Model model) {
		KpiStrucModel kpiStrucModel = new KpiStrucModel();
		KpiGroupModel kpiGroupModel = new KpiGroupModel();
		StrucTypeModel strucTypeModel = new StrucTypeModel();
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiStrucModel.setGroupId(Integer.parseInt(groupId));
		kpiStrucModel.setKeySearch(keySearch);
		kpiStrucModel.setActive(keyListStatus);		
		kpiStrucModel.setPaging(page);
		kpiStrucModel.getPaging().setPageSize(pageSize);
		
		List<KpiGroupModel> groups = service.searchKpiGroup(kpiGroupModel);
		List<StrucTypeModel> strucTypes = service.searchStrucType(strucTypeModel);
		List<KpiStrucModel> strucs = service.searchKpiStruc(kpiStrucModel);	
		
		model.addAttribute("strucs", strucs);
		model.addAttribute("groups", groups);
		model.addAttribute("strucTypes", strucTypes);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("groupId", groupId);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("keyListStatus", keyListStatus);		
		return "master/KpiStruc";
	}
	
	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", kpiStrucForm.getPageNo().toString()); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", kpiStrucForm.getKeySearch());
		response.setRenderParameter("keyListStatus", kpiStrucForm.getKeyListStatus());
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiStrucForm.getKpiStrucModel().setStrucId(null);
		kpiStrucForm.getKpiStrucModel().setAcademicYear(getCurrentYear());
		kpiStrucForm.getKpiStrucModel().setCreatedBy(user.getFullName());
		kpiStrucForm.getKpiStrucModel().setUpdatedBy(user.getFullName());
		
		ResultService rs = service.saveKpiStruc(kpiStrucForm.getKpiStrucModel());
		
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", kpiStrucForm.getPageNo().toString()); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", kpiStrucForm.getKeySearch());
		response.setRenderParameter("keyListStatus", kpiStrucForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiStrucForm.getKpiStrucModel().setAcademicYear(getCurrentYear());
		kpiStrucForm.getKpiStrucModel().setUpdatedBy(user.getFullName());
		String createStr = kpiStrucForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiStrucForm.getKpiStrucModel().setCreatedDate(timestamp);
		
		ResultService rs = service.updateKpiStruc(kpiStrucForm.getKpiStrucModel());
		
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", kpiStrucForm.getPageNo().toString()); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", kpiStrucForm.getKeySearch());
		response.setRenderParameter("keyListStatus", kpiStrucForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) {
		KpiStrucModel kpiStrucModel = new KpiStrucModel();
		kpiStrucModel.setStrucId(kpiStrucForm.getKpiStrucModel().getStrucId());		
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiStruc(kpiStrucModel);
		if(recoedCount == -9){
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}		
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", kpiStrucForm.getPageNo().toString()); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", kpiStrucForm.getKeySearch());
		response.setRenderParameter("keyListStatus", kpiStrucForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", kpiStrucForm.getPageNo().toString()); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", (kpiStrucForm.getKeySearch() == null ? "" : kpiStrucForm.getKeySearch()));
		response.setRenderParameter("keyListStatus", (kpiStrucForm.getKeyListStatus() == null ? "": kpiStrucForm.getKeyListStatus()));
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiStrucForm") KpiStrucForm kpiStrucForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");		
		response.setRenderParameter("pageNoStr", "1"); 
		response.setRenderParameter("pageSize", kpiStrucForm.getPageSize());
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("groupId", kpiStrucForm.getKpiStrucModel().getGroupId().toString());
		response.setRenderParameter("keySearch", (kpiStrucForm.getKeySearch() == null ? "" : kpiStrucForm.getKeySearch()));
		response.setRenderParameter("keyListStatus", (kpiStrucForm.getKeyListStatus() == null ? "": kpiStrucForm.getKeyListStatus()));
	}
}
