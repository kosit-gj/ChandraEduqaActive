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
import th.ac.chandra.eduqa.form.KpiUomForm;
import th.ac.chandra.eduqa.mapper.ResultService;
import th.ac.chandra.eduqa.model.KpiUomModel;
import th.ac.chandra.eduqa.model.SysYearModel;
import th.ac.chandra.eduqa.service.EduqaService;
import th.ac.chandra.eduqa.xstream.common.Paging;

@Controller("kpiUomController")
@RequestMapping("VIEW")
@SessionAttributes({ "kpiUomForm" })
public class KpiUomController {
	private static final Logger logger = Logger
			.getLogger(KpiUomController.class);
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
		KpiUomForm kpiUomForm = null;
		if (!model.containsAttribute("kpiUomForm")) {
			kpiUomForm = new KpiUomForm();
			model.addAttribute("kpiUomForm", kpiUomForm);
		} else {
			kpiUomForm = (KpiUomForm) model.asMap().get("kpiUomForm");
		}
		KpiUomModel kpiUomModel = new KpiUomModel();
		kpiUomModel.setKeySearch("");
		kpiUomModel.setActive("99");		
		Paging page = new Paging(); //default pageNo = 1
		kpiUomModel.setPaging(page);
		List<KpiUomModel> uoms = service.searchKpiUom(kpiUomModel);
		model.addAttribute("uoms", uoms);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", "1");
		
		return "master/KpiUom";
	}

	@RequestMapping(params = "action=doInsert")
	public void actionInsert(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiUomForm.getKpiUomModel().setUomId(null);
		kpiUomForm.getKpiUomModel().setAcademicYear(getCurrentYear());
		kpiUomForm.getKpiUomModel().setCreatedBy(user.getFullName());
		kpiUomForm.getKpiUomModel().setUpdatedBy(user.getFullName());
		ResultService rs = service.saveKpiUom(kpiUomForm.getKpiUomModel());
		String pageNo = kpiUomForm.getPageNo().toString();
		String pageSize = kpiUomForm.getPageSize();
		String keySearch = kpiUomForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo); 
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doEdit")
	public void actionUpdate(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) throws ParseException {
		User user = (User) request.getAttribute(WebKeys.USER);
		kpiUomForm.getKpiUomModel().setAcademicYear(getCurrentYear());
		kpiUomForm.getKpiUomModel().setUpdatedBy(user.getFullName());
		String createStr = kpiUomForm.getCreateDate();
		Timestamp timestamp = Timestamp.valueOf(createStr);
		kpiUomForm.getKpiUomModel().setCreatedDate(timestamp);
		ResultService rs = service.updateKpiUom(kpiUomForm.getKpiUomModel());
		String pageNo = kpiUomForm.getPageNo().toString();
		String pageSize = kpiUomForm.getPageSize();
		String keySearch = kpiUomForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo); 
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", rs.getMsgCode());
		response.setRenderParameter("messageDesc", rs.getMsgDesc());
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doDelete")
	public void actionDelete(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) {
		User user = (User) request.getAttribute(WebKeys.USER);
		KpiUomModel kpiUomModel = new KpiUomModel();
		kpiUomModel.setUomId(kpiUomForm.getKpiUomModel().getUomId());
		String messageDesc ="";
		String messageCode ="";
		int recoedCount=service.deleteKpiUom(kpiUomModel);
		if(recoedCount == -9){
			messageDesc = ServiceConstant.ERROR_CONSTRAINT_VIOLATION_MESSAGE_CODE;
			messageCode = "0";
		}

		String pageNo = kpiUomForm.getPageNo().toString();
		String pageSize = kpiUomForm.getPageSize();
		String keySearch = kpiUomForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageCode", messageCode);
		response.setRenderParameter("messageDesc", messageDesc);
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
	}

	@RequestMapping(params = "action=doSearch")
	public void actionSearch(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) {
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", kpiUomForm.getKeySearch());
		response.setRenderParameter("pageNoStr", kpiUomForm.getPageNo().toString());
		response.setRenderParameter("pageSize", kpiUomForm.getPageSize());
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
	}
	
	@RequestMapping(params = "action=doListPage")
	public void actionListPage(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) {
		String pageNo = kpiUomForm.getPageNo().toString();
		String pageSize = kpiUomForm.getPageSize();
		String keySearch = kpiUomForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", pageNo);
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
	
	}
	
	@RequestMapping(params = "action=doPageSize")
	public void actionPageSize(javax.portlet.ActionRequest request,
			javax.portlet.ActionResponse response,
			@ModelAttribute("kpiUomForm") KpiUomForm kpiUomForm,
			BindingResult result, Model model) {
		String pageSize = kpiUomForm.getPageSize();
		String keySearch = kpiUomForm.getKeySearch();
		response.setRenderParameter("render", "listPage");
		response.setRenderParameter("keySearch", keySearch);
		response.setRenderParameter("pageNoStr", "1");
		response.setRenderParameter("pageSize", pageSize);
		response.setRenderParameter("messageDesc", "");
		response.setRenderParameter("messageCode", "");
		response.setRenderParameter("keyListStatus", kpiUomForm.getKeyListStatus());
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
		KpiUomModel kpiUomModel = new KpiUomModel();
		kpiUomModel.setKeySearch(keySearch);
		Paging page = new Paging(Integer.parseInt(pageNoStr), 10, "ASC");
		kpiUomModel.setPaging(page);
		kpiUomModel.getPaging().setPageSize(pageSize);
		kpiUomModel.setActive(keyListStatus);
		List<KpiUomModel> uoms = service.searchKpiUom(kpiUomModel);
		model.addAttribute("uoms", uoms);
		model.addAttribute("lastPage", service.getResultPage());
		model.addAttribute("PageCur", pageNoStr);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("messageCode", messageCode);
		model.addAttribute("messageDesc", messageDesc);
		model.addAttribute("keySearch", keySearch);
		model.addAttribute("keyListStatus", keyListStatus);
		return "master/KpiUom";
	}
	
}
