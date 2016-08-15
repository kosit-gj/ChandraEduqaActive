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
import th.ac.chandra.eduqa.form.KpiLevelForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiLevelModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiLevelController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiLevelForm" })
public class KpiLevelController {
	private static final Logger logger = Logger
			.getLogger(KpiLevelController.class);
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
		KpiLevelForm kpiLevelForm = null;
		if (!model.containsAttribute("kpiLevelForm")) {
			kpiLevelForm = new KpiLevelForm();
			model.addAttribute("kpiLevelForm", kpiLevelForm);
		} else {
			kpiLevelForm = (KpiLevelForm) model.asMap().get("kpiLevelForm");
		}
		KpiLevelModel kpiLevelModel = new KpiLevelModel();
		Paging page = new Paging(); //default pageNo = 1
		kpiLevelModel.setPaging(page);
		kpiLevelModel.setKeySearch("");
		kpiLevelModel.setActive(99);
		List<KpiLevelModel> levels = service.searchKpiLevel(kpiLevelModel);
		model.addAttribute("levels", levels);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		return "master/KpiLevel";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiLevelForm.getKpiLevelModel().setLevelId(null);
		kpiLevelForm.getKpiLevelModel().setAcademicYear(getCurrentYear());		
		kpiLevelForm.getKpiLevelModel().setActive(kpiLevelForm.getKpiLevelModel().getActive());
		kpiLevelForm.getKpiLevelModel().setCreatedBy(user.getFullName());
		kpiLevelForm.getKpiLevelModel().setUpdatedBy(user.getFullName());
		
		ResultService rs = service.saveKpiLevel(kpiLevelForm.getKpiLevelModel());
		String pageNo = kpiLevelForm.getPageNo().toString();
		String pageSize = kpiLevelForm.getPageSize();
		String keySearch = kpiLevelForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus", kpiLevelForm.getKeyListStatus().toString());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiLevelForm.getKpiLevelModel().setAcademicYear(getCurrentYear());
		kpiLevelForm.getKpiLevelModel().setActive(kpiLevelForm.getKpiLevelModel().getActive());
		kpiLevelForm.getKpiLevelModel().setUpdatedBy(user.getFullName());
		String createStr = kpiLevelForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiLevelForm.getKpiLevelModel().setCreatedDate(timestamp);
		String pageNo = kpiLevelForm.getPageNo().toString();
		
		ResultService rs = service.updateKpiLevel(kpiLevelForm.getKpiLevelModel());

		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiLevelForm.getKeySearch());
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", kpiLevelForm.getPageSize());
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus", kpiLevelForm.getKeyListStatus().toString());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		KpiLevelModel kpiLevelModel = new KpiLevelModel();
		kpiLevelModel.setLevelId(kpiLevelForm.getKpiLevelModel().getLevelId());		
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiLevel(kpiLevelModel);
		if(recoedCount == -9){
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}
		//Render to list page.
		String pageNo = kpiLevelForm.getPageNo().toString();
		String pageSize = kpiLevelForm.getPageSize();
		String keySearch = kpiLevelForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("keyListStatus", kpiLevelForm.getKeyListStatus().toString());
	}

	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) {

		String keySearch = kpiLevelForm.getKeySearch();
		String pageSize = kpiLevelForm.getPageSize();
		Integer keyListStatus = kpiLevelForm.getKeyListStatus();
		String pageNo = kpiLevelForm.getPageNo().toString();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", keyListStatus.toString());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) {
		String pageNo = kpiLevelForm.getPageNo().toString();
		String pageSize = kpiLevelForm.getPageSize();
		String keySearch = kpiLevelForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiLevelForm.getKeyListStatus().toString());
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiLevelForm") KpiLevelForm kpiLevelForm,
			BindingResult result, Model model) {
		String pageSize = kpiLevelForm.getPageSize();
		String keySearch = kpiLevelForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiLevelForm.getKeyListStatus().toString());
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
		KpiLevelModel kpiLevelModel = new KpiLevelModel();
		kpiLevelModel.setKeySearch(keySearch);
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiLevelModel.setPaging(page);
		kpiLevelModel.getPaging().setPageSize(pageSize);
		kpiLevelModel.setActive(Integer.parseInt(keyListStatus));
		List<KpiLevelModel> levels = service.searchKpiLevel(kpiLevelModel);
		model.addAttribute("levels", levels);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("keyListStatus", keyListStatus);
		return "master/KpiLevel";
	}

}
