/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.qc.banq.gia.authentication.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.AppRepository;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class AppController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "apps/createOrUpdateAppForm";

	@Autowired
	AppRepository apps;
	
	@Autowired
	GiaBackOfficeService business;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/apps/new")
	public String initCreationForm(Map<String, Object> model) {
		App app = new App(); app.setNouveau(true);
		model.put("app", app);
		model.put("types", Arrays.asList(TypeAuth.values()));
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/apps/new")
	public String processCreationForm(@Valid App app, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.business.saveApp(app);
			return "redirect:/apps/" + app.getClientId();
		}
	}

	@GetMapping("/apps/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("app", new App());
		return "apps/findApps";
	}

	@GetMapping("/apps")
	public String processFindForm(AppPayload app, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /apps to return all records
		if (app.getTitle() == null) app.setTitle("");

		// find apps by last name
		List<AppPayload> results = this.business.findLikeTitle(app.getTitle());
		if (results.isEmpty()) {
			// no apps found
			result.rejectValue("title", "notFound", "Aucune application trouvee");
			return "apps/findApps";
		}
		else if (results.size() == 1) {
			// 1 app found
			app = results.stream().findFirst().orElse(new AppPayload());//.iterator().next();
			return "redirect:/apps/" + app.getClientId();
		}
		else {
			// multiple apps found
			model.put("selections", results);
			return "apps/appsList";
		}
	}

	@GetMapping("/apps/{appId}/edit")
	public String initUpdateAppForm(@PathVariable("appId") String appId, Model model) {
		App app = this.apps.findById(appId).orElse(null);
		model.addAttribute(app);
		model.addAttribute("types", Arrays.asList(TypeAuth.values()));
		//model.addAttribute("isnew", false);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/apps/{appId}/edit")
	public String processUpdateAppForm(@Valid App app, BindingResult result, @PathVariable("appId") String appId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			app.setClientId(appId);
			this.business.saveApp(app);
			return "redirect:/apps/{appId}";
		}
	}

	/**
	 * Custom handler for displaying an app.
	 * @param appId the ID of the app to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/apps/{appId}")
	public ModelAndView showApp(@PathVariable("appId") String appId) {
		ModelAndView mav = new ModelAndView("apps/appDetails");
		AppPayload app = this.business.findByClientId(appId); //.apps.findById(appId).orElse(null);
		mav.addObject("app", app);
		return mav;
	}

	@RequestMapping("/apps/remove")
	public String processDeleteApp(HttpServletRequest request) {
		String appId = request.getParameter("appid");
		this.business.deleteApp(appId);
		return "redirect:/apps";
	}

}